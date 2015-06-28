package pl.superczynski

class Utils {

  def jsonString(nr: Int, amount: Int):String = {
    s"""
      {
          "type": "publish",
          "data":
              {
                  "channel": "user_$nr",
                  "event": "range",
                  "data": ${amount + 1}
              }
      }
       """
  }
}
