import java.util.regex.Pattern

import akka.actor._
import ua.com.entity._

  object ValidateURLActor {
    def props: Props = Props[ValidateURLActor]

    val urlPattern = "([A-Za-z]{3,9}:(?:\\/\\/)?)+([a-zA-Z0-9.-]+(:[a-zA-Z0-9.&%$-]+)*@)*" +
                      "((?!(10\\.|172\\.(1[6-9]|2\\d|3[01])\\.|192\\.168\\.).*)(?!255\\.255\\.255\\.255)" +
                      "(25[0-5]|2[1,3]\\d|[1]\\d\\d|[1-9]\\d|[1-9])(\\.(25[0-5]|2[0-4]\\d|[1]\\d\\d|[1-9]\\d|\\d)){3}|" +
                      "[^\\.\\-\\s]([a-zA-Z0-9-]+\\.)*[a-zA-Z0-9-]+\\." +
                      "(com|edu|gov|int|mil|net|org|biz|arpa|info|name|pro|aero|coop|museum|[a-zA-Z]{1,9})|" +
                      "(\\[(([\\da-fA-F]{4}(:|::)){1}([\\da-fA-F]{1,4}(:|::)){1,6}([:]{1,2})?" +
                      "([\\da-fA-F]{1,4}){1})\\])|((?:\\+?(\\d{1}))?[- (](\\d{3})[- )](\\d{3})[- ](\\d{1,4})))(:[0-9]+)*" +
                      "(\\/($|[a-zA-Z0-9\\.,\\?'\\\\\\+&%$#=~_\\-\\(\\)]+)?)*"

    def isValid(url: String): Boolean = Pattern.matches(urlPattern, url)
  }

  class ValidateURLActor extends Actor {
    import ValidateURLActor._

    def receive = {
      case a: InputURL =>
        if (isValid(a.url)) {
          sender() ! ValidURL(a.url)
          println("Valid url.")
          } else {
          println("Invalid url. Please, please provide a valid URL.")
        }
    }
  }

