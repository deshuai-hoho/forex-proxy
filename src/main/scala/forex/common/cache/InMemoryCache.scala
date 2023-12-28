package forex.common.cache

import java.util.concurrent.ConcurrentHashMap
import java.time.Instant

class InMemoryCache[K, V] extends Cache[K, V] {
  private val cache = new ConcurrentHashMap[K, CacheEntry[V]]()

  override def get(key: K): Option[V] = {
    Option(cache.get(key)).flatMap( entry => 
      if (entry.expiryTime.isAfter(Instant.now)) Some(entry.value)
      else {
        cache.remove(key)
        None
      }
    )
  }

  override def put(key: K, value: V): Unit = {
    val entry = CacheEntry(value, Instant.MAX)
    cache.put(key, entry)
    ()
  }

  override def put(key: K, value: V, ttl: Long): Unit = {
    val expiryTime = Instant.now.plusMillis(ttl)
    val entry = CacheEntry(value, expiryTime)
    cache.put(key, entry)
    ()
  }
}

case class CacheEntry[V](value: V, expiryTime: Instant)