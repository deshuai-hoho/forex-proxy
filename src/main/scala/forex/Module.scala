package forex

import cats.effect.{ Concurrent, Timer }
import forex.config.ApplicationConfig
import forex.http.rates.RatesHttpRoutes
import forex.services._
import forex.services.rates.interpreters._
import forex.programs._
import org.http4s._
import org.http4s.implicits._
import org.http4s.server.middleware.{ AutoSlash, Timeout }
import org.http4s.client.Client
import java.util.concurrent.{Executors, TimeUnit}
import forex.common.cache.CacheFactory

class Module[F[_]: Concurrent: Timer](config: ApplicationConfig, client: Client[F]) {

  val ownRequestQuata = CacheFactory.createCache[String, Int]()
  setupQuotaResetScheduler()

  private val forexApiService = new ForexApiService[F](client)
  private val ratesService: RatesService[F] = new OneFrameLive[F](forexApiService, config.requestQuota.sourceQuota)

  private val ratesProgram: RatesProgram[F] = RatesProgram[F](ratesService)

  private val ratesHttpRoutes: HttpRoutes[F] = new RatesHttpRoutes[F](ratesProgram, ownRequestQuata, config.requestQuota.directQuota).routes

  type PartialMiddleware = HttpRoutes[F] => HttpRoutes[F]
  type TotalMiddleware   = HttpApp[F] => HttpApp[F]

  private val routesMiddleware: PartialMiddleware = {
    { http: HttpRoutes[F] =>
      AutoSlash(http)
    }
  }

  private val appMiddleware: TotalMiddleware = { http: HttpApp[F] =>
    Timeout(config.http.timeout)(http)
  }

  private val http: HttpRoutes[F] = LoggingMiddleware(ratesHttpRoutes)

  val httpApp: HttpApp[F] = appMiddleware(routesMiddleware(http).orNotFound)

  private def setupQuotaResetScheduler(): Unit = {
    /**
      * TODO centralized scheduler for distribute system
      */
    val scheduler = Executors.newScheduledThreadPool(1)

    scheduler.scheduleAtFixedRate(new Runnable {
      override def run(): Unit = {
        ownRequestQuata.clear()
      }
    }, 0, 1, TimeUnit.DAYS)
    ()
  }
}
