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
import play.api.http.Status.{BAD_REQUEST, INTERNAL_SERVER_ERROR, NOT_FOUND, OK, SERVICE_UNAVAILABLE}
import uk.gov.hmrc.dprs.BaseIntegrationWithConnectorSpec

class ReadSubscriptionSpec extends BaseIntegrationWithConnectorSpec {

  override val connectorPath: String      = "/dac6/dct70d/v1"
  override lazy val connectorName: String = "read-subscription"

  "attempting to read a subscription, when" - {
    "the request is" - {
      "valid" in {
        stubFor(
          post(urlEqualTo(connectorPath))
            .withRequestBody(
              equalToJson(
                s"""
                 |{
                 |    "displaySubscriptionForMDRRequest": {
                 |        "requestCommon": {
                 |            "regime": "MDR",
                 |            "receiptDate": "$currentDateAndTime",
                 |            "acknowledgementReference": "$acknowledgementReference",
                 |            "originatingSystem": "MDTP"
                 |        },
                 |        "requestDetail": {
                 |            "IDType": "MDR",
                 |            "IDNumber": "a7405c8d-06ee-46a3-b5a0-5d65176360ec"
                 |        }
                 |    }
                 |}
                 |""".stripMargin
              )
            )
            .willReturn(
              aResponse()
                .withHeader("Content-Type", "application/json")
                .withStatus(OK)
                .withBody(s"""
                     |{
                     |    "displaySubscriptionForMDRResponse": {
                     |        "responseCommon": {
                     |            "status": "OK",
                     |            "processingDate": "2020-09-12T18:03:45Z"
                     |        },
                     |        "responseDetail": {
                     |            "subscriptionID": "XAMDR0000XE0000352129",
                     |            "tradingName": "Baumbach-Waelchi",
                     |            "isGBUser": true,
                     |            "primaryContact": {
                     |                "email": "christopher.wisoky@example.com",
                     |                "phone": "687394104",
                     |                "mobile": "73744443225",
                     |                "individual": {
                     |                    "firstName": "Josefina",
                     |                    "lastName": "Zieme"
                     |                }
                     |            },
                     |            "secondaryContact": {
                     |                "email": "cody.halvorson@example.com",
                     |                "organisation": {
                     |                    "organisationName": "Daugherty, Mante and Rodriguez"
                     |                }
                     |            }
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
          response = response,
          status = OK,
          jsonBodyOpt = Some(
            """
              |{
              |    "id": "XAMDR0000XE0000352129",
              |    "name": "Baumbach-Waelchi",
              |    "contacts": [
              |        {
              |            "type": "I",
              |            "firstName": "Josefina",
              |            "middleName": null,
              |            "lastName": "Zieme",
              |            "landline": "687394104",
              |            "mobile": "73744443225",
              |            "emailAddress": "christopher.wisoky@example.com"
              |        },
              |        {
              |            "type": "O",
              |            "name": "Daugherty, Mante and Rodriguez",
              |            "landline": null,
              |            "mobile": null,
              |            "emailAddress": "cody.halvorson@example.com"
              |        }
              |    ]
              |}
              |""".stripMargin
          )
        )
        verifyThatDownstreamApiWasCalled()
      }
      "valid but the integration call fails with response:" - {
        "bad request" in {
          stubFor(
            post(urlEqualTo(connectorPath))
              .withRequestBody(
                equalToJson(
                  s"""
                     |{
                     |    "displaySubscriptionForMDRRequest": {
                     |        "requestCommon": {
                     |            "regime": "MDR",
                     |            "receiptDate": "$currentDateAndTime",
                     |            "acknowledgementReference": "$acknowledgementReference",
                     |            "originatingSystem": "MDTP"
                     |        },
                     |        "requestDetail": {
                     |            "IDType": "MDR",
                     |            "IDNumber": "a7405c8d-06ee-46a3-b5a0-5d65176360ec"
                     |        }
                     |    }
                     |}
                     |""".stripMargin
                )
              )
              .willReturn(
                aResponse()
                  .withHeader("Content-Type", "application/json")
                  .withStatus(BAD_REQUEST)
                  .withBody(s"""
                       |{
                       |    "errorDetail": {
                       |        "timestamp": "$currentDateAndTime",
                       |        "correlationId": "566297cf-78a7-4bf5-9f1a-f3632bda7e12",
                       |        "errorCode": "400",
                       |        "errorMessage": "Invalid message",
                       |        "source": "JSON validation",
                       |        "sourceFaultDetail": {
                       |            "detail": [
                       |                "object has missing required parameters (['regime'])"
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
        "not found" in {
          stubFor(
            post(urlEqualTo(connectorPath))
              .withRequestBody(
                equalToJson(
                  s"""
                     |{
                     |    "displaySubscriptionForMDRRequest": {
                     |        "requestCommon": {
                     |            "regime": "MDR",
                     |            "receiptDate": "$currentDateAndTime",
                     |            "acknowledgementReference": "$acknowledgementReference",
                     |            "originatingSystem": "MDTP"
                     |        },
                     |        "requestDetail": {
                     |            "IDType": "MDR",
                     |            "IDNumber": "a7405c8d-06ee-46a3-b5a0-5d65176360ec"
                     |        }
                     |    }
                     |}
                     |""".stripMargin
                )
              )
              .willReturn(
                aResponse()
                  .withHeader("Content-Type", "application/json")
                  .withStatus(NOT_FOUND)
                  .withBody(s"""
                       |{
                       |    "errorDetail": {
                       |    "timestamp": "$currentDateAndTime",
                       |    "correlationId": "1e8cff35-854e-4972-b64a-b585ee499f41",
                       |    "errorCode": "404",
                       |    "errorMessage": "Record not found",
                       |    "source": "Back End",
                       |    "sourceFaultDetail": {
                       |        "detail": [
                       |            "Record not found"
                       |        ]
                       |    }
                       |}
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
            NOT_FOUND,
            Some(
              """
              |[
              |  {
              |    "code": "eis-returned-not-found"
              |  }
              |]
              |""".stripMargin
            )
          )
          verifyThatDownstreamApiWasCalled()
        }
        "service unavailable" in {
          stubFor(
            post(urlEqualTo(connectorPath))
              .withRequestBody(
                equalToJson(
                  s"""
                     |{
                     |    "displaySubscriptionForMDRRequest": {
                     |        "requestCommon": {
                     |            "regime": "MDR",
                     |            "receiptDate": "$currentDateAndTime",
                     |            "acknowledgementReference": "$acknowledgementReference",
                     |            "originatingSystem": "MDTP"
                     |        },
                     |        "requestDetail": {
                     |            "IDType": "MDR",
                     |            "IDNumber": "a7405c8d-06ee-46a3-b5a0-5d65176360ec"
                     |        }
                     |    }
                     |}
                     |""".stripMargin
                )
              )
              .willReturn(
                aResponse()
                  .withHeader("Content-Type", "application/json")
                  .withStatus(SERVICE_UNAVAILABLE)
                  .withBody(s"""
                       |{
                       |    "errorDetail": {
                       |    "timestamp": "$currentDateAndTime",
                       |    "correlationId": "a40a8a76-7b7a-4b00-ab2b-7cfba64aa820",
                       |    "errorCode": "503",
                       |    "errorMessage": "Request could not be processed",
                       |    "source": "Back End",
                       |    "sourceFaultDetail": {
                       |        "detail": [
                       |            "003 - Request could not be processed"
                       |        ]
                       |    }
                       |}
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
                |    "code": "eis-returned-service-unavailable"
                |  }
                |]
                |""".stripMargin
            )
          )
          verifyThatDownstreamApiWasCalled()
        }
        "internal server error" in {
          stubFor(
            post(urlEqualTo(connectorPath))
              .withRequestBody(
                equalToJson(
                  s"""
                     |{
                     |    "displaySubscriptionForMDRRequest": {
                     |        "requestCommon": {
                     |            "regime": "MDR",
                     |            "receiptDate": "$currentDateAndTime",
                     |            "acknowledgementReference": "$acknowledgementReference",
                     |            "originatingSystem": "MDTP"
                     |        },
                     |        "requestDetail": {
                     |            "IDType": "MDR",
                     |            "IDNumber": "a7405c8d-06ee-46a3-b5a0-5d65176360ec"
                     |        }
                     |    }
                     |}
                     |""".stripMargin
                )
              )
              .willReturn(
                aResponse()
                  .withHeader("Content-Type", "application/json")
                  .withStatus(INTERNAL_SERVER_ERROR)
                  .withBody(s"""
                       |{
                       |    "errorDetail": {
                       |        "timestamp": "$currentDateAndTime",
                       |        "correlationId": "3e8873a3-b8d4-4d95-aa2b-9e8ab397422b",
                       |        "errorCode": "500",
                       |        "errorMessage": "Internal Server Error",
                       |        "source": "journey-dct70c-service-camel",
                       |        "sourceFaultDetail": {
                       |            "detail": [
                       |                "Internal Server Error"
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
                |    "code" : "eis-returned-internal-server-error"
                |  }
                |]
                |""".stripMargin
            )
          )
          verifyThatDownstreamApiWasCalled()
        }
      }
      "invalid" in {
        stubFor(
          post(urlEqualTo(connectorPath))
            .withRequestBody(
              equalToJson(
                s"""
                   |{
                   |    "displaySubscriptionForMDRRequest": {
                   |        "requestCommon": {
                   |            "regime": "MDR",
                   |            "receiptDate": "$currentDateAndTime",
                   |            "acknowledgementReference": "$acknowledgementReference",
                   |            "originatingSystem": "MDTP"
                   |        },
                   |        "requestDetail": {
                   |            "IDType": "MDR",
                   |            "IDNumber": "a7405c8d-06ee-46a3-b5a0-5d65176360ec"
                   |        }
                   |    }
                   |}
                   |""".stripMargin
              )
            )
            .willReturn(
              aResponse()
                .withHeader("Content-Type", "application/json")
                .withStatus(OK)
                .withBody(
                  s"""
                     |{
                     |    "displaySubscriptionForMDRResponse": {
                     |        "responseCommon": {
                     |            "status": "OK",
                     |            "processingDate": "2020-09-12T18:03:45Z"
                     |        },
                     |        "responseDetail": {
                     |            "subscriptionID": "XAMDR0000XE0000352129",
                     |            "tradingName": "Baumbach-Waelchi",
                     |            "isGBUser": true,
                     |            "primaryContact": {
                     |                "email": "christopher.wisoky@example.com",
                     |                "phone": "687394104",
                     |                "mobile": "73744443225",
                     |                "individual": {
                     |                    "firstName": "Josefina",
                     |                    "lastName": "Zieme"
                     |                }
                     |            },
                     |            "secondaryContact": {
                     |                "email": "cody.halvorson@example.com",
                     |                "organisation": {
                     |                    "organisationName": "Daugherty, Mante and Rodriguez"
                     |                }
                     |            }
                     |        }
                     |    }
                     |}
                     |""".stripMargin)
            )
        )

        val response = wsClient
          .url(fullUrl("/subscriptions"))
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
              |    "requested": "/dprs/subscriptions"
              |}
              |""".stripMargin
          )
        )
        verifyThatDownstreamApiWasNotCalled()
      }
    }
  }
}
