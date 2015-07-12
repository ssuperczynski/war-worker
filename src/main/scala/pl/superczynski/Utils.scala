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

  def scanReportWS(nr: Int, amount: Int):String = {
    s"""
      {
          "type": "publish",
          "data": {
              "channel": "user_$nr",
              "event": "scan",
              "data": {
                  "amount": $amount
              }
          }
      }
       """
  }
}
