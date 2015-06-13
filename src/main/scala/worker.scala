import java.text.SimpleDateFormat
import java.util.Date

import com.redis._

object worker extends App {

  override def main(args: Array[String]): Unit = {
    println("Worker start...")
    val r = new RedisClient("localhost", 6379)
    var i: Int = 0

    while (true) {
        Thread.sleep(1000)
        println("loop: " + i)
        i = i + 1
        val numbers: List[Int] = getUsers(r)
        checkQueue(r, numbers)

    }

  }

  private def checkQueue(r: RedisClient, numbers: List[Int]): Unit = {
    val d1 = new Date()
    val format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    val now = new Date()
    for (nr <- numbers) {
      val ranges = r.hkeys("user_" + nr + ":soldier:queue_time")
      ranges match {
        case Some(ss) => for (range <- ss) {
          val added_time = r.hget("user_" + nr + ":soldier:queue_time", range)
          val saved = format.parse(added_time.get)
          val diff = (now.getTime - saved.getTime) / 1000 // diff in sec
          val interval = r.hget("user_" + nr + ":soldier:interval", range)
          val modulo = diff % interval.get.toInt
          if (modulo == 0) {
            r.hincrby("user_" + nr + ":soldier:amount", range, 1)
            val queue_amount = r.hget("user_" + nr + ":soldier:queue_amount", range)
            if (queue_amount.get.toInt <= 0) {
              r.hdel("user_" + nr + ":soldier:queue_amount", range)
              r.hdel("user_" + nr + ":soldier:queue_time", range)
            } else {
              r.hincrby("user_" + nr + ":soldier:queue_amount", range, -1)
            }

          }

        }
        case None =>
      }
    }
    val d2 = new Date()
    println("loop time: " + (d2.getTime - d1.getTime) + " milliseconds")
  }

  private def getUsers(r: RedisClient): List[Int] = {
    val list = r.keys("*user_*:soldier:interval*")
    var numbers = List[Int]()

    list match {
      case Some(s) => for (k <- s) {
        val key = k.get
        val nr = key.replaceAll("[^0-9?]", "")
        numbers = nr.toInt :: numbers
      }
      case None =>
    }
    numbers
  }
}
