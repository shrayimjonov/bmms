import _root_.io.gatling.core.scenario.Simulation
import ch.qos.logback.classic.{Level, LoggerContext}
import io.gatling.core.Predef._
import io.gatling.http.Predef._
import org.slf4j.LoggerFactory

import scala.concurrent.duration._

/**
 * Performance test for the BookkeepingTransaction entity.
 */
class BookkeepingTransactionGatlingTest extends Simulation {

    val context: LoggerContext = LoggerFactory.getILoggerFactory.asInstanceOf[LoggerContext]
    // Log all HTTP requests
    //context.getLogger("io.gatling.http").setLevel(Level.valueOf("TRACE"))
    // Log failed HTTP requests
    //context.getLogger("io.gatling.http").setLevel(Level.valueOf("DEBUG"))

    val baseURL = Option(System.getProperty("baseURL")) getOrElse """http://localhost:8082"""

    val httpConf = http
        .baseUrl(baseURL)
        .inferHtmlResources()
        .acceptHeader("*/*")
        .acceptEncodingHeader("gzip, deflate")
        .acceptLanguageHeader("fr,fr-fr;q=0.8,en-us;q=0.5,en;q=0.3")
        .connectionHeader("keep-alive")
        .userAgentHeader("Mozilla/5.0 (Macintosh; Intel Mac OS X 10.10; rv:33.0) Gecko/20100101 Firefox/33.0")
        .silentResources // Silence all resources like css or css so they don't clutter the results

    val headers_http = Map(
        "Accept" -> """application/json"""
    )

    val headers_http_authentication = Map(
        "Content-Type" -> """application/json""",
        "Accept" -> """application/json"""
    )

    val headers_http_authenticated = Map(
        "Accept" -> """application/json""",
        "Authorization" -> "${access_token}"
    )

    val scn = scenario("Test the BookkeepingTransaction entity")
        .exec(http("First unauthenticated request")
        .get("/api/account")
        .headers(headers_http)
        .check(status.is(401))
        ).exitHereIfFailed
        .pause(10)
        .exec(http("Authentication")
        .post("/api/authenticate")
        .headers(headers_http_authentication)
        .body(StringBody("""{"username":"admin", "password":""}""")).asJson
        .check(header("Authorization").saveAs("access_token"))).exitHereIfFailed
        .pause(2)
        .exec(http("Authenticated request")
        .get("/api/account")
        .headers(headers_http_authenticated)
        .check(status.is(200)))
        .pause(10)
        .repeat(2) {
            exec(http("Get all bookkeepingTransactions")
            .get("/services/pkbillms/api/bookkeeping-transactions")
            .headers(headers_http_authenticated)
            .check(status.is(200)))
            .pause(10 seconds, 20 seconds)
            .exec(http("Create new bookkeepingTransaction")
            .post("/services/pkbillms/api/bookkeeping-transactions")
            .headers(headers_http_authenticated)
            .body(StringBody("""{
                "groupId":"0"
                , "sMfo":"SAMPLE_TEXT"
                , "sInn":"SAMPLE_TEXT"
                , "sAccount":"SAMPLE_TEXT"
                , "rMfo":"SAMPLE_TEXT"
                , "rInn":"SAMPLE_TEXT"
                , "rAccount":"SAMPLE_TEXT"
                , "amount":"SAMPLE_TEXT"
                , "period":"DAY"
                , "bookkeepingType":"INTERNAL"
                , "success":null
                , "details":"SAMPLE_TEXT"
                , "docType":"SAMPLE_TEXT"
                , "docCode":"SAMPLE_TEXT"
                , "docTaskCode":"SAMPLE_TEXT"
                , "rAccountName":"SAMPLE_TEXT"
                , "confirmUserID":"SAMPLE_TEXT"
                , "sendUserID":"SAMPLE_TEXT"
                , "cancelUserID":"SAMPLE_TEXT"
                , "createTime":"2020-01-01T00:00:00.000Z"
                , "confirmTime":"2020-01-01T00:00:00.000Z"
                , "sendTime":"2020-01-01T00:00:00.000Z"
                , "cancelTime":"2020-01-01T00:00:00.000Z"
                , "absId":"0"
                , "docId":"0"
                , "templateId":"0"
                , "control":"0"
                , "responseText":"SAMPLE_TEXT"
                , "type":"NEW"
                , "unionId":"0"
                , "groupIds":"SAMPLE_TEXT"
                , "subRecipient":"SAMPLE_TEXT"
                , "subRecipientId":"0"
                , "comment":"SAMPLE_TEXT"
                , "bookkeepingPayType":"CONTRACT"
                , "params":"SAMPLE_TEXT"
                }""")).asJson
            .check(status.is(201))
            .check(headerRegex("Location", "(.*)").saveAs("new_bookkeepingTransaction_url"))).exitHereIfFailed
            .pause(10)
            .repeat(5) {
                exec(http("Get created bookkeepingTransaction")
                .get("/services/pkbillms${new_bookkeepingTransaction_url}")
                .headers(headers_http_authenticated))
                .pause(10)
            }
            .exec(http("Delete created bookkeepingTransaction")
            .delete("/services/pkbillms${new_bookkeepingTransaction_url}")
            .headers(headers_http_authenticated))
            .pause(10)
        }

    val users = scenario("Users").exec(scn)

    setUp(
        users.inject(rampUsers(Integer.getInteger("users", 100)) during (Integer.getInteger("ramp", 1) minutes))
    ).protocols(httpConf)
}
