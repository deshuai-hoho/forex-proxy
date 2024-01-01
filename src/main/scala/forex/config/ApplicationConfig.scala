package forex.config

import scala.concurrent.duration.FiniteDuration

case class ApplicationConfig(
    http: HttpConfig,
    requestQuota: RequestQuotaConfig
)

case class RequestQuotaConfig(
    directQuota: Int,
    sourceQuota: Int
)

case class HttpConfig(
    host: String,
    port: Int,
    timeout: FiniteDuration
)
