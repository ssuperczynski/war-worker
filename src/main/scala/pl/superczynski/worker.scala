package pl.superczynski

import java.text.SimpleDateFormat
import java.util.Date

import com.redis.RedisClient

object worker extends App {

  lazy val r = new RedisClient("localhost", 6379)

  override def main(args: Array[String]): Unit = {

    var i: Int = 0
    var users_list_size = 0
    while (true) {
      Thread.sleep(1000)
      i = i + 1
      if (users_list_size == 0) users_list_size = getUsers.size
      val d1 = new Date()
      queueSoldiers(getUsers)
      queueScan()
      println("loop time: " + (new Date().getTime - d1.getTime) + " milliseconds")
    }
  }

  /**
   * @return
   */
  private def queueScan(): Unit = {
    val list = r.keys("*user_*:scan:**")
    val format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    list match {
      case Some(keys) => for (key <- keys) {
        val time = r.hget(key.get, "time")
        val saved = format.parse(time.get)
        if (isScanned(saved)) sendScanReport(key)
      }
      case None =>
    }
  }

  /**
   *
   * @return
   */
  private def isScanned(saved: Date): Boolean = {
    val diff = (new Date().getTime - saved.getTime) / 1000
    println(diff)
    diff >= 0
  }

  private def sendScanReport(key: Option[String]) = {
    val nr = key.get.replaceAll("[^0-9?]", "")
    val user_nr = nr.take(1).toInt
    val scanned = r.hget(key.get, "scanned")
    val json = (new Utils).scanReportWS(user_nr, scanned.get)

    r.del(key.get)
    r.decr("user_" + user_nr + ":scan_amount")
    r.lpush("user_" + user_nr + ":messages", json)
    r.publish("socket-redis-down", json)
  }

  /**
   *
   * @param numbers Set[Int]
   */
  private def queueSoldiers(numbers: Set[Int]): Unit = {
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
  }

  /**
   *
   * @return
   */
  private def getUsers: Set[Int] = {

    val list = r.keys("*user_*:soldier:interval*")
    var numbers = Set[Int]()
    list match {
      case Some(keys) => for (k <- keys) {
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
        val json = (new Utils).soldierAmountWS(nr, amount.get.toInt, range)
        r.publish("socket-redis-down", json)
      }
    }
  }
}
