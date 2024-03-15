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
import uk.gov.hmrc.dprs.BaseIntegrationWithConnectorSpec

class CreateSubscriptionSpec extends BaseIntegrationWithConnectorSpec {

  override val connectorPath: String = "/dac6/dct70c/v1"
  override lazy val connectorName: String = "create-subscription"

  "attempting to create a subscription, when" - {
    "the request is" - {
      "valid, when" - {
        "containing a single contact" in {
          stubFor(
            post(urlEqualTo(connectorPath))
              .withRequestBody(equalToJson(s"""
                                              {
                                              |    "createSubscriptionForMDRRequest": {
                                              |        "requestCommon": {
                                              |            "regime": "MDR",
                                              |            "receiptDate": "$currentDateAndTime",
                                              |            "acknowledgementReference": "$acknowledgementReference",
                                              |            "originatingSystem": "MDTP"
                                              |        },
                                              |        "requestDetail": {
                                              |            "IDType": "NINO",
                                              |            "IDNumber": "AA000000A",
                                              |            "tradingName": "Harold Winter",
                                              |            "isGBUser": true,
                                              |            "primaryContact": {
                                              |                "individual": {
                                              |                    "firstName": "Patrick",
                                              |                    "middleName": "John",
                                              |                    "lastName": "Dyson"
                                              |                },
                                              |                "email": "Patrick.Dyson@example.com",
                                              |                "phone": "747663966",
                                              |                "mobile": "38390756243"
                                              |            }
                                              |        }
                                              |    }
                                              |}
                                              |""".stripMargin))
              .willReturn(
                aResponse()
                  .withHeader("Content-Type", "application/json")
                  .withStatus(OK)
                  .withBody(s"""
                               |{
                               |  "createSubscriptionForMDRResponse" : {
                               |    "responseCommon" : {
                               |      "status" : "OK",
                               |      "processingDate" : "$currentDateAndTime"
                               |    },
                               |    "responseDetail" : {
                               |      "subscriptionID" : "a7405c8d-06ee-46a3-b5a0-5d65176360ec"
                               |    }
                               |  }
                               |}
                               |""".stripMargin)
              )
          )

          val response = wsClient
            .url(fullUrl("/subscriptions"))
            .withHttpHeaders(("Content-Type", "application/json"))
            .post("""
                    |{
                    |    "id": {
                    |        "type": "NINO",
                    |        "value": "AA000000A"
                    |    },
                    |    "name": "Harold Winter",
                    |    "contacts": [
                    |        {
                    |            "type": "I",
                    |            "firstName": "Patrick",
                    |            "middleName": "John",
                    |            "lastName": "Dyson",
                    |            "landline": "747663966",
                    |            "mobile": "38390756243",
                    |            "emailAddress": "Patrick.Dyson@example.com"
                    |        }
                    |    ]
                    |}
                    |""".stripMargin)
            .futureValue

          assertAsExpected(
            response = response,
            status = OK,
            jsonBodyOpt = Some(
              """
                |{
                |  "id": "a7405c8d-06ee-46a3-b5a0-5d65176360ec"
                |}
                |""".stripMargin
            )
          )
          verifyThatDownstreamApiWasCalled()
        }
        "containing multiple contacts" in {
          stubFor(
            post(urlEqualTo(connectorPath))
              .withRequestBody(equalToJson(s"""
                                              {
                                              |    "createSubscriptionForMDRRequest": {
                                              |        "requestCommon": {
                                              |            "regime": "MDR",
                                              |            "receiptDate": "$currentDateAndTime",
                                              |            "acknowledgementReference": "$acknowledgementReference",
                                              |            "originatingSystem": "MDTP"
                                              |        },
                                              |        "requestDetail": {
                                              |            "IDType": "NINO",
                                              |            "IDNumber": "AA000000A",
                                              |            "tradingName": "Harold Winter",
                                              |            "isGBUser": true,
                                              |            "primaryContact": {
                                              |                "individual": {
                                              |                    "firstName": "Patrick",
                                              |                    "middleName": "John",
                                              |                    "lastName": "Dyson"
                                              |                },
                                              |                "email": "Patrick.Dyson@example.com",
                                              |                "phone": "747663966",
                                              |                "mobile": "38390756243"
                                              |            },
                                              |            "secondaryContact": {
                                              |                "organisation": {
                                              |                    "organisationName": "Dyson"
                                              |                },
                                              |                "email": "info@example.com",
                                              |                "phone": "847663966",
                                              |                "mobile": "48390756243"
                                              |            }
                                              |        }
                                              |    }
                                              |}
                                              |""".stripMargin))
              .willReturn(
                aResponse()
                  .withHeader("Content-Type", "application/json")
                  .withStatus(OK)
                  .withBody(s"""
                               |{
                               |  "createSubscriptionForMDRResponse" : {
                               |    "responseCommon" : {
                               |      "status" : "OK",
                               |      "processingDate" : "$currentDateAndTime"
                               |    },
                               |    "responseDetail" : {
                               |      "subscriptionID" : "a7405c8d-06ee-46a3-b5a0-5d65176360ec"
                               |    }
                               |  }
                               |}
                               |""".stripMargin)
              )
          )

          val response = wsClient
            .url(fullUrl("/subscriptions"))
            .withHttpHeaders(("Content-Type", "application/json"))
            .post("""
                    |{
                    |    "id": {
                    |        "type": "NINO",
                    |        "value": "AA000000A"
                    |    },
                    |    "name": "Harold Winter",
                    |    "contacts": [
                    |        {
                    |            "type": "I",
                    |            "firstName": "Patrick",
                    |            "middleName": "John",
                    |            "lastName": "Dyson",
                    |            "landline": "747663966",
                    |            "mobile": "38390756243",
                    |            "emailAddress": "Patrick.Dyson@example.com"
                    |        },
                    |        {
                    |            "type": "O",
                    |            "name": "Dyson",
                    |            "landline": "847663966",
                    |            "mobile": "48390756243",
                    |            "emailAddress": "info@example.com"
                    |        }
                    |    ]
                    |}
                    |""".stripMargin)
            .futureValue

          assertAsExpected(
            response = response,
            status = OK,
            jsonBodyOpt = Some(
              """
                |{
                |  "id": "a7405c8d-06ee-46a3-b5a0-5d65176360ec"
                |}
                |""".stripMargin
            )
          )
          verifyThatDownstreamApiWasCalled()
        }
      }
      "valid but the integration call fails with response:" - {
        "internal server error" in {
          stubFor(
            post(urlEqualTo(connectorPath))
              .withRequestBody(equalToJson(s"""
                                              {
                                              |    "createSubscriptionForMDRRequest": {
                                              |        "requestCommon": {
                                              |            "regime": "MDR",
                                              |            "receiptDate": "$currentDateAndTime",
                                              |            "acknowledgementReference": "$acknowledgementReference",
                                              |            "originatingSystem": "MDTP"
                                              |        },
                                              |        "requestDetail": {
                                              |            "IDType": "NINO",
                                              |            "IDNumber": "AA000000A",
                                              |            "tradingName": "Harold Winter",
                                              |            "isGBUser": true,
                                              |            "primaryContact": {
                                              |                "individual": {
                                              |                    "firstName": "Patrick",
                                              |                    "middleName": "John",
                                              |                    "lastName": "Dyson"
                                              |                },
                                              |                "email": "Patrick.Dyson@example.com",
                                              |                "phone": "747663966",
                                              |                "mobile": "38390756243"
                                              |            }
                                              |        }
                                              |    }
                                              |}
                                              |""".stripMargin))
              .willReturn(
                aResponse()
                  .withHeader("Content-Type", "application/json")
                  .withStatus(INTERNAL_SERVER_ERROR)
                  .withBody(s"""
                               |{
                               |  "errorDetail" : {
                               |    "timestamp" : "2023-12-13T11:50:35Z",
                               |    "correlationId" : "eac14118-57cf-44c5-83f9-63f50c5ff712",
                               |    "errorCode" : "500",
                               |    "errorMessage" : "Internal error",
                               |    "source" : "Internal error"
                               |  }
                               |}
                               |""".stripMargin)
              )
          )

          val response = wsClient
            .url(fullUrl("/subscriptions"))
            .withHttpHeaders(("Content-Type", "application/json"))
            .post("""
                    |{
                    |    "id": {
                    |        "type": "NINO",
                    |        "value": "AA000000A"
                    |    },
                    |    "name": "Harold Winter",
                    |    "contacts": [
                    |        {
                    |            "type": "I",
                    |            "firstName": "Patrick",
                    |            "middleName": "John",
                    |            "lastName": "Dyson",
                    |            "landline": "747663966",
                    |            "mobile": "38390756243",
                    |            "emailAddress": "Patrick.Dyson@example.com"
                    |        }
                    |    ]
                    |}
                    |""".stripMargin)
            .futureValue

          assertAsExpected(
            response,
            SERVICE_UNAVAILABLE,
            Some("""
                   |[
                   |  {
                   |    "code": "eis-returned-internal-server-error"
                   |  }
                   |]
                   |""".stripMargin)
          )
          verifyThatDownstreamApiWasCalled()
        }
        "bad request" in {
          stubFor(
            post(urlEqualTo(connectorPath))
              .withRequestBody(equalToJson(s"""
                                              {
                                              |    "createSubscriptionForMDRRequest": {
                                              |        "requestCommon": {
                                              |            "regime": "MDR",
                                              |            "receiptDate": "$currentDateAndTime",
                                              |            "acknowledgementReference": "$acknowledgementReference",
                                              |            "originatingSystem": "MDTP"
                                              |        },
                                              |        "requestDetail": {
                                              |            "IDType": "NINO",
                                              |            "IDNumber": "AA000000A",
                                              |            "tradingName": "Harold Winter",
                                              |            "isGBUser": true,
                                              |            "primaryContact": {
                                              |                "individual": {
                                              |                    "firstName": "Patrick",
                                              |                    "middleName": "John",
                                              |                    "lastName": "Dyson"
                                              |                },
                                              |                "email": "Patrick.Dyson@example.com",
                                              |                "phone": "747663966",
                                              |                "mobile": "38390756243"
                                              |            }
                                              |        }
                                              |    }
                                              |}
                                              |""".stripMargin))
              .willReturn(
                aResponse()
                  .withHeader("Content-Type", "application/json")
                  .withStatus(BAD_REQUEST)
                  .withBody(s"""
                               |{
                               |  "errorDetail" : {
                               |    "timestamp" : "2023-12-13T11:50:35Z",
                               |    "correlationId" : "d102b24f-767c-4620-826b-d068f86e4abc",
                               |    "errorCode" : "400",
                               |    "errorMessage" : "Invalid ID",
                               |    "source" : "Back End",
                               |    "sourceFaultDetail" : {
                               |      "detail" : [ "001 - Regime missing or invalid" ]
                               |    }
                               |  }
                               |}
                               |""".stripMargin)
              )
          )

          val response = wsClient
            .url(fullUrl("/subscriptions"))
            .withHttpHeaders(("Content-Type", "application/json"))
            .post("""
                    |{
                    |    "id": {
                    |        "type": "NINO",
                    |        "value": "AA000000A"
                    |    },
                    |    "name": "Harold Winter",
                    |    "contacts": [
                    |        {
                    |            "type": "I",
                    |            "firstName": "Patrick",
                    |            "middleName": "John",
                    |            "lastName": "Dyson",
                    |            "landline": "747663966",
                    |            "mobile": "38390756243",
                    |            "emailAddress": "Patrick.Dyson@example.com"
                    |        }
                    |    ]
                    |}
                    |""".stripMargin)
            .futureValue

          assertAsExpected(response, INTERNAL_SERVER_ERROR)
          verifyThatDownstreamApiWasCalled()
        }
        "service unavailable" in {
          stubFor(
            post(urlEqualTo(connectorPath))
              .withRequestBody(equalToJson(s"""
                                              {
                                              |    "createSubscriptionForMDRRequest": {
                                              |        "requestCommon": {
                                              |            "regime": "MDR",
                                              |            "receiptDate": "$currentDateAndTime",
                                              |            "acknowledgementReference": "$acknowledgementReference",
                                              |            "originatingSystem": "MDTP"
                                              |        },
                                              |        "requestDetail": {
                                              |            "IDType": "NINO",
                                              |            "IDNumber": "AA000000A",
                                              |            "tradingName": "Harold Winter",
                                              |            "isGBUser": true,
                                              |            "primaryContact": {
                                              |                "individual": {
                                              |                    "firstName": "Patrick",
                                              |                    "middleName": "John",
                                              |                    "lastName": "Dyson"
                                              |                },
                                              |                "email": "Patrick.Dyson@example.com",
                                              |                "phone": "747663966",
                                              |                "mobile": "38390756243"
                                              |            }
                                              |        }
                                              |    }
                                              |}
                                              |""".stripMargin))
              .willReturn(
                aResponse()
                  .withHeader("Content-Type", "application/json")
                  .withStatus(SERVICE_UNAVAILABLE)
                  .withBody(s"""
                               |{
                               |  "errorDetail" : {
                               |    "timestamp" : "2023-12-13T11:50:35Z",
                               |    "correlationId" : "3b560e67-1a0d-47ca-b0c4-6dcbf013203b",
                               |    "errorCode" : "503",
                               |    "errorMessage" : "Request could not be processed",
                               |    "source" : "Back End",
                               |    "sourceFaultDetail" : {
                               |      "detail" : [ "001 - Request could not be processed" ]
                               |    }
                               |  }
                               |}
                               |""".stripMargin)
              )
          )

          val response = wsClient
            .url(fullUrl("/subscriptions"))
            .withHttpHeaders(("Content-Type", "application/json"))
            .post("""
                    |{
                    |    "id": {
                    |        "type": "NINO",
                    |        "value": "AA000000A"
                    |    },
                    |    "name": "Harold Winter",
                    |    "contacts": [
                    |        {
                    |            "type": "I",
                    |            "firstName": "Patrick",
                    |            "middleName": "John",
                    |            "lastName": "Dyson",
                    |            "landline": "747663966",
                    |            "mobile": "38390756243",
                    |            "emailAddress": "Patrick.Dyson@example.com"
                    |        }
                    |    ]
                    |}
                    |""".stripMargin)
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
        "conflict" in {
          stubFor(
            post(urlEqualTo(connectorPath))
              .withRequestBody(equalToJson(s"""
                                              {
                                              |    "createSubscriptionForMDRRequest": {
                                              |        "requestCommon": {
                                              |            "regime": "MDR",
                                              |            "receiptDate": "$currentDateAndTime",
                                              |            "acknowledgementReference": "$acknowledgementReference",
                                              |            "originatingSystem": "MDTP"
                                              |        },
                                              |        "requestDetail": {
                                              |            "IDType": "NINO",
                                              |            "IDNumber": "AA000000A",
                                              |            "tradingName": "Harold Winter",
                                              |            "isGBUser": true,
                                              |            "primaryContact": {
                                              |                "individual": {
                                              |                    "firstName": "Patrick",
                                              |                    "middleName": "John",
                                              |                    "lastName": "Dyson"
                                              |                },
                                              |                "email": "Patrick.Dyson@example.com",
                                              |                "phone": "747663966",
                                              |                "mobile": "38390756243"
                                              |            }
                                              |        }
                                              |    }
                                              |}
                                              |""".stripMargin))
              .willReturn(
                aResponse()
                  .withHeader("Content-Type", "application/json")
                  .withStatus(CONFLICT)
                  .withBody(s"""
                               |{
                               |  "errorDetail" : {
                               |    "timestamp" : "2023-12-13T11:50:35Z",
                               |    "correlationId" : "3b560e67-1a0d-47ca-b0c4-6dcbf013203b",
                               |    "errorCode" : "409",
                               |    "errorMessage" : "Request could not be processed",
                               |    "source" : "Back End",
                               |    "sourceFaultDetail" : {
                               |      "detail" : [ "Duplicate submission" ]
                               |    }
                               |  }
                               |}
                               |""".stripMargin)
              )
          )

          val response = wsClient
            .url(fullUrl("/subscriptions"))
            .withHttpHeaders(("Content-Type", "application/json"))
            .post("""
                    |{
                    |    "id": {
                    |        "type": "NINO",
                    |        "value": "AA000000A"
                    |    },
                    |    "name": "Harold Winter",
                    |    "contacts": [
                    |        {
                    |            "type": "I",
                    |            "firstName": "Patrick",
                    |            "middleName": "John",
                    |            "lastName": "Dyson",
                    |            "landline": "747663966",
                    |            "mobile": "38390756243",
                    |            "emailAddress": "Patrick.Dyson@example.com"
                    |        }
                    |    ]
                    |}
                    |""".stripMargin)
            .futureValue

          assertAsExpected(
            response,
            CONFLICT,
            Some(
              """
                |[
                |  {
                |    "code": "eis-returned-conflict"
                |  }
                |]
                |""".stripMargin
            )
          )
          verifyThatDownstreamApiWasCalled()
        }
        "i'm a teapot" in {
          stubFor(
            post(urlEqualTo(connectorPath))
              .withRequestBody(equalToJson(s"""
                                              {
                                              |    "createSubscriptionForMDRRequest": {
                                              |        "requestCommon": {
                                              |            "regime": "MDR",
                                              |            "receiptDate": "$currentDateAndTime",
                                              |            "acknowledgementReference": "$acknowledgementReference",
                                              |            "originatingSystem": "MDTP"
                                              |        },
                                              |        "requestDetail": {
                                              |            "IDType": "NINO",
                                              |            "IDNumber": "AA000000A",
                                              |            "tradingName": "Harold Winter",
                                              |            "isGBUser": true,
                                              |            "primaryContact": {
                                              |                "individual": {
                                              |                    "firstName": "Patrick",
                                              |                    "middleName": "John",
                                              |                    "lastName": "Dyson"
                                              |                },
                                              |                "email": "Patrick.Dyson@example.com",
                                              |                "phone": "747663966",
                                              |                "mobile": "38390756243"
                                              |            }
                                              |        }
                                              |    }
                                              |}
                                              |""".stripMargin))
              .willReturn(
                aResponse()
                  .withHeader("Content-Type", "application/json")
                  .withStatus(IM_A_TEAPOT)
                  .withBody(s"""
                               |{
                               |  "errorDetail" : {
                               |    "timestamp" : "2023-12-13T11:50:35Z",
                               |    "correlationId" : "3b560e67-1a0d-47ca-b0c4-6dcbf013203b",
                               |    "errorCode" : "418",
                               |    "errorMessage" : "I'm a little teapot",
                               |    "source" : "Back End",
                               |    "sourceFaultDetail" : {
                               |      "detail" : [ "Earl Grey" ]
                               |    }
                               |  }
                               |}
                               |""".stripMargin)
              )
          )

          val response = wsClient
            .url(fullUrl("/subscriptions"))
            .withHttpHeaders(("Content-Type", "application/json"))
            .post("""
                    |{
                    |    "id": {
                    |        "type": "NINO",
                    |        "value": "AA000000A"
                    |    },
                    |    "name": "Harold Winter",
                    |    "contacts": [
                    |        {
                    |            "type": "I",
                    |            "firstName": "Patrick",
                    |            "middleName": "John",
                    |            "lastName": "Dyson",
                    |            "landline": "747663966",
                    |            "mobile": "38390756243",
                    |            "emailAddress": "Patrick.Dyson@example.com"
                    |        }
                    |    ]
                    |}
                    |""".stripMargin)
            .futureValue

          assertAsExpected(response, INTERNAL_SERVER_ERROR)
          verifyThatDownstreamApiWasCalled()
        }
      }
      "invalid, specifically:" - {
        "the id type is" - {
          "absent" in {
            val response = wsClient
              .url(fullUrl("/subscriptions"))
              .withHttpHeaders(("Content-Type", "application/json"))
              .post("""
                      |{
                      |    "id": {
                      |        "value": "AA000000A"
                      |    },
                      |    "name": "Harold Winter",
                      |    "contacts": [
                      |        {
                      |            "type": "I",
                      |            "firstName": "Patrick",
                      |            "middleName": "John",
                      |            "lastName": "Dyson",
                      |            "landline": "747663966",
                      |            "mobile": "38390756243",
                      |            "emailAddress": "Patrick.Dyson@example.com"
                      |        }
                      |    ]
                      |}
                      |""".stripMargin)
              .futureValue

            assertAsExpected(
              response,
              BAD_REQUEST,
              Some("""
                     |[
                     |  {
                     |    "code": "invalid-id-type"
                     |  }
                     |]
                     |""".stripMargin)
            )
            verifyThatDownstreamApiWasNotCalled()
          }
          "blank" in {
            val response = wsClient
              .url(fullUrl("/subscriptions"))
              .withHttpHeaders(("Content-Type", "application/json"))
              .post("""
                      |{
                      |    "id": {
                      |        "type": "",
                      |        "value": "AA000000A"
                      |    },
                      |    "name": "Harold Winter",
                      |    "contacts": [
                      |        {
                      |            "type": "I",
                      |            "firstName": "Patrick",
                      |            "middleName": "John",
                      |            "lastName": "Dyson",
                      |            "landline": "747663966",
                      |            "mobile": "38390756243",
                      |            "emailAddress": "Patrick.Dyson@example.com"
                      |        }
                      |    ]
                      |}
                      |""".stripMargin)
              .futureValue

            assertAsExpected(
              response,
              BAD_REQUEST,
              Some("""
                     |[
                     |  {
                     |    "code": "invalid-id-type"
                     |  }
                     |]
                     |""".stripMargin)
            )
            verifyThatDownstreamApiWasNotCalled()
          }
          "unrecognised" in {
            val response = wsClient
              .url(fullUrl("/subscriptions"))
              .withHttpHeaders(("Content-Type", "application/json"))
              .post("""
                      |{
                      |    "id": {
                      |        "type": "VAT",
                      |        "value": "AA000000A"
                      |    },
                      |    "name": "Harold Winter",
                      |    "contacts": [
                      |        {
                      |            "type": "I",
                      |            "firstName": "Patrick",
                      |            "middleName": "John",
                      |            "lastName": "Dyson",
                      |            "landline": "747663966",
                      |            "mobile": "38390756243",
                      |            "emailAddress": "Patrick.Dyson@example.com"
                      |        }
                      |    ]
                      |}
                      |""".stripMargin)
              .futureValue

            assertAsExpected(
              response,
              BAD_REQUEST,
              Some("""
                     |[
                     |  {
                     |    "code": "invalid-id-type"
                     |  }
                     |]
                     |""".stripMargin)
            )
            verifyThatDownstreamApiWasNotCalled()
          }
        }
        "the id value is" - {
          "absent" in {
            val response = wsClient
              .url(fullUrl("/subscriptions"))
              .withHttpHeaders(("Content-Type", "application/json"))
              .post("""
                      |{
                      |    "id": {
                      |        "type": "NINO"
                      |    },
                      |    "name": "Harold Winter",
                      |    "contacts": [
                      |        {
                      |            "type": "I",
                      |            "firstName": "Patrick",
                      |            "middleName": "John",
                      |            "lastName": "Dyson",
                      |            "landline": "747663966",
                      |            "mobile": "38390756243",
                      |            "emailAddress": "Patrick.Dyson@example.com"
                      |        }
                      |    ]
                      |}
                      |""".stripMargin)
              .futureValue

            assertAsExpected(
              response,
              BAD_REQUEST,
              Some("""
                     |[
                     |  {
                     |    "code": "invalid-id-value"
                     |  }
                     |]
                     |""".stripMargin)
            )
            verifyThatDownstreamApiWasNotCalled()
          }
          "blank" in {
            val response = wsClient
              .url(fullUrl("/subscriptions"))
              .withHttpHeaders(("Content-Type", "application/json"))
              .post("""
                      |{
                      |    "id": {
                      |        "type": "NINO",
                      |        "value": ""
                      |    },
                      |    "name": "Harold Winter",
                      |    "contacts": [
                      |        {
                      |            "type": "I",
                      |            "firstName": "Patrick",
                      |            "middleName": "John",
                      |            "lastName": "Dyson",
                      |            "landline": "747663966",
                      |            "mobile": "38390756243",
                      |            "emailAddress": "Patrick.Dyson@example.com"
                      |        }
                      |    ]
                      |}
                      |""".stripMargin)
              .futureValue

            assertAsExpected(
              response,
              BAD_REQUEST,
              Some("""
                     |[
                     |  {
                     |    "code": "invalid-id-value"
                     |  }
                     |]
                     |""".stripMargin)
            )
            verifyThatDownstreamApiWasNotCalled()
          }
          "too long" in {
            val response = wsClient
              .url(fullUrl("/subscriptions"))
              .withHttpHeaders(("Content-Type", "application/json"))
              .post("""
                      |{
                      |    "id": {
                      |        "type": "NINO",
                      |        "value": "AA000000AAA00000"
                      |    },
                      |    "name": "Harold Winter",
                      |    "contacts": [
                      |        {
                      |            "type": "I",
                      |            "firstName": "Patrick",
                      |            "middleName": "John",
                      |            "lastName": "Dyson",
                      |            "landline": "747663966",
                      |            "mobile": "38390756243",
                      |            "emailAddress": "Patrick.Dyson@example.com"
                      |        }
                      |    ]
                      |}
                      |""".stripMargin)
              .futureValue

            assertAsExpected(
              response,
              BAD_REQUEST,
              Some("""
                     |[
                     |  {
                     |    "code": "invalid-id-value"
                     |  }
                     |]
                     |""".stripMargin)
            )
            verifyThatDownstreamApiWasNotCalled()
          }
        }
        "the name is" - {
          "blank" in {
            val response = wsClient
              .url(fullUrl("/subscriptions"))
              .withHttpHeaders(("Content-Type", "application/json"))
              .post("""
                      |{
                      |    "id": {
                      |        "type": "NINO",
                      |        "value": "AA000000"
                      |    },
                      |    "name": "",
                      |    "contacts": [
                      |        {
                      |            "type": "I",
                      |            "firstName": "Patrick",
                      |            "middleName": "John",
                      |            "lastName": "Dyson",
                      |            "landline": "747663966",
                      |            "mobile": "38390756243",
                      |            "emailAddress": "Patrick.Dyson@example.com"
                      |        }
                      |    ]
                      |}
                      |""".stripMargin)
              .futureValue

            assertAsExpected(
              response,
              BAD_REQUEST,
              Some("""
                     |[
                     |  {
                     |    "code": "invalid-name"
                     |  }
                     |]
                     |""".stripMargin)
            )
            verifyThatDownstreamApiWasNotCalled()
          }
          "too long" in {
            val response = wsClient
              .url(fullUrl("/subscriptions"))
              .withHttpHeaders(("Content-Type", "application/json"))
              .post("""
                      |{
                      |    "id": {
                      |        "type": "NINO",
                      |        "value": "AA000000"
                      |    },
                      |    "name": "Harold Winter, III, Earl Of East Mountain & North River, Duke Of South Wales, Phd",
                      |    "contacts": [
                      |        {
                      |            "type": "I",
                      |            "firstName": "Patrick",
                      |            "middleName": "John",
                      |            "lastName": "Dyson",
                      |            "landline": "747663966",
                      |            "mobile": "38390756243",
                      |            "emailAddress": "Patrick.Dyson@example.com"
                      |        }
                      |    ]
                      |}
                      |""".stripMargin)
              .futureValue

            assertAsExpected(
              response,
              BAD_REQUEST,
              Some("""
                     |[
                     |  {
                     |    "code": "invalid-name"
                     |  }
                     |]
                     |""".stripMargin)
            )
            verifyThatDownstreamApiWasNotCalled()
          }
        }
        "it contains no contacts" in {
          val response = wsClient
            .url(fullUrl("/subscriptions"))
            .withHttpHeaders(("Content-Type", "application/json"))
            .post("""
                    |{
                    |    "id": {
                    |        "type": "NINO",
                    |        "value": "AA000000"
                    |    },
                    |    "name": "Harold Winter",
                    |    "contacts": [
                    |    ]
                    |}
                    |""".stripMargin)
            .futureValue

          assertAsExpected(
            response,
            BAD_REQUEST,
            Some("""
                   |[
                   |  {
                   |    "code": "invalid-number-of-contacts"
                   |  }
                   |]
                   |""".stripMargin)
          )
          verifyThatDownstreamApiWasNotCalled()
        }
        "it contains three contacts" in {
          val response = wsClient
            .url(fullUrl("/subscriptions"))
            .withHttpHeaders(("Content-Type", "application/json"))
            .post("""
                    |{
                    |    "id": {
                    |        "type": "NINO",
                    |        "value": "AA000000A"
                    |    },
                    |    "name": "Harold Winter",
                    |    "contacts": [
                    |        {
                    |            "type": "I",
                    |            "firstName": "Patrick",
                    |            "middleName": "John",
                    |            "lastName": "Dyson",
                    |            "landline": "747663966",
                    |            "mobile": "38390756243",
                    |            "emailAddress": "Patrick.Dyson@example.com"
                    |        },
                    |        {
                    |            "type": "O",
                    |            "name": "Dyson",
                    |            "landline": "847663966",
                    |            "mobile": "48390756243",
                    |            "emailAddress": "info@example.com"
                    |        },
                    |        {
                    |            "type": "I",
                    |            "firstName": "Patricia",
                    |            "middleName": "Jane",
                    |            "lastName": "Dyson",
                    |            "landline": "747663967",
                    |            "mobile": "38390756244",
                    |            "emailAddress": "Patricia.Dyson@example.com"
                    |        }
                    |    ]
                    |}
                    |""".stripMargin)
            .futureValue

          assertAsExpected(
            response,
            BAD_REQUEST,
            Some("""
                   |[
                   |  {
                   |    "code": "invalid-number-of-contacts"
                   |  }
                   |]
                   |""".stripMargin)
          )
          verifyThatDownstreamApiWasNotCalled()
        }
        "it contains two contacts, one has a type which is" - {
          "an individual, where" - {
            "the first name is" - {
              "absent" in {
                val response = wsClient
                  .url(fullUrl("/subscriptions"))
                  .withHttpHeaders(("Content-Type", "application/json"))
                  .post("""
                          |{
                          |    "id": {
                          |        "type": "NINO",
                          |        "value": "AA000000A"
                          |    },
                          |    "name": "Harold Winter",
                          |    "contacts": [
                          |        {
                          |            "type": "I",
                          |            "middleName": "John",
                          |            "lastName": "Dyson",
                          |            "landline": "747663966",
                          |            "mobile": "38390756243",
                          |            "emailAddress": "Patrick.Dyson@example.com"
                          |        },
                          |        {
                          |            "type": "O",
                          |            "name": "Dyson",
                          |            "landline": "847663966",
                          |            "mobile": "48390756243",
                          |            "emailAddress": "info@example.com"
                          |        }
                          |    ]
                          |}
                          |""".stripMargin)
                  .futureValue

                assertAsExpected(
                  response,
                  BAD_REQUEST,
                  Some("""
                         |[
                         |  {
                         |    "code": "invalid-contact-1-first-name"
                         |  }
                         |]
                         |""".stripMargin)
                )
                verifyThatDownstreamApiWasNotCalled()
              }
              "blank" in {
                val response = wsClient
                  .url(fullUrl("/subscriptions"))
                  .withHttpHeaders(("Content-Type", "application/json"))
                  .post("""
                          |{
                          |    "id": {
                          |        "type": "NINO",
                          |        "value": "AA000000A"
                          |    },
                          |    "name": "Harold Winter",
                          |    "contacts": [
                          |        {
                          |            "type": "I",
                          |            "firstName": "",
                          |            "middleName": "John",
                          |            "lastName": "Dyson",
                          |            "landline": "747663966",
                          |            "mobile": "38390756243",
                          |            "emailAddress": "Patrick.Dyson@example.com"
                          |        },
                          |        {
                          |            "type": "O",
                          |            "name": "Dyson",
                          |            "landline": "847663966",
                          |            "mobile": "48390756243",
                          |            "emailAddress": "info@example.com"
                          |        }
                          |    ]
                          |}
                          |""".stripMargin)
                  .futureValue

                assertAsExpected(
                  response,
                  BAD_REQUEST,
                  Some("""
                         |[
                         |  {
                         |    "code": "invalid-contact-1-first-name"
                         |  }
                         |]
                         |""".stripMargin)
                )
                verifyThatDownstreamApiWasNotCalled()
              }
              "too long" in {
                val response = wsClient
                  .url(fullUrl("/subscriptions"))
                  .withHttpHeaders(("Content-Type", "application/json"))
                  .post("""
                          |{
                          |    "id": {
                          |        "type": "NINO",
                          |        "value": "AA000000A"
                          |    },
                          |    "name": "Harold Winter",
                          |    "contacts": [
                          |        {
                          |            "type": "I",
                          |            "firstName": "Patrick Alexander John Fitzpatrick James",
                          |            "middleName": "John",
                          |            "lastName": "Dyson",
                          |            "landline": "747663966",
                          |            "mobile": "38390756243",
                          |            "emailAddress": "Patrick.Dyson@example.com"
                          |        },
                          |        {
                          |            "type": "O",
                          |            "name": "Dyson",
                          |            "landline": "847663966",
                          |            "mobile": "48390756243",
                          |            "emailAddress": "info@example.com"
                          |        }
                          |    ]
                          |}
                          |""".stripMargin)
                  .futureValue

                assertAsExpected(
                  response,
                  BAD_REQUEST,
                  Some("""
                         |[
                         |  {
                         |    "code": "invalid-contact-1-first-name"
                         |  }
                         |]
                         |""".stripMargin)
                )
                verifyThatDownstreamApiWasNotCalled()
              }
            }
            "the middle name is" - {
              "blank" in {
                val response = wsClient
                  .url(fullUrl("/subscriptions"))
                  .withHttpHeaders(("Content-Type", "application/json"))
                  .post("""
                          |{
                          |    "id": {
                          |        "type": "NINO",
                          |        "value": "AA000000A"
                          |    },
                          |    "name": "Harold Winter",
                          |    "contacts": [
                          |        {
                          |            "type": "I",
                          |            "firstName": "Patrick",
                          |            "middleName": "",
                          |            "lastName": "Dyson",
                          |            "landline": "747663966",
                          |            "mobile": "38390756243",
                          |            "emailAddress": "Patrick.Dyson@example.com"
                          |        },
                          |        {
                          |            "type": "O",
                          |            "name": "Dyson",
                          |            "landline": "847663966",
                          |            "mobile": "48390756243",
                          |            "emailAddress": "info@example.com"
                          |        }
                          |    ]
                          |}
                          |""".stripMargin)
                  .futureValue

                assertAsExpected(
                  response,
                  BAD_REQUEST,
                  Some("""
                         |[
                         |  {
                         |    "code": "invalid-contact-1-middle-name"
                         |  }
                         |]
                         |""".stripMargin)
                )
                verifyThatDownstreamApiWasNotCalled()
              }
              "too long" in {
                val response = wsClient
                  .url(fullUrl("/subscriptions"))
                  .withHttpHeaders(("Content-Type", "application/json"))
                  .post("""
                          |{
                          |    "id": {
                          |        "type": "NINO",
                          |        "value": "AA000000A"
                          |    },
                          |    "name": "Harold Winter",
                          |    "contacts": [
                          |        {
                          |            "type": "I",
                          |            "firstName": "Patrick",
                          |            "middleName": "Alexander John Fitzpatrick James Edward",
                          |            "lastName": "Dyson",
                          |            "landline": "747663966",
                          |            "mobile": "38390756243",
                          |            "emailAddress": "Patrick.Dyson@example.com"
                          |        },
                          |        {
                          |            "type": "O",
                          |            "name": "Dyson",
                          |            "landline": "847663966",
                          |            "mobile": "48390756243",
                          |            "emailAddress": "info@example.com"
                          |        }
                          |    ]
                          |}
                          |""".stripMargin)
                  .futureValue

                assertAsExpected(
                  response,
                  BAD_REQUEST,
                  Some("""
                         |[
                         |  {
                         |    "code": "invalid-contact-1-middle-name"
                         |  }
                         |]
                         |""".stripMargin)
                )
                verifyThatDownstreamApiWasNotCalled()
              }
            }
            "the last name is" - {
              "absent" in {
                val response = wsClient
                  .url(fullUrl("/subscriptions"))
                  .withHttpHeaders(("Content-Type", "application/json"))
                  .post("""
                          |{
                          |    "id": {
                          |        "type": "NINO",
                          |        "value": "AA000000A"
                          |    },
                          |    "name": "Harold Winter",
                          |    "contacts": [
                          |        {
                          |            "type": "I",
                          |            "firstName": "Patrick",
                          |            "middleName": "John",
                          |            "landline": "747663966",
                          |            "mobile": "38390756243",
                          |            "emailAddress": "Patrick.Dyson@example.com"
                          |        },
                          |        {
                          |            "type": "O",
                          |            "name": "Dyson",
                          |            "landline": "847663966",
                          |            "mobile": "48390756243",
                          |            "emailAddress": "info@example.com"
                          |        }
                          |    ]
                          |}
                          |""".stripMargin)
                  .futureValue

                assertAsExpected(
                  response,
                  BAD_REQUEST,
                  Some("""
                         |[
                         |  {
                         |    "code": "invalid-contact-1-last-name"
                         |  }
                         |]
                         |""".stripMargin)
                )
                verifyThatDownstreamApiWasNotCalled()
              }
              "blank" in {
                val response = wsClient
                  .url(fullUrl("/subscriptions"))
                  .withHttpHeaders(("Content-Type", "application/json"))
                  .post("""
                          |{
                          |    "id": {
                          |        "type": "NINO",
                          |        "value": "AA000000A"
                          |    },
                          |    "name": "Harold Winter",
                          |    "contacts": [
                          |        {
                          |            "type": "I",
                          |            "firstName": "Patrick",
                          |            "middleName": "John",
                          |            "lastName": "",
                          |            "landline": "747663966",
                          |            "mobile": "38390756243",
                          |            "emailAddress": "Patrick.Dyson@example.com"
                          |        },
                          |        {
                          |            "type": "O",
                          |            "name": "Dyson",
                          |            "landline": "847663966",
                          |            "mobile": "48390756243",
                          |            "emailAddress": "info@example.com"
                          |        }
                          |    ]
                          |}
                          |""".stripMargin)
                  .futureValue

                assertAsExpected(
                  response,
                  BAD_REQUEST,
                  Some("""
                         |[
                         |  {
                         |    "code": "invalid-contact-1-last-name"
                         |  }
                         |]
                         |""".stripMargin)
                )
                verifyThatDownstreamApiWasNotCalled()
              }
              "too long" in {
                val response = wsClient
                  .url(fullUrl("/subscriptions"))
                  .withHttpHeaders(("Content-Type", "application/json"))
                  .post("""
                          |{
                          |    "id": {
                          |        "type": "NINO",
                          |        "value": "AA000000A"
                          |    },
                          |    "name": "Harold Winter",
                          |    "contacts": [
                          |        {
                          |            "type": "I",
                          |            "firstName": "Patrick",
                          |            "middleName": "John",
                          |            "lastName": "Alexander III, Earl Of Somewhere Else",
                          |            "landline": "747663966",
                          |            "mobile": "38390756243",
                          |            "emailAddress": "Patrick.Dyson@example.com"
                          |        },
                          |        {
                          |            "type": "O",
                          |            "name": "Dyson",
                          |            "landline": "847663966",
                          |            "mobile": "48390756243",
                          |            "emailAddress": "info@example.com"
                          |        }
                          |    ]
                          |}
                          |""".stripMargin)
                  .futureValue

                assertAsExpected(
                  response,
                  BAD_REQUEST,
                  Some("""
                         |[
                         |  {
                         |    "code": "invalid-contact-1-last-name"
                         |  }
                         |]
                         |""".stripMargin)
                )
                verifyThatDownstreamApiWasNotCalled()
              }
            }
            "the landline number is" - {
              "blank" in {
                val response = wsClient
                  .url(fullUrl("/subscriptions"))
                  .withHttpHeaders(("Content-Type", "application/json"))
                  .post("""
                          |{
                          |    "id": {
                          |        "type": "NINO",
                          |        "value": "AA000000A"
                          |    },
                          |    "name": "Harold Winter",
                          |    "contacts": [
                          |        {
                          |            "type": "I",
                          |            "firstName": "Patrick",
                          |            "middleName": "John",
                          |            "lastName": "Dyson",
                          |            "landline": "",
                          |            "mobile": "38390756243",
                          |            "emailAddress": "Patrick.Dyson@example.com"
                          |        },
                          |        {
                          |            "type": "O",
                          |            "name": "Dyson",
                          |            "landline": "847663966",
                          |            "mobile": "48390756243",
                          |            "emailAddress": "info@example.com"
                          |        }
                          |    ]
                          |}
                          |""".stripMargin)
                  .futureValue

                assertAsExpected(
                  response,
                  BAD_REQUEST,
                  Some("""
                         |[
                         |  {
                         |    "code": "invalid-contact-1-landline"
                         |  }
                         |]
                         |""".stripMargin)
                )
                verifyThatDownstreamApiWasNotCalled()
              }
              "too long" in {
                val response = wsClient
                  .url(fullUrl("/subscriptions"))
                  .withHttpHeaders(("Content-Type", "application/json"))
                  .post("""
                          |{
                          |    "id": {
                          |        "type": "NINO",
                          |        "value": "AA000000A"
                          |    },
                          |    "name": "Harold Winter",
                          |    "contacts": [
                          |        {
                          |            "type": "I",
                          |            "firstName": "Patrick",
                          |            "middleName": "John",
                          |            "lastName": "Dyson",
                          |            "landline": "747663966747663966747663966",
                          |            "mobile": "38390756243",
                          |            "emailAddress": "Patrick.Dyson@example.com"
                          |        },
                          |        {
                          |            "type": "O",
                          |            "name": "Dyson",
                          |            "landline": "847663966",
                          |            "mobile": "48390756243",
                          |            "emailAddress": "info@example.com"
                          |        }
                          |    ]
                          |}
                          |""".stripMargin)
                  .futureValue

                assertAsExpected(
                  response,
                  BAD_REQUEST,
                  Some("""
                         |[
                         |  {
                         |    "code": "invalid-contact-1-landline"
                         |  }
                         |]
                         |""".stripMargin)
                )
                verifyThatDownstreamApiWasNotCalled()
              }
              "of an invalid format" in {
                val response = wsClient
                  .url(fullUrl("/subscriptions"))
                  .withHttpHeaders(("Content-Type", "application/json"))
                  .post("""
                          |{
                          |    "id": {
                          |        "type": "NINO",
                          |        "value": "AA000000A"
                          |    },
                          |    "name": "Harold Winter",
                          |    "contacts": [
                          |        {
                          |            "type": "I",
                          |            "firstName": "Patrick",
                          |            "middleName": "John",
                          |            "lastName": "Dyson",
                          |            "landline": "£747663966",
                          |            "mobile": "38390756243",
                          |            "emailAddress": "Patrick.Dyson@example.com"
                          |        },
                          |        {
                          |            "type": "O",
                          |            "name": "Dyson",
                          |            "landline": "847663966",
                          |            "mobile": "48390756243",
                          |            "emailAddress": "info@example.com"
                          |        }
                          |    ]
                          |}
                          |""".stripMargin)
                  .futureValue

                assertAsExpected(
                  response,
                  BAD_REQUEST,
                  Some("""
                         |[
                         |  {
                         |    "code": "invalid-contact-1-landline"
                         |  }
                         |]
                         |""".stripMargin)
                )
                verifyThatDownstreamApiWasNotCalled()
              }
            }
            "the mobile number is" - {
              "blank" in {
                val response = wsClient
                  .url(fullUrl("/subscriptions"))
                  .withHttpHeaders(("Content-Type", "application/json"))
                  .post("""
                          |{
                          |    "id": {
                          |        "type": "NINO",
                          |        "value": "AA000000A"
                          |    },
                          |    "name": "Harold Winter",
                          |    "contacts": [
                          |        {
                          |            "type": "I",
                          |            "firstName": "Patrick",
                          |            "middleName": "John",
                          |            "lastName": "Dyson",
                          |            "landline": "747663966",
                          |            "mobile": "",
                          |            "emailAddress": "Patrick.Dyson@example.com"
                          |        },
                          |        {
                          |            "type": "O",
                          |            "name": "Dyson",
                          |            "landline": "847663966",
                          |            "mobile": "48390756243",
                          |            "emailAddress": "info@example.com"
                          |        }
                          |    ]
                          |}
                          |""".stripMargin)
                  .futureValue

                assertAsExpected(
                  response,
                  BAD_REQUEST,
                  Some("""
                         |[
                         |  {
                         |    "code": "invalid-contact-1-mobile"
                         |  }
                         |]
                         |""".stripMargin)
                )
                verifyThatDownstreamApiWasNotCalled()
              }
              "too long" in {
                val response = wsClient
                  .url(fullUrl("/subscriptions"))
                  .withHttpHeaders(("Content-Type", "application/json"))
                  .post("""
                          |{
                          |    "id": {
                          |        "type": "NINO",
                          |        "value": "AA000000A"
                          |    },
                          |    "name": "Harold Winter",
                          |    "contacts": [
                          |        {
                          |            "type": "I",
                          |            "firstName": "Patrick",
                          |            "middleName": "John",
                          |            "lastName": "Dyson",
                          |            "landline": "747663966",
                          |            "mobile": "38390756243383907562433839",
                          |            "emailAddress": "Patrick.Dyson@example.com"
                          |        },
                          |        {
                          |            "type": "O",
                          |            "name": "Dyson",
                          |            "landline": "847663966",
                          |            "mobile": "48390756243",
                          |            "emailAddress": "info@example.com"
                          |        }
                          |    ]
                          |}
                          |""".stripMargin)
                  .futureValue

                assertAsExpected(
                  response,
                  BAD_REQUEST,
                  Some("""
                         |[
                         |  {
                         |    "code": "invalid-contact-1-mobile"
                         |  }
                         |]
                         |""".stripMargin)
                )
                verifyThatDownstreamApiWasNotCalled()
              }
              "of an invalid format" in {
                val response = wsClient
                  .url(fullUrl("/subscriptions"))
                  .withHttpHeaders(("Content-Type", "application/json"))
                  .post("""
                          |{
                          |    "id": {
                          |        "type": "NINO",
                          |        "value": "AA000000A"
                          |    },
                          |    "name": "Harold Winter",
                          |    "contacts": [
                          |        {
                          |            "type": "I",
                          |            "firstName": "Patrick",
                          |            "middleName": "John",
                          |            "lastName": "Dyson",
                          |            "landline": "747663966",
                          |            "mobile": "£48390756243",
                          |            "emailAddress": "Patrick.Dyson@example.com"
                          |        },
                          |        {
                          |            "type": "O",
                          |            "name": "Dyson",
                          |            "landline": "847663966",
                          |            "mobile": "38390756243",
                          |            "emailAddress": "info@example.com"
                          |        }
                          |    ]
                          |}
                          |""".stripMargin)
                  .futureValue

                assertAsExpected(
                  response,
                  BAD_REQUEST,
                  Some("""
                         |[
                         |  {
                         |    "code": "invalid-contact-1-mobile"
                         |  }
                         |]
                         |""".stripMargin)
                )
                verifyThatDownstreamApiWasNotCalled()
              }
            }
            "the email address is" - {
              "absent" in {
                val response = wsClient
                  .url(fullUrl("/subscriptions"))
                  .withHttpHeaders(("Content-Type", "application/json"))
                  .post("""
                          |{
                          |    "id": {
                          |        "type": "NINO",
                          |        "value": "AA000000A"
                          |    },
                          |    "name": "Harold Winter",
                          |    "contacts": [
                          |        {
                          |            "type": "I",
                          |            "firstName": "Patrick",
                          |            "middleName": "John",
                          |            "lastName": "Dyson",
                          |            "landline": "747663966",
                          |            "mobile": "38390756243"
                          |        },
                          |        {
                          |            "type": "O",
                          |            "name": "Dyson",
                          |            "landline": "847663966",
                          |            "mobile": "38390756243",
                          |            "emailAddress": "info@example.com"
                          |        }
                          |    ]
                          |}
                          |""".stripMargin)
                  .futureValue

                assertAsExpected(
                  response,
                  BAD_REQUEST,
                  Some("""
                         |[
                         |  {
                         |    "code": "invalid-contact-1-email-address"
                         |  }
                         |]
                         |""".stripMargin)
                )
                verifyThatDownstreamApiWasNotCalled()
              }
              "blank" in {
                val response = wsClient
                  .url(fullUrl("/subscriptions"))
                  .withHttpHeaders(("Content-Type", "application/json"))
                  .post("""
                          |{
                          |    "id": {
                          |        "type": "NINO",
                          |        "value": "AA000000A"
                          |    },
                          |    "name": "Harold Winter",
                          |    "contacts": [
                          |        {
                          |            "type": "I",
                          |            "firstName": "Patrick",
                          |            "middleName": "John",
                          |            "lastName": "Dyson",
                          |            "landline": "747663966",
                          |            "mobile": "38390756243",
                          |            "emailAddress": ""
                          |        },
                          |        {
                          |            "type": "O",
                          |            "name": "Dyson",
                          |            "landline": "847663966",
                          |            "mobile": "38390756243",
                          |            "emailAddress": "info@example.com"
                          |        }
                          |    ]
                          |}
                          |""".stripMargin)
                  .futureValue

                assertAsExpected(
                  response,
                  BAD_REQUEST,
                  Some("""
                         |[
                         |  {
                         |    "code": "invalid-contact-1-email-address"
                         |  }
                         |]
                         |""".stripMargin)
                )
                verifyThatDownstreamApiWasNotCalled()
              }
              "too long" in {
                val response = wsClient
                  .url(fullUrl("/subscriptions"))
                  .withHttpHeaders(("Content-Type", "application/json"))
                  .post("""
                          |{
                          |    "id": {
                          |        "type": "NINO",
                          |        "value": "AA000000A"
                          |    },
                          |    "name": "Harold Winter",
                          |    "contacts": [
                          |        {
                          |            "type": "I",
                          |            "firstName": "Patrick",
                          |            "middleName": "John",
                          |            "lastName": "Dyson",
                          |            "landline": "747663966",
                          |            "mobile": "38390756243",
                          |            "emailAddress": "loremipsumdolorsitametconsetetursadipscingelitrseddiamnonumyeirmodtemporinviduntutlaboreetdoloremagnLoremipsumdolorsisum@example.com"
                          |        },
                          |        {
                          |            "type": "O",
                          |            "name": "Dyson",
                          |            "landline": "847663966",
                          |            "mobile": "38390756243",
                          |            "emailAddress": "info@example.com"
                          |        }
                          |    ]
                          |}
                          |""".stripMargin)
                  .futureValue

                assertAsExpected(
                  response,
                  BAD_REQUEST,
                  Some("""
                         |[
                         |  {
                         |    "code": "invalid-contact-1-email-address"
                         |  }
                         |]
                         |""".stripMargin)
                )
                verifyThatDownstreamApiWasNotCalled()
              }
              "of an invalid format" in {
                val response = wsClient
                  .url(fullUrl("/subscriptions"))
                  .withHttpHeaders(("Content-Type", "application/json"))
                  .post("""
                          |{
                          |    "id": {
                          |        "type": "NINO",
                          |        "value": "AA000000A"
                          |    },
                          |    "name": "Harold Winter",
                          |    "contacts": [
                          |        {
                          |            "type": "I",
                          |            "firstName": "Patrick",
                          |            "middleName": "John",
                          |            "lastName": "Dyson",
                          |            "landline": "747663966",
                          |            "mobile": "38390756243",
                          |            "emailAddress": "@example.com"
                          |        },
                          |        {
                          |            "type": "O",
                          |            "name": "Dyson",
                          |            "landline": "847663966",
                          |            "mobile": "38390756243",
                          |            "emailAddress": "info@example.com"
                          |        }
                          |    ]
                          |}
                          |""".stripMargin)
                  .futureValue

                assertAsExpected(
                  response,
                  BAD_REQUEST,
                  Some("""
                         |[
                         |  {
                         |    "code": "invalid-contact-1-email-address"
                         |  }
                         |]
                         |""".stripMargin)
                )
                verifyThatDownstreamApiWasNotCalled()
              }
            }
          }
          "an organisation, where" - {
            "the name is" - {
              "absent" in {
                val response = wsClient
                  .url(fullUrl("/subscriptions"))
                  .withHttpHeaders(("Content-Type", "application/json"))
                  .post("""
                          |{
                          |    "id": {
                          |        "type": "NINO",
                          |        "value": "AA000000A"
                          |    },
                          |    "name": "Harold Winter",
                          |    "contacts": [
                          |        {
                          |            "type": "I",
                          |            "firstName": "Patrick",
                          |            "middleName": "John",
                          |            "lastName": "Dyson",
                          |            "landline": "747663966",
                          |            "mobile": "38390756243",
                          |            "emailAddress": "Patrick.Dyson@example.com"
                          |        },
                          |        {
                          |            "type": "O",
                          |            "landline": "847663966",
                          |            "mobile": "48390756243",
                          |            "emailAddress": "info@example.com"
                          |        }
                          |    ]
                          |}
                          |""".stripMargin)
                  .futureValue

                assertAsExpected(
                  response,
                  BAD_REQUEST,
                  Some("""
                         |[
                         |  {
                         |    "code": "invalid-contact-2-name"
                         |  }
                         |]
                         |""".stripMargin)
                )
                verifyThatDownstreamApiWasNotCalled()
              }
              "blank" in {
                val response = wsClient
                  .url(fullUrl("/subscriptions"))
                  .withHttpHeaders(("Content-Type", "application/json"))
                  .post("""
                          |{
                          |    "id": {
                          |        "type": "NINO",
                          |        "value": "AA000000A"
                          |    },
                          |    "name": "Harold Winter",
                          |    "contacts": [
                          |        {
                          |            "type": "I",
                          |            "firstName": "Patrick",
                          |            "middleName": "John",
                          |            "lastName": "Dyson",
                          |            "landline": "747663966",
                          |            "mobile": "38390756243",
                          |            "emailAddress": "Patrick.Dyson@example.com"
                          |        },
                          |        {
                          |            "type": "O",
                          |            "name": "",
                          |            "landline": "847663966",
                          |            "mobile": "48390756243",
                          |            "emailAddress": "info@example.com"
                          |        }
                          |    ]
                          |}
                          |""".stripMargin)
                  .futureValue

                assertAsExpected(
                  response,
                  BAD_REQUEST,
                  Some("""
                         |[
                         |  {
                         |    "code": "invalid-contact-2-name"
                         |  }
                         |]
                         |""".stripMargin)
                )
                verifyThatDownstreamApiWasNotCalled()
              }
              "too long" in {
                val response = wsClient
                  .url(fullUrl("/subscriptions"))
                  .withHttpHeaders(("Content-Type", "application/json"))
                  .post("""
                          |{
                          |    "id": {
                          |        "type": "NINO",
                          |        "value": "AA000000A"
                          |    },
                          |    "name": "Harold Winter",
                          |    "contacts": [
                          |        {
                          |            "type": "I",
                          |            "firstName": "Patrick",
                          |            "middleName": "John",
                          |            "lastName": "Dyson",
                          |            "landline": "747663966",
                          |            "mobile": "38390756243",
                          |            "emailAddress": "Patrick.Dyson@example.com"
                          |        },
                          |        {
                          |            "type": "O",
                          |            "name": "The Dyson Electronics Company Of Great Britain And Northern Ireland (aka The Dyson Electronics Company Of Great Britain And Northern Ireland)",
                          |            "landline": "847663966",
                          |            "mobile": "48390756243",
                          |            "emailAddress": "info@example.com"
                          |        }
                          |    ]
                          |}
                          |""".stripMargin)
                  .futureValue

                assertAsExpected(
                  response,
                  BAD_REQUEST,
                  Some("""
                         |[
                         |  {
                         |    "code": "invalid-contact-2-name"
                         |  }
                         |]
                         |""".stripMargin)
                )
                verifyThatDownstreamApiWasNotCalled()
              }
            }
            "the landline number is" - {
              "blank" in {
                val response = wsClient
                  .url(fullUrl("/subscriptions"))
                  .withHttpHeaders(("Content-Type", "application/json"))
                  .post("""
                          |{
                          |    "id": {
                          |        "type": "NINO",
                          |        "value": "AA000000A"
                          |    },
                          |    "name": "Harold Winter",
                          |    "contacts": [
                          |        {
                          |            "type": "I",
                          |            "firstName": "Patrick",
                          |            "middleName": "John",
                          |            "lastName": "Dyson",
                          |            "landline": "747663966",
                          |            "mobile": "38390756243",
                          |            "emailAddress": "Patrick.Dyson@example.com"
                          |        },
                          |        {
                          |            "type": "O",
                          |            "name": "Dyson",
                          |            "landline": "",
                          |            "mobile": "48390756243",
                          |            "emailAddress": "info@example.com"
                          |        }
                          |    ]
                          |}
                          |""".stripMargin)
                  .futureValue

                assertAsExpected(
                  response,
                  BAD_REQUEST,
                  Some("""
                         |[
                         |  {
                         |    "code": "invalid-contact-2-landline"
                         |  }
                         |]
                         |""".stripMargin)
                )
                verifyThatDownstreamApiWasNotCalled()
              }
              "too long" in {
                val response = wsClient
                  .url(fullUrl("/subscriptions"))
                  .withHttpHeaders(("Content-Type", "application/json"))
                  .post("""
                          |{
                          |    "id": {
                          |        "type": "NINO",
                          |        "value": "AA000000A"
                          |    },
                          |    "name": "Harold Winter",
                          |    "contacts": [
                          |        {
                          |            "type": "I",
                          |            "firstName": "Patrick",
                          |            "middleName": "John",
                          |            "lastName": "Dyson",
                          |            "landline": "747663966",
                          |            "mobile": "38390756243",
                          |            "emailAddress": "Patrick.Dyson@example.com"
                          |        },
                          |        {
                          |            "type": "O",
                          |            "name": "Dyson",
                          |            "landline": "747663966747663966747663966",
                          |            "mobile": "48390756243",
                          |            "emailAddress": "info@example.com"
                          |        }
                          |    ]
                          |}
                          |""".stripMargin)
                  .futureValue

                assertAsExpected(
                  response,
                  BAD_REQUEST,
                  Some("""
                         |[
                         |  {
                         |    "code": "invalid-contact-2-landline"
                         |  }
                         |]
                         |""".stripMargin)
                )
                verifyThatDownstreamApiWasNotCalled()
              }
              "of an invalid format" in {
                val response = wsClient
                  .url(fullUrl("/subscriptions"))
                  .withHttpHeaders(("Content-Type", "application/json"))
                  .post("""
                          |{
                          |    "id": {
                          |        "type": "NINO",
                          |        "value": "AA000000A"
                          |    },
                          |    "name": "Harold Winter",
                          |    "contacts": [
                          |        {
                          |            "type": "I",
                          |            "firstName": "Patrick",
                          |            "middleName": "John",
                          |            "lastName": "Dyson",
                          |            "landline": "747663966",
                          |            "mobile": "38390756243",
                          |            "emailAddress": "Patrick.Dyson@example.com"
                          |        },
                          |        {
                          |            "type": "O",
                          |            "name": "Dyson",
                          |            "landline": "£747663966",
                          |            "mobile": "48390756243",
                          |            "emailAddress": "info@example.com"
                          |        }
                          |    ]
                          |}
                          |""".stripMargin)
                  .futureValue

                assertAsExpected(
                  response,
                  BAD_REQUEST,
                  Some("""
                         |[
                         |  {
                         |    "code": "invalid-contact-2-landline"
                         |  }
                         |]
                         |""".stripMargin)
                )
                verifyThatDownstreamApiWasNotCalled()
              }
            }
            "the mobile number is" - {
              "blank" in {
                val response = wsClient
                  .url(fullUrl("/subscriptions"))
                  .withHttpHeaders(("Content-Type", "application/json"))
                  .post("""
                          |{
                          |    "id": {
                          |        "type": "NINO",
                          |        "value": "AA000000A"
                          |    },
                          |    "name": "Harold Winter",
                          |    "contacts": [
                          |        {
                          |            "type": "I",
                          |            "firstName": "Patrick",
                          |            "middleName": "John",
                          |            "lastName": "Dyson",
                          |            "landline": "747663966",
                          |            "mobile": "38390756243",
                          |            "emailAddress": "Patrick.Dyson@example.com"
                          |        },
                          |        {
                          |            "type": "O",
                          |            "name": "Dyson",
                          |            "landline": "747663966",
                          |            "mobile": "",
                          |            "emailAddress": "info@example.com"
                          |        }
                          |    ]
                          |}
                          |""".stripMargin)
                  .futureValue

                assertAsExpected(
                  response,
                  BAD_REQUEST,
                  Some("""
                         |[
                         |  {
                         |    "code": "invalid-contact-2-mobile"
                         |  }
                         |]
                         |""".stripMargin)
                )
                verifyThatDownstreamApiWasNotCalled()
              }
              "too long" in {
                val response = wsClient
                  .url(fullUrl("/subscriptions"))
                  .withHttpHeaders(("Content-Type", "application/json"))
                  .post("""
                          |{
                          |    "id": {
                          |        "type": "NINO",
                          |        "value": "AA000000A"
                          |    },
                          |    "name": "Harold Winter",
                          |    "contacts": [
                          |        {
                          |            "type": "I",
                          |            "firstName": "Patrick",
                          |            "middleName": "John",
                          |            "lastName": "Dyson",
                          |            "landline": "747663966",
                          |            "mobile": "38390756243",
                          |            "emailAddress": "Patrick.Dyson@example.com"
                          |        },
                          |        {
                          |            "type": "O",
                          |            "name": "Dyson",
                          |            "landline": "747663966",
                          |            "mobile": "38390756243383907562433839",
                          |            "emailAddress": "info@example.com"
                          |        }
                          |    ]
                          |}
                          |""".stripMargin)
                  .futureValue

                assertAsExpected(
                  response,
                  BAD_REQUEST,
                  Some("""
                         |[
                         |  {
                         |    "code": "invalid-contact-2-mobile"
                         |  }
                         |]
                         |""".stripMargin)
                )
                verifyThatDownstreamApiWasNotCalled()
              }
              "of an invalid format" in {
                val response = wsClient
                  .url(fullUrl("/subscriptions"))
                  .withHttpHeaders(("Content-Type", "application/json"))
                  .post("""
                          |{
                          |    "id": {
                          |        "type": "NINO",
                          |        "value": "AA000000A"
                          |    },
                          |    "name": "Harold Winter",
                          |    "contacts": [
                          |        {
                          |            "type": "I",
                          |            "firstName": "Patrick",
                          |            "middleName": "John",
                          |            "lastName": "Dyson",
                          |            "landline": "747663966",
                          |            "mobile": "38390756243",
                          |            "emailAddress": "Patrick.Dyson@example.com"
                          |        },
                          |        {
                          |            "type": "O",
                          |            "name": "Dyson",
                          |            "landline": "747663966",
                          |            "mobile": "£38390756243",
                          |            "emailAddress": "info@example.com"
                          |        }
                          |    ]
                          |}
                          |""".stripMargin)
                  .futureValue

                assertAsExpected(
                  response,
                  BAD_REQUEST,
                  Some("""
                         |[
                         |  {
                         |    "code": "invalid-contact-2-mobile"
                         |  }
                         |]
                         |""".stripMargin)
                )
                verifyThatDownstreamApiWasNotCalled()
              }
            }
            "the email address is" - {
              "absent" in {
                val response = wsClient
                  .url(fullUrl("/subscriptions"))
                  .withHttpHeaders(("Content-Type", "application/json"))
                  .post("""
                          |{
                          |    "id": {
                          |        "type": "NINO",
                          |        "value": "AA000000A"
                          |    },
                          |    "name": "Harold Winter",
                          |    "contacts": [
                          |        {
                          |            "type": "I",
                          |            "firstName": "Patrick",
                          |            "middleName": "John",
                          |            "lastName": "Dyson",
                          |            "landline": "747663966",
                          |            "mobile": "38390756243",
                          |            "emailAddress": "Patrick.Dyson@example.com"
                          |        },
                          |        {
                          |            "type": "O",
                          |            "name": "Dyson",
                          |            "landline": "747663966",
                          |            "mobile": "38390756243"
                          |        }
                          |    ]
                          |}
                          |""".stripMargin)
                  .futureValue

                assertAsExpected(
                  response,
                  BAD_REQUEST,
                  Some("""
                         |[
                         |  {
                         |    "code": "invalid-contact-2-email-address"
                         |  }
                         |]
                         |""".stripMargin)
                )
                verifyThatDownstreamApiWasNotCalled()
              }
              "blank" in {
                val response = wsClient
                  .url(fullUrl("/subscriptions"))
                  .withHttpHeaders(("Content-Type", "application/json"))
                  .post("""
                          |{
                          |    "id": {
                          |        "type": "NINO",
                          |        "value": "AA000000A"
                          |    },
                          |    "name": "Harold Winter",
                          |    "contacts": [
                          |        {
                          |            "type": "I",
                          |            "firstName": "Patrick",
                          |            "middleName": "John",
                          |            "lastName": "Dyson",
                          |            "landline": "747663966",
                          |            "mobile": "38390756243",
                          |            "emailAddress": "Patrick.Dyson@example.com"
                          |        },
                          |        {
                          |            "type": "O",
                          |            "name": "Dyson",
                          |            "landline": "747663966",
                          |            "mobile": "38390756243",
                          |            "emailAddress": ""
                          |        }
                          |    ]
                          |}
                          |""".stripMargin)
                  .futureValue

                assertAsExpected(
                  response,
                  BAD_REQUEST,
                  Some("""
                         |[
                         |  {
                         |    "code": "invalid-contact-2-email-address"
                         |  }
                         |]
                         |""".stripMargin)
                )
                verifyThatDownstreamApiWasNotCalled()
              }
              "too long" in {
                val response = wsClient
                  .url(fullUrl("/subscriptions"))
                  .withHttpHeaders(("Content-Type", "application/json"))
                  .post("""
                          |{
                          |    "id": {
                          |        "type": "NINO",
                          |        "value": "AA000000A"
                          |    },
                          |    "name": "Harold Winter",
                          |    "contacts": [
                          |        {
                          |            "type": "I",
                          |            "firstName": "Patrick",
                          |            "middleName": "John",
                          |            "lastName": "Dyson",
                          |            "landline": "747663966",
                          |            "mobile": "38390756243",
                          |            "emailAddress": "Patrick.Dyson@example.com"
                          |        },
                          |        {
                          |            "type": "O",
                          |            "name": "Dyson",
                          |            "landline": "747663966",
                          |            "mobile": "38390756243",
                          |            "emailAddress": "loremipsumdolorsitametconsetetursadipscingelitrseddiamnonumyeirmodtemporinviduntutlaboreetdoloremagnLoremipsumdolorsisum@example.com"
                          |        }
                          |    ]
                          |}
                          |""".stripMargin)
                  .futureValue

                assertAsExpected(
                  response,
                  BAD_REQUEST,
                  Some("""
                         |[
                         |  {
                         |    "code": "invalid-contact-2-email-address"
                         |  }
                         |]
                         |""".stripMargin)
                )
                verifyThatDownstreamApiWasNotCalled()
              }
              "of an invalid format" in {
                val response = wsClient
                  .url(fullUrl("/subscriptions"))
                  .withHttpHeaders(("Content-Type", "application/json"))
                  .post("""
                          |{
                          |    "id": {
                          |        "type": "NINO",
                          |        "value": "AA000000A"
                          |    },
                          |    "name": "Harold Winter",
                          |    "contacts": [
                          |        {
                          |            "type": "I",
                          |            "firstName": "Patrick",
                          |            "middleName": "John",
                          |            "lastName": "Dyson",
                          |            "landline": "747663966",
                          |            "mobile": "38390756243",
                          |            "emailAddress": "Patrick.Dyson@example.com"
                          |        },
                          |        {
                          |            "type": "O",
                          |            "name": "Dyson",
                          |            "landline": "747663966",
                          |            "mobile": "38390756243",
                          |            "emailAddress": "@example.com"
                          |        }
                          |    ]
                          |}
                          |""".stripMargin)
                  .futureValue

                assertAsExpected(
                  response,
                  BAD_REQUEST,
                  Some("""
                         |[
                         |  {
                         |    "code": "invalid-contact-2-email-address"
                         |  }
                         |]
                         |""".stripMargin)
                )
                verifyThatDownstreamApiWasNotCalled()
              }
            }
          }
          "absent" in {
            val response = wsClient
              .url(fullUrl("/subscriptions"))
              .withHttpHeaders(("Content-Type", "application/json"))
              .post("""
                      |{
                      |    "id": {
                      |        "type": "NINO",
                      |        "value": "AA000000A"
                      |    },
                      |    "name": "Harold Winter",
                      |    "contacts": [
                      |        {
                      |            "firstName": "Patrick",
                      |            "middleName": "John",
                      |            "lastName": "Dyson",
                      |            "landline": "747663966",
                      |            "mobile": "38390756243",
                      |            "emailAddress": "Patrick.Dyson@example.com"
                      |        },
                      |        {
                      |            "type": "O",
                      |            "name": "Dyson",
                      |            "landline": "747663966",
                      |            "mobile": "38390756243",
                      |            "emailAddress": "info@example.com"
                      |        }
                      |    ]
                      |}
                      |""".stripMargin)
              .futureValue

            assertAsExpected(
              response,
              BAD_REQUEST,
              Some("""
                     |[
                     |  {
                     |    "code": "invalid-contact-1-type"
                     |  }
                     |]
                     |""".stripMargin)
            )
            verifyThatDownstreamApiWasNotCalled()
          }
          "unrecognised" in {
            val response = wsClient
              .url(fullUrl("/subscriptions"))
              .withHttpHeaders(("Content-Type", "application/json"))
              .post("""
                      |{
                      |    "id": {
                      |        "type": "NINO",
                      |        "value": "AA000000A"
                      |    },
                      |    "name": "Harold Winter",
                      |    "contacts": [
                      |        {
                      |            "type": "I",
                      |            "firstName": "Patrick",
                      |            "middleName": "John",
                      |            "lastName": "Dyson",
                      |            "landline": "747663966",
                      |            "mobile": "38390756243",
                      |            "emailAddress": "Patrick.Dyson@example.com"
                      |        },
                      |        {
                      |            "type": "X",
                      |            "name": "Dyson",
                      |            "landline": "747663966",
                      |            "mobile": "38390756243",
                      |            "emailAddress": "info@example.com"
                      |        }
                      |    ]
                      |}
                      |""".stripMargin)
              .futureValue

            assertAsExpected(
              response,
              BAD_REQUEST,
              Some("""
                     |[
                     |  {
                     |    "code": "invalid-contact-2-type"
                     |  }
                     |]
                     |""".stripMargin)
            )
            verifyThatDownstreamApiWasNotCalled()
          }
        }
      }
    }
  }
}
