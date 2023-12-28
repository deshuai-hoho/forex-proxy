package forex.services.rates.interpreters

import forex.services.rates.Algebra
import forex.services.ForexApiService
import forex.services.rates.errors.Error
import forex.domain.{ Rate, Price, Timestamp }
import cats.implicits._
import cats.effect.Sync
import java.time.OffsetDateTime

class OneFrameLive[F[_]: Sync](forexApiService: ForexApiService[F]) extends Algebra[F] {
  override def get(pair: Rate.Pair): F[Error Either Rate] = {
    forexApiService.getRates(pair: Rate.Pair).flatMap {
      case Right(rates) =>
        println(s"rates: $rates")
        rates.find(r => r.from == pair.from.toString && r.to == pair.to.toString) match {
          case Some(oneFrameRate) =>
            Sync[F].pure(Rate(pair, Price(oneFrameRate.price), Timestamp(OffsetDateTime.parse(oneFrameRate.time_stamp))).asRight)
          case None =>
            Sync[F].pure(Error.OneFrameLookupFailed(s"Rate not found for pair ${pair.from} to ${pair.to}").asLeft)

        }
      case Left(error) =>
        println(s"error: $error")
        Sync[F].pure(error.asLeft)
    }
  }
}