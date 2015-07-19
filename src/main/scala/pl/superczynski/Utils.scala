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
    val counters_amount = r.hgetall("user_" + nr + ":counter")
    val soldiers_amount = r.hgetall("user_" + nr + ":soldier:amount")
    s"""
      {
          "type": "publish",
          "data": {
              "channel": "user_scan_$nr",
              "event": "scan",
              "data": {
                  "nr": $scanned,
                  "soldier": ${counters_amount.get("soldier").toInt},
                  "food": ${counters_amount.get("food").toInt},
                  "iron": ${counters_amount.get("iron").toInt},
                  "concrete": ${counters_amount.get("concrete").toInt},
                  "time": ${'"' + counters_amount.get("time").toString + '"'},
                  "Sergeant": ${soldiers_amount.get("Sergeant").toInt},
                  "Warrant_Officer": ${soldiers_amount.get("Warrant_Officer").toInt},
                  "Private": ${soldiers_amount.get("Private").toInt},
                  "Corporal": ${soldiers_amount.get("Corporal").toInt}
              }
          }
      }
       """
  }
}
