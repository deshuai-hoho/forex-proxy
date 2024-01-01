package forex.common.cache

object CacheFactory {
  // for more environments configuration
  // def createCache[K, V](config: ApplicationConfig)
  def createCache[K, V](): Cache[K, V] = {
    new InMemoryCache[K, V]()
  }
}