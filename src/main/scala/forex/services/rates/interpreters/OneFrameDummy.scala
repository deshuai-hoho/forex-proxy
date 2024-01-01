package forex.services.rates.interpreters

import forex.services.rates.Algebra
import cats.Applicative
import cats.syntax.applicative._
import cats.syntax.either._
import forex.domain.{ Price, Rate, Timestamp }
import forex.services.rates.errors._

class OneFrameDummy[F[_]: Applicative] extends Algebra[F] {

  override def get(pair: Rate.Pair, token: String): F[Error Either Rate] =
    Rate(pair, Price(BigDecimal(686)), Timestamp.now).asRight[Error].pure[F]

}
