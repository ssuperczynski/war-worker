package pl.superczynski

import com.redis.RedisClient

class Utils {

  lazy val r = new RedisClient("localhost", 6379)
  def soldierAmountWS(nr: Int, amount: Int, range: String):String = {
    s"""
      {
          "type": "publish",
          "data": {
              "channel": "user_$nr",
              "event": "range",
              "data": {
                  "amount": $amount,
                  "range": "$range"
              }
          }
      }
       """
  }

  def scanReportWS(nr: Int, scanned: String):String = {
    val amount = r.hgetall("user_" + nr + ":counter")
    s"""
      {
          "type": "publish",
          "data": {
              "channel": "user_scan_$nr",
              "event": "scan",
              "data": {
                  "nr": $scanned,
                  "soldier": ${amount.get("soldier").toInt},
                  "food": ${amount.get("food").toInt},
                  "iron": ${amount.get("iron").toInt},
                  "concrete": ${amount.get("concrete").toInt},
                  "time": ${'"' + amount.get("time").toString + '"'}
              }
          }
      }
       """
  }
}
