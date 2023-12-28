package forex.http
package rates

import cats.effect.Sync
import cats.implicits._
import forex.programs.RatesProgram
import forex.programs.rates.{ Protocol => RatesProgramProtocol }
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import org.http4s.server.Router
import org.http4s.headers.Allow
import org.http4s.Method
import forex.domain.Currency
import forex.programs.rates.errors

class RatesHttpRoutes[F[_]: Sync](rates: RatesProgram[F]) extends Http4sDsl[F] {

  import Converters._, Protocol._

  private[http] val prefixPath = "/rates"

  object OptionalFromQueryParam extends OptionalQueryParamDecoderMatcher[String]("from")
  object OptionalToQueryParam extends OptionalQueryParamDecoderMatcher[String]("to")

  private val httpRoutes: HttpRoutes[F] = HttpRoutes.of[F] {

    // Validate query parameters
    case GET -> Root :? OptionalFromQueryParam(fromOpt) +& OptionalToQueryParam(toOpt) =>
      (fromOpt, toOpt) match {
        case (Some(fromStr), Some(toStr)) =>
          (Currency.fromString(fromStr), Currency.fromString(toStr)) match {
            case (Some(from), Some(to)) =>
              rates.get(RatesProgramProtocol.GetRatesRequest(from, to))
                .flatMap(Sync[F].fromEither)
                .flatMap( rate => Ok(rate.asGetApiResponse))
                .handleErrorWith {
                  case errors.Error.RateLookupFailed(msg) => InternalServerError(msg)
                  case _ => InternalServerError("Internal server error")
              }
            case (None, None) => BadRequest("Both 'from' and 'to' query parameters are invalid currencies.")
            case (None, _) => BadRequest("'from' query parameter is an invalid currency.")
            case (_, None) => BadRequest("'to' query parameter is an invalid currency.")
          }
        case (None, None) => BadRequest("Missing both 'from' and 'to' query parameters.")
        case (None, _) => BadRequest("Missing 'from' query parameter.")
        case (_, None) => BadRequest("Missing 'to' query parameter.")
      }
    case _ => MethodNotAllowed(Allow(Method.GET))
  }

  val routes: HttpRoutes[F] = Router(
    prefixPath -> httpRoutes
  )

}
