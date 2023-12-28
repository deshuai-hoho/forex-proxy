package forex.services

import cats.effect.Sync
import forex.domain._
import forex.services.rates.errors.Error
import org.http4s.client.Client
import org.http4s._
import org.http4s.implicits._
import org.typelevel.ci.CIString
import cats.implicits._

class ForexApiService[F[_]: Sync](client: Client[F]) {
  private val token = "10dc303535874aeccc86a8251e6992f5"
  def getRates(pair: Rate.Pair): F[Either[Error, Rate]] = {
    val request = Request[F](
      Method.GET,
      uri"http://localhost:8080/rates".withQueryParam("pair", pair.from.toString + pair.to.toString)
    ).withHeaders(Header.Raw(CIString("token"), token))

    println(request)
    client.expect[String](request).attempt.map { response =>
      println(response)
      Rate(pair, Price(BigDecimal(686)), Timestamp.now).asRight[Error]
    }
  }
}
case class OneFrameRate(
  from: String,
  to: String,
  bid: Price,
  ask: Price,
  price: Price,
  timestamp: Timestamp
)
case class ApiResponse(
  rates: List[OneFrameRate]
)