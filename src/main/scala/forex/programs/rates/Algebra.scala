package forex.programs.rates

import forex.domain.Rate
import errors._

trait Algebra[F[_]] {
  def get(request: Protocol.GetRatesRequest, token: String): F[Error Either Rate]
}
