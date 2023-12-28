package forex.common.cache

trait Cache[K, V] {
  def get(key: K): Option[V]
  def put(key: K, value: V): Unit
  def put(key: K, value: V, ttl: Long): Unit
}