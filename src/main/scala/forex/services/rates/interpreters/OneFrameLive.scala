package forex.services.rates.interpreters

import forex.services.rates.Algebra
import forex.services.ForexApiService
import forex.services.rates.errors.Error
import forex.domain.{ Rate, Price, Timestamp }
import cats.implicits._
import cats.effect.Sync
import java.time.{ OffsetDateTime, Duration }
import forex.common.cache.CacheFactory
import java.util.concurrent.PriorityBlockingQueue
import scala.collection.mutable.HashSet
import org.slf4j.LoggerFactory
import java.time.Instant

case class TokenQuota(token: String, var quota: Int) extends Comparable[TokenQuota] {
  override def compareTo(o: TokenQuota): Int = o.quota.compareTo(this.quota)
}

class OneFrameLive[F[_]: Sync](forexApiService: ForexApiService[F], quota: Int) extends Algebra[F] {
  private final val CACHE_TTL = 5 * 60 * 1000 // 5 minutes
  private final val ONEFRAME_QUOTA = quota

  private val logger = LoggerFactory.getLogger("http4s")

  private val pairCache = CacheFactory.createCache[(String, String), Rate]()
  private val tokenPool = new PriorityBlockingQueue[TokenQuota]()
  private val tokenSet = new HashSet[String]()
  private val frozenToken = CacheFactory.createCache[String, (Int, Instant)]()

  private def selectToken(token: String): Option[TokenQuota] = {
    frozenToken.get(token) match {
      case Some(value) => {
        val (quota: Int, freezeTime: Instant) = value
        if (freezeTime.isAfter(Instant.now)) {
          tokenPool.synchronized {
            tokenPool.add(TokenQuota(token, quota))
            val tokenQuota = tokenPool.poll()
            tokenPool.add(tokenQuota)
            Some(tokenQuota)
          }
        } else {
          None
        }
      }
      case None => {
        val tokenQuota = tokenPool.poll()
        tokenPool.add(tokenQuota)
        Some(tokenQuota)
      }
    }
  }

  private def freezeToken(tokenQuota: TokenQuota, duration: Duration): Unit = {
    tokenPool.remove(tokenQuota)

    frozenToken.put(tokenQuota.token, (tokenQuota.quota, Instant.now().plus(duration)))

    ()
  }
  /**
    * SimpleFrozenStrategy
    *
    * @param tokenQuota
    */
  private def freezeTokenIfNeeded(tokenQuota: TokenQuota): Unit = {
    val percentageLeft = tokenQuota.quota.toDouble / ONEFRAME_QUOTA
    if (percentageLeft < 0.05) {
      if (tokenPool.size() < 5) {
        freezeToken(tokenQuota, Duration.ofHours(2))
      } else if (tokenPool.size() < 10) {
        freezeToken(tokenQuota, Duration.ofMinutes(90))
      }
    } else if (percentageLeft < 0.2) {
      if (tokenPool.size() < 5) {
        freezeToken(tokenQuota, Duration.ofHours(1))
      } else if (tokenPool.size() < 10) {
        freezeToken(tokenQuota, Duration.ofMinutes(30))
      }
    } else if (percentageLeft < 0.5 && tokenPool.size() < 3) {
      freezeToken(tokenQuota, Duration.ofMinutes(30))
    }
  }

  private def postProcess(rate:Option[Rate], tokenQuota: TokenQuota): Unit = {
    // update rate cache
    rate match {
      case Some(value) => pairCache.put((value.pair.from.toString, value.pair.to.toString), value, CACHE_TTL)
      case None => {}
    }
    // update token quota
    tokenPool.synchronized {
      tokenQuota.quota -= 1

      // re-sort
      tokenPool.remove(tokenQuota)
      tokenPool.add(tokenQuota)
      freezeTokenIfNeeded(tokenQuota)
    }
    ()
  }

  private def getRatesFromApi(pair: Rate.Pair, tokenQ: TokenQuota): F[Error Either Rate] = {
    logger.info(s"Getting rates from api for pair ${pair.from} to ${pair.to}")
    forexApiService.getRates(pair: Rate.Pair, tokenQ.token).flatMap {
      case Right(rates) =>
        rates.find(r => r.from == pair.from.toString && r.to == pair.to.toString) match {
          case Some(oneFrameRate) =>
            val rate = Rate(pair, Price(oneFrameRate.price), Timestamp(OffsetDateTime.parse(oneFrameRate.time_stamp)))
            postProcess(Some(rate), tokenQ)
            Sync[F].pure(rate.asRight)
          case None =>
            postProcess(None, tokenQ)
            Sync[F].pure(Error.OneFrameLookupFailed(s"Rate not found for pair ${pair.from} to ${pair.to}").asLeft)
        }
      case Left(error) =>
        Sync[F].pure(error.asLeft)
    }
  }

  override def get(pair: Rate.Pair, token: String): F[Error Either Rate] = {
    // add token to token pool
    if (!tokenSet.contains(token)) {
      tokenSet.add(token)
      tokenPool.add(TokenQuota(token, ONEFRAME_QUOTA))
    }

    // check rates cache
    pairCache.get((pair.from.toString, pair.to.toString)) match {
      case Some(rate) =>
        logger.info(s"Rate found in cache for pair ${pair.from} to ${pair.to}")
        Sync[F].pure(rate.asRight)
      case None =>
        selectToken(token) match {
          case Some(tokenQuota) =>
            getRatesFromApi(pair, tokenQuota)
          case _ =>
            Sync[F].pure(Error.OneFrameLookupFailed(s"Resource constraints, please try later").asLeft)
        }
        
    }
  }
}