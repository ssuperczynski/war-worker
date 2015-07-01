package pl.superczynski

class Utils {

  def jsonString(nr: Int, amount: Int, range: String):String = {
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
}
