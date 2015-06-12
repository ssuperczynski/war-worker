import com.redis._

object worker extends App {

  override def main(args: Array[String]): Unit = {
    println("Worker start...")
    val r = new RedisClient("localhost", 6379)
    var i: Int = 0
//    while (true) {
      try {

        val list = r.keys("*soldier*")

        list match {
          case Some(s) => for (k <- s) {
            val amount = r.hget(k.get, "amount")
            val added_time = r.hget(k.get, "time")
            println(amount.get)
            println(added_time.get)
          }
        }

        i = i + 1
        Thread.sleep(1000)

      } catch {
        case e: Exception =>
          println(e.getMessage)
      }

//    }

  }
}
