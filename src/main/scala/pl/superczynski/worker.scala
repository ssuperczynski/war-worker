package pl.superczynski

import java.text.SimpleDateFormat
import java.util.Date

import com.redis.RedisClient
import com.typesafe.scalalogging.Logger
import org.slf4j.LoggerFactory

object worker extends App {

  override def main(args: Array[String]): Unit = {
    val Log = Logger(LoggerFactory.getLogger(this.getClass.toString))
    println("Worker start...")
    Log.info("STARTED")
    val r = new RedisClient("localhost", 6379)
    var i: Int = 0

    while (true) {
      Thread.sleep(1000)
      i = i + 1
      val numbers = getUsers(r)
      checkQueue(r, numbers)
    }
  }

  /**
   *
   * @param r RedisClient
   * @param numbers Set[Int]
   */
  private def checkQueue(r: RedisClient, numbers: Set[Int]): Unit = {
    val format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    val d1 = new Date()

    for (nr <- numbers) {
      val ranges = r.hkeys("user_" + nr + ":soldier:queue_time")
      ranges match {
        case Some(ss) => for (range <- ss) {
          val added_time = r.hget("user_" + nr + ":soldier:queue_time", range)
          val saved = format.parse(added_time.get)
          val diff = (d1.getTime - saved.getTime) / 1000 // diff in sec
          val interval = r.hget("user_" + nr + ":soldier:interval", range)
          val modulo = diff % interval.get.toInt

          if (modulo == 0) {
            val queue_amount = r.hget("user_" + nr + ":soldier:queue_amount", range)
            if (queue_amount.get.toInt >= 1) {
              r.hincrby("user_" + nr + ":soldier:amount", range, 1)
              r.hincrby("user_" + nr + ":soldier:queue_amount", range, -1)
              val amount = r.hget("user_" + nr + ":soldier:amount", range)
              val jsonString = (new Utils).jsonString(nr, amount.get.toInt, range)
              println("amount: " + amount.get.toInt)
              println("range: " + range)
              r.publish("socket-redis-down", jsonString)
            }
          }
        }
        case None =>
        case _ =>
      }
    }
    val d2 = new Date()
    println("loop time: " + (d2.getTime - d1.getTime) + " milliseconds")
  }

  /**
   *
   * @param r RedisClient
   * @return
   */
  private def getUsers(r: RedisClient): Set[Int] = {
    val list = r.keys("*user_*:soldier:interval*")
    var numbers = Set[Int]()

    list match {
      case Some(s) => for (k <- s) {
        val key = k.get
        val nr = key.replaceAll("[^0-9?]", "")
        numbers += nr.toInt
      }
      case None =>
    }
    numbers
  }
}
