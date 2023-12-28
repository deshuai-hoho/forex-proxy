package forex.services

import cats.effect.Sync
import forex.domain._
import forex.services.rates.errors.Error
import forex.services.rates.errors.Error.OneFrameLookupFailed
import org.http4s.client.Client
import org.http4s._
import org.http4s.implicits._
import org.typelevel.ci.CIString
import io.circe.generic.auto._
import org.http4s.circe.CirceEntityDecoder._
import cats.implicits._

class ForexApiService[F[_]: Sync](client: Client[F]) {
  private val token = "10dc303535874aeccc86a8251e6992f5"
  def getRates(pair: Rate.Pair): F[Either[Error, List[OneFrameRate]]] = {
    /**
      * For OneFrame API
      * * Successful return:
      *   [{
      *      "from":"EUR",
      *      "to":"USD",
      *      "bid":0.27402200624561224,
      *      "ask":0.5175526761655438,
      *      "price":0.39578734120557802,
      *      "time_stamp":"2023-12-28T12:33:35.535Z"
      *   }]
      * * Pair with same currency return:
      *   []
      * * Invalid token return
      *    {"error": "Forbidden"}
      */
    val request = Request[F](
      Method.GET,
      uri"http://localhost:8080/rates".withQueryParam("pair", pair.from.toString + pair.to.toString)
    ).withHeaders(Header.Raw(CIString("token"), token))

    println(request)
    client.expect[List[OneFrameRate]](request).attempt.map {
      case Right(rates) => Right(rates)
      case Left(error) => Left(OneFrameLookupFailed(error.getMessage))
    }
  }
}
case class OneFrameRate(
  from: String,
  to: String,
  bid: Double,
  ask: Double,
  price: Double,
  time_stamp: String
)
