package forex.services.rates.interpreters

import forex.services.rates.Algebra
import forex.services.ForexApiService
import forex.services.rates.errors.Error
import forex.domain.{ Rate }

class OneFrameLive[F[_]](forexApiService: ForexApiService[F]) extends Algebra[F] {
  override def get(pair: Rate.Pair): F[Error Either Rate] =
    forexApiService.getRates(pair)
}