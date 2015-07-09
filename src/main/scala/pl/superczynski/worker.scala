package pl.superczynski

import java.text.SimpleDateFormat
import java.util.Date

import com.redis.RedisClient

object worker extends App {

  lazy val r = new RedisClient("localhost", 6379)
  override def main(args: Array[String]): Unit = {
    var i: Int = 0
    var numbers = Set[Int]()
    var list_size = 0
    while (true) {
      Thread.sleep(1000)
      i = i + 1

      if (list_size == 0) {
        numbers = getUsers
        list_size = numbers.size
      }

      checkQueue(numbers)
    }
  }

  /**
   *
   * @param numbers Set[Int]
   */
  private def checkQueue(numbers: Set[Int]): Unit = {
    val d1 = new Date()

    for (nr <- numbers) {
      val ranges = r.hkeys("user_" + nr + ":soldier:queue_time")
      ranges match {
        case Some(s) => for (range <- s) {
          addSoldiers(range, nr)
        }
        case None =>
        case _ =>
      }
    }

    println("loop time: " + (new Date().getTime - d1.getTime) + " milliseconds")
  }

  /**
   *
   * @return
   */
  private def getUsers: Set[Int] = {

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

  /**
   *
   * @param range range
   * @param nr user nr
   * @return
   */
  private def addSoldiers(range: String, nr: Int) = {
    val format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    val added_time = r.hget("user_" + nr + ":soldier:queue_time", range)
    val saved = format.parse(added_time.get)
    val diff = (new Date().getTime - saved.getTime) / 1000 // diff in sec
    val interval = r.hget("user_" + nr + ":soldier:interval", range)

    if (diff % interval.get.toInt == 0) {
      val queue_amount = r.hget("user_" + nr + ":soldier:queue_amount", range)
      if (queue_amount.get.toInt >= 1) {
        r.hincrby("user_" + nr + ":soldier:amount", range, 1)
        r.hincrby("user_" + nr + ":soldier:queue_amount", range, -1)
        val amount = r.hget("user_" + nr + ":soldier:amount", range)
        val jsonString = (new Utils).jsonString(nr, amount.get.toInt, range)
        r.publish("socket-redis-down", jsonString)
      }
    }
  }
}
