package pl.superczynski

class Utils {

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

  def scanReportWS(nr: Int, amount: Option[Map[String, String]]):String = {
    s"""
      {
          "type": "publish",
          "data": {
              "channel": "user_scan_$nr",
              "event": "scan",
              "data": {
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
