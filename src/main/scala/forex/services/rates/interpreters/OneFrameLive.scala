package forex.services.rates.interpreters

import forex.services.rates.Algebra
import forex.services.ForexApiService
import forex.services.rates.errors.Error
import forex.domain.{ Rate, Price, Timestamp }
import cats.implicits._
import cats.effect.Sync
import java.time.OffsetDateTime
import forex.common.cache.InMemoryCache

class OneFrameLive[F[_]: Sync](forexApiService: ForexApiService[F]) extends Algebra[F] {
  private final val CACHE_TTL = 5 * 60 * 1000 // 5 minutes

  private val pairCache = new InMemoryCache[(String, String), Rate]()

  private def getRatesFromApi(pair: Rate.Pair): F[Error Either Rate] = {
    forexApiService.getRates(pair: Rate.Pair).flatMap {
      case Right(rates) =>
        println(s"rates: $rates")
        rates.find(r => r.from == pair.from.toString && r.to == pair.to.toString) match {
          case Some(oneFrameRate) =>
            val rate = Rate(pair, Price(oneFrameRate.price), Timestamp(OffsetDateTime.parse(oneFrameRate.time_stamp)))
            pairCache.put((pair.from.toString, pair.to.toString), rate, CACHE_TTL)
            Sync[F].pure(rate.asRight)
          case None =>
            Sync[F].pure(Error.OneFrameLookupFailed(s"Rate not found for pair ${pair.from} to ${pair.to}").asLeft)
        }
      case Left(error) =>
        println(s"error: $error")
        Sync[F].pure(error.asLeft)
    }
  }

  override def get(pair: Rate.Pair): F[Error Either Rate] = {
    // add token to token pool
    // check rates cache
    pairCache.get((pair.from.toString, pair.to.toString)) match {
      case Some(rate) =>
        Sync[F].pure(rate.asRight)
      case None =>
        getRatesFromApi(pair)
    }
    // getRates
    // update cache
    // check throttle strategy

    
  }
}