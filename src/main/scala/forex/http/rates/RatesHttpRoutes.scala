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
import org.typelevel.ci.CIString
import forex.common.cache._
import org.http4s.Response

class RatesHttpRoutes[F[_]: Sync](rates: RatesProgram[F], quotaUsage: Cache[String, Int], quota: Int) extends Http4sDsl[F] {

  import Converters._, Protocol._

  private[http] val prefixPath = "/rates"

  private final val REQUEST_QUOTA_PER_TOKEN = quota

  object OptionalFromQueryParam extends OptionalQueryParamDecoderMatcher[String]("from")
  object OptionalToQueryParam extends OptionalQueryParamDecoderMatcher[String]("to")

  private val httpRoutes: HttpRoutes[F] = HttpRoutes.of[F] {

    // Validate query parameters
    case req @ GET -> Root :? OptionalFromQueryParam(fromOpt) +& OptionalToQueryParam(toOpt) =>
      val maybeToken = req.headers.get(CIString("token")).map(_.head.value)

      def processRequest: F[Response[F]] = (fromOpt, toOpt) match {
        case (Some(fromStr), Some(toStr)) =>
          (Currency.fromString(fromStr), Currency.fromString(toStr)) match {
            case (Some(from), Some(to)) =>
              rates.get(RatesProgramProtocol.GetRatesRequest(from, to), maybeToken.get)
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

      def checkQuota(token: String): F[Response[F]] = {
        quotaUsage.get(token) match {
          case Some(0) => BadRequest("Your Quota exceeded today")
          case Some(usage) => {
            quotaUsage.put(token, usage - 1)
            processRequest
          }
          case None => {
            quotaUsage.put(token, REQUEST_QUOTA_PER_TOKEN - 1)
            processRequest
          }
        }
      }
      
      maybeToken match {
        case Some(token) => checkQuota(token)
        case None => BadRequest("Forbidden, Invalid Token")
      }

      
    case _ => MethodNotAllowed(Allow(Method.GET))
  }

  val routes: HttpRoutes[F] = Router(
    prefixPath -> httpRoutes
  )

}
