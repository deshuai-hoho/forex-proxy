package forex.services

import cats.effect.IO
import forex.domain._
import org.http4s._
import org.http4s.client.Client
import org.scalamock.scalatest.MockFactory
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

class ForexApiServiceTest extends AnyFunSuite with Matchers with MockFactory {

  test("ForexApiService should return a valid rate on successful response") {
    val mockClient = mock[Client[IO]]
    val pair = Rate.Pair(Currency.USD, Currency.EUR)
    val expectedResponse = ApiResponse(
      List(OneFrameRate("USD", "EUR", 0.61, 0.82, 0.71, "2023-12-28T05:42:57.78Z"))
    )
    val response = 

    (mockClient.expect[ApiResponse](_: Request[IO])(_: EntityDecoder[IO, ApiResponse]))
      .expects(*, *)
      .returning(IO.pure(apiResponse))

    val forexApiService = new ForexApiService[IO](mockClient)
    val result = forexApiService.getRates(pair).unsafeRunSync()

    result should be a 'right
    result.right.get should be apiResponse
  }

  // Additional tests for error scenarios
}