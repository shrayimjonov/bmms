import _root_.io.gatling.core.scenario.Simulation
import ch.qos.logback.classic.{Level, LoggerContext}
import io.gatling.core.Predef._
import io.gatling.http.Predef._
import org.slf4j.LoggerFactory

import scala.concurrent.duration._

/**
 * Performance test for the Transaction entity.
 */
class TransactionGatlingTest extends Simulation {

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

    val scn = scenario("Test the Transaction entity")
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
            exec(http("Get all transactions")
            .get("/services/pkbillms/api/transactions")
            .headers(headers_http_authenticated)
            .check(status.is(200)))
            .pause(10 seconds, 20 seconds)
            .exec(http("Create new transaction")
            .post("/services/pkbillms/api/transactions")
            .headers(headers_http_authenticated)
            .body(StringBody("""{
                "requestAmount":"0"
                , "requestCcy":"0"
                , "requestTrId":"SAMPLE_TEXT"
                , "requestLogin":"SAMPLE_TEXT"
                , "requestGroup":"SAMPLE_TEXT"
                , "senderName":"SAMPLE_TEXT"
                , "senderAccount":"SAMPLE_TEXT"
                , "senderServiceId":"0"
                , "senderAmount":"0"
                , "senderCcy":"0"
                , "senderTrId":"SAMPLE_TEXT"
                , "senderSuccess":null
                , "senderDate":"2020-01-01T00:00:00.000Z"
                , "senderStatus":"SAMPLE_TEXT"
                , "senderStatusText":"SAMPLE_TEXT"
                , "senderBillId":"0"
                , "senderGateway":"SAMPLE_TEXT"
                , "gatewayAmount":"0"
                , "recipientName":"SAMPLE_TEXT"
                , "recipientAccount":"SAMPLE_TEXT"
                , "recipientServiceId":"0"
                , "recipientAmount":"0"
                , "recipientCcy":"0"
                , "recipientTrId":"SAMPLE_TEXT"
                , "recipientSuccess":null
                , "recipientDate":"2020-01-01T00:00:00.000Z"
                , "recipientStatus":"SAMPLE_TEXT"
                , "recipientStatusText":"SAMPLE_TEXT"
                , "recipientBillId":"0"
                , "recipientGateway":"SAMPLE_TEXT"
                , "systemCommissionUpAmount":"0"
                , "systemCommissionDownAmount":"0"
                , "systemFeeAmount":"0"
                , "agentFeeAmount":"0"
                , "roundAmount":"0"
                , "systemFee":"0"
                , "systemCurrency":"0"
                , "ccyRate":"0"
                , "status":"NEW"
                , "comment":"SAMPLE_TEXT"
                , "date":"2020-01-01T00:00:00.000Z"
                , "payType":"CASH"
                , "requestSubAccount":"SAMPLE_TEXT"
                , "eposMerchant":"SAMPLE_TEXT"
                , "eposTerminal":"SAMPLE_TEXT"
                , "main":null
                , "amountRatio":"0"
                , "senderBean":"SAMPLE_TEXT"
                , "category":"SAMPLE_TEXT"
                , "promo":"SAMPLE_TEXT"
                }""")).asJson
            .check(status.is(201))
            .check(headerRegex("Location", "(.*)").saveAs("new_transaction_url"))).exitHereIfFailed
            .pause(10)
            .repeat(5) {
                exec(http("Get created transaction")
                .get("/services/pkbillms${new_transaction_url}")
                .headers(headers_http_authenticated))
                .pause(10)
            }
            .exec(http("Delete created transaction")
            .delete("/services/pkbillms${new_transaction_url}")
            .headers(headers_http_authenticated))
            .pause(10)
        }

    val users = scenario("Users").exec(scn)

    setUp(
        users.inject(rampUsers(Integer.getInteger("users", 100)) during (Integer.getInteger("ramp", 1) minutes))
    ).protocols(httpConf)
}
