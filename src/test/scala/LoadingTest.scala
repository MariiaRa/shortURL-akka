import io.gatling.core.Predef._
import io.gatling.http.Predef._

class LoadingTest extends Simulation {

  private val baseURL = "http://localhost:8080"
  private val uri = "http://localhost:8080/urls/stats"
  private val contentType = "application/json"
  private val endpoint = "/urls/stats"
  private val requestCount = 7000

  val httpConf = http
    .baseURL(baseURL)
    .contentTypeHeader(contentType)
    .userAgentHeader("curl/7.47.0")

  val scn = scenario("URLTasks")
    .exec(http("request_1")
      .get("/urls/stats")
      .check(status.is(200), jsonPath("$..totalURLCount").ofType[Int].is(17)))
    .pause(1)
    .exec(http("request_2")
      .put("/urls")
      .body(StringBody("""{ "url": "https://www.blazemeter.com" }""")).asJSON
      .body(StringBody("""{ "url": "https://www.atlassian.com" }""")).asJSON
      .body(StringBody("""{ "url": "http://www.marcphilipp.de" }""")).asJSON
      .body(StringBody("""{ "url": "https://gatling.io" }""")).asJSON
      check (status.is(201)))
    .pause(1)
    .exec(http("request_3")
      .get("/urls/www.short.com/dQ7SLbF_")
      .check(status.is(200), regex("ycombinator.").exists, substring("https://news.ycombinator.com/").exists))
    .pause(1)
    .exec(http("request_4")
      .get("/urls/stats/www.short.com/dMZXgD63")
      .check(status.is(200), substring("clickCount").exists, jsonPath("$..clickCount").ofType[Int].is(22)))

  setUp(scn.inject(atOnceUsers(requestCount))).protocols(httpConf)

}
