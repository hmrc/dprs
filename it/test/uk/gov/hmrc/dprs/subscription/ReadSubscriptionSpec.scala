/*
 * Copyright 2024 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.dprs.subscription

import com.github.tomakehurst.wiremock.client.WireMock._
import play.api.http.Status._
import uk.gov.hmrc.dprs.BaseBackendIntegrationSpec
import uk.gov.hmrc.dprs.connectors.subscription.ReadSubscriptionConnector

class ReadSubscriptionSpec extends BaseBackendIntegrationSpec {

  override val baseConnectorPath: String = ReadSubscriptionConnector.connectorPath

  "attempting to read a subscription, when" - {
    "the request is" - {
      "valid" in {
        stubFor(
          get(urlEqualTo(baseConnectorPath + "/" + "XLD1234567891"))
            .willReturn(
              aResponse()
                .withHeader("Content-Type", "application/json")
                .withStatus(OK)
                .withBody(s"""
                 |{
                 |  "success": {
                 |    "processingDate": "2024-01-25T09:26:17Z",
                 |    "customer": {
                 |      "id": "XLD1234567891",
                 |      "tradingName": "James Hank",
                 |      "gbUser": true,
                 |      "primaryContact": {
                 |        "individual": {
                 |          "firstName": "Mark",
                 |          "middleName": "Jacob",
                 |          "lastName": "Robinson"
                 |        },
                 |        "email": "markrobinson@hmrc.gov.uk",
                 |        "phone": "0202731454",
                 |        "mobile": "07896543333"
                 |      },
                 |      "secondaryContact": {
                 |        "organisation": {
                 |          "organisationName": "Tools for Traders"
                 |        },
                 |        "email": "toolsfortraders@example.com",
                 |        "phone": "+44 020 39898980",
                 |        "mobile": "+44 07896542228"
                 |      }
                 |    }
                 |  }
                 |}
                 |""".stripMargin)
            )
        )

        val response = wsClient
          .url(fullUrl("/subscriptions/XLD1234567891"))
          .withHttpHeaders(("Content-Type", "application/json"))
          .get()
          .futureValue

        assertAsExpected(
          response = response,
          status = OK,
          jsonBodyOpt = Some(
            """
              |{
              |    "id": "XLD1234567891",
              |    "name": "James Hank",
              |    "contacts": [
              |        {
              |            "type": "I",
              |            "firstName": "Mark",
              |            "middleName": "Jacob",
              |            "lastName": "Robinson",
              |            "landline": "0202731454",
              |            "mobile": "07896543333",
              |            "emailAddress": "markrobinson@hmrc.gov.uk"
              |        },
              |        {
              |            "type": "O",
              |            "name": "Tools for Traders",
              |            "landline": "+44 020 39898980",
              |            "mobile": "+44 07896542228",
              |            "emailAddress": "toolsfortraders@example.com"
              |        }
              |    ]
              |}
              |""".stripMargin
          )
        )
        verifyThatDownstreamApiWasCalled()
      }
      "valid but the integration call fails with response:" - {
        "internal server error" in {
          stubFor(
            get(urlEqualTo(baseConnectorPath + "/" + "a7405c8d-06ee-46a3-b5a0-5d65176360ec"))
              .willReturn(
                aResponse()
                  .withHeader("Content-Type", "application/json")
                  .withStatus(INTERNAL_SERVER_ERROR)
              )
          )
          val response = wsClient
            .url(fullUrl("/subscriptions/a7405c8d-06ee-46a3-b5a0-5d65176360ec"))
            .withHttpHeaders(("Content-Type", "application/json"))
            .get()
            .futureValue

          assertAsExpected(
            response,
            INTERNAL_SERVER_ERROR,
            Some(
              """
                |[
                |  {
                |    "code" : "eis-returned-internal-server-error"
                |  }
                |]
                |""".stripMargin
            )
          )
          verifyThatDownstreamApiWasCalled()
        }
        "could not be processed" in {
          stubFor(
            get(urlEqualTo(baseConnectorPath + "/" + "a7405c8d-06ee-46a3-b5a0-5d65176360ec"))
              .willReturn(
                aResponse()
                  .withHeader("Content-Type", "application/json")
                  .withStatus(UNPROCESSABLE_ENTITY)
                  .withBody(s"""
                               |{
                               |    "errorDetail": {
                               |        "timestamp": "$currentDateAndTime",
                               |        "correlationId": "3e8873a3-b8d4-4d95-aa2b-9e8ab397422b",
                               |        "errorCode": "003",
                               |        "errorMessage": "Request could not be processed",
                               |        "source": "ETMP",
                               |        "sourceFaultDetail": {
                               |            "detail": [
                               |                "Request could not be processed"
                               |            ]
                               |        }
                               |    }
                               |}
                               |""".stripMargin)
              )
          )
          val response = wsClient
            .url(fullUrl("/subscriptions/a7405c8d-06ee-46a3-b5a0-5d65176360ec"))
            .withHttpHeaders(("Content-Type", "application/json"))
            .get()
            .futureValue

          assertAsExpected(
            response,
            SERVICE_UNAVAILABLE,
            Some(
              """
                |[
                |  {
                |    "code" : "eis-returned-service-unavailable"
                |  }
                |]
                |""".stripMargin
            )
          )
          verifyThatDownstreamApiWasCalled()
        }
        "invalid ID" in {
          stubFor(
            get(urlEqualTo(baseConnectorPath + "/" + "a7405c8d-06ee-46a3-b5a0-5d65176360ec"))
              .willReturn(
                aResponse()
                  .withHeader("Content-Type", "application/json")
                  .withStatus(UNPROCESSABLE_ENTITY)
                  .withBody(s"""
                               |{
                               |    "errorDetail": {
                               |        "timestamp": "$currentDateAndTime",
                               |        "correlationId": "3e8873a3-b8d4-4d95-aa2b-9e8ab397422b",
                               |        "errorCode": "016",
                               |        "errorMessage": "Invalid ID",
                               |        "source": "ETMP",
                               |        "sourceFaultDetail": {
                               |            "detail": [
                               |                "Invalid ID"
                               |            ]
                               |        }
                               |    }
                               |}
                               |""".stripMargin)
              )
          )
          val response = wsClient
            .url(fullUrl("/subscriptions/a7405c8d-06ee-46a3-b5a0-5d65176360ec"))
            .withHttpHeaders(("Content-Type", "application/json"))
            .get()
            .futureValue

          assertAsExpected(response, INTERNAL_SERVER_ERROR)
          verifyThatDownstreamApiWasCalled()
        }
        "create or amend in progress" in {
          stubFor(
            get(urlEqualTo(baseConnectorPath + "/" + "a7405c8d-06ee-46a3-b5a0-5d65176360ec"))
              .willReturn(
                aResponse()
                  .withHeader("Content-Type", "application/json")
                  .withStatus(UNPROCESSABLE_ENTITY)
                  .withBody(s"""
                               |{
                               |    "errorDetail": {
                               |        "timestamp": "$currentDateAndTime",
                               |        "correlationId": "3e8873a3-b8d4-4d95-aa2b-9e8ab397422b",
                               |        "errorCode": "201",
                               |        "errorMessage": "Create/amend is in progress",
                               |        "source": "ETMP",
                               |        "sourceFaultDetail": {
                               |            "detail": [
                               |                "Create/amend is in progress"
                               |            ]
                               |        }
                               |    }
                               |}
                               |""".stripMargin)
              )
          )
          val response = wsClient
            .url(fullUrl("/subscriptions/a7405c8d-06ee-46a3-b5a0-5d65176360ec"))
            .withHttpHeaders(("Content-Type", "application/json"))
            .get()
            .futureValue

          assertAsExpected(
            response,
            SERVICE_UNAVAILABLE,
            Some(
              """
                |[
                |  {
                |    "code" : "eis-returned-service-unavailable"
                |  }
                |]
                |""".stripMargin
            )
          )
          verifyThatDownstreamApiWasCalled()
        }
        "forbidden" in {
          stubFor(
            get(urlEqualTo(baseConnectorPath + "/" + "a7405c8d-06ee-46a3-b5a0-5d65176360ec"))
              .willReturn(
                aResponse()
                  .withHeader("Content-Type", "application/json")
                  .withStatus(FORBIDDEN)
              )
          )
          val response = wsClient
            .url(fullUrl("/subscriptions/a7405c8d-06ee-46a3-b5a0-5d65176360ec"))
            .withHttpHeaders(("Content-Type", "application/json"))
            .get()
            .futureValue

          assertAsExpected(
            response,
            FORBIDDEN,
            Some(
              """
                |[
                |  {
                |    "code" : "eis-returned-forbidden"
                |  }
                |]
                |""".stripMargin
            )
          )
          verifyThatDownstreamApiWasCalled()
        }
        "bad gateway" in {
          stubFor(
            get(urlEqualTo(baseConnectorPath + "/" + "a7405c8d-06ee-46a3-b5a0-5d65176360ec"))
              .willReturn(
                aResponse()
                  .withHeader("Content-Type", "application/json")
                  .withStatus(BAD_GATEWAY)
              )
          )
          val response = wsClient
            .url(fullUrl("/subscriptions/a7405c8d-06ee-46a3-b5a0-5d65176360ec"))
            .withHttpHeaders(("Content-Type", "application/json"))
            .get()
            .futureValue

          assertAsExpected(
            response,
            BAD_GATEWAY,
            Some(
              """
                |[
                |  {
                |    "code" : "eis-returned-bad-gateway"
                |  }
                |]
                |""".stripMargin
            )
          )
          verifyThatDownstreamApiWasCalled()
        }
      }
      "invalid" in {
        val response = wsClient
          .url(fullUrl("/subscriptions/"))
          .withHttpHeaders(("Content-Type", "application/json"))
          .get()
          .futureValue

        assertAsExpected(
          response = response,
          status = NOT_FOUND,
          jsonBodyOpt = Some(
            """
              |{
              |    "statusCode": 404,
              |    "message": "URI not found",
              |    "requested": "/dprs/subscriptions/"
              |}
              |""".stripMargin
          )
        )
        verifyThatDownstreamApiWasNotCalled()
      }
    }
  }
}
