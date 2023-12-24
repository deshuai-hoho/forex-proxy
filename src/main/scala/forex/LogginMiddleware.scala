package forex

import org.http4s._
import org.slf4j.LoggerFactory
import cats.implicits._
import cats.data._
import cats.effect.Sync

object LoggingMiddleware {
    private val logger = LoggerFactory.getLogger("http4s")
    def apply[F[_]: Sync](httpRoutes: HttpRoutes[F]): HttpRoutes[F] = Kleisli{ req =>
        OptionT.liftF(Sync[F].delay(logger.info(s"Request: ${req.method} ${req.uri}"))) *> httpRoutes(req)
    }
}