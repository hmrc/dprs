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
import uk.gov.hmrc.dprs.connectors.subscription.CreateSubscriptionConnector

class CreateSubscriptionSpec extends BaseBackendIntegrationSpec {

  override val baseConnectorPath: String = CreateSubscriptionConnector.connectorPath

  "attempting to create a subscription, when" - {
    "the request is" - {
      "valid, when" - {
        "containing a single contact" in {
          stubFor(
            post(urlEqualTo(baseConnectorPath))
              .withRequestBody(equalToJson(s"""
                                              |{
                                              |    "idType": "NINO",
                                              |    "idNumber": "AA000000A",
                                              |    "tradingName": "Harold Winter",
                                              |    "gbUser": true,
                                              |    "primaryContact": {
                                              |        "individual": {
                                              |            "firstName": "Patrick",
                                              |            "middleName": "John",
                                              |            "lastName": "Dyson"
                                              |        },
                                              |        "email": "Patrick.Dyson@example.com",
                                              |        "mobile": "747663966",
                                              |        "phone": "38390756243"
                                              |    }
                                              |}
                                              |""".stripMargin))
              .willReturn(
                aResponse()
                  .withHeader("Content-Type", "application/json")
                  .withStatus(CREATED)
                  .withBody(s"""
                               |{
                               |  "success": {
                               |    "processingDate": "2001-12-17T09:30:47Z",
                               |    "dprsReference": "XSP1234567890"
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
                |  "id": "XSP1234567890"
                |}
                |""".stripMargin
            )
          )
          verifyThatDownstreamApiWasCalled()
        }
        "containing multiple contacts" in {
          stubFor(
            post(urlEqualTo(baseConnectorPath))
              .withRequestBody(equalToJson(s"""
                                              {
                                              |            "idType": "NINO",
                                              |            "idNumber": "AA000000A",
                                              |            "tradingName": "Harold Winter",
                                              |            "gbUser": true,
                                              |            "primaryContact": {
                                              |                "individual": {
                                              |                    "firstName": "Patrick",
                                              |                    "middleName": "John",
                                              |                    "lastName": "Dyson"
                                              |                },
                                              |                "email": "Patrick.Dyson@example.com",
                                              |                "mobile": "747663966",
                                              |                "phone": "38390756243"
                                              |            },
                                              |             "secondaryContact": {
                                              |                "organisation": {
                                              |                    "name" : "Dyson"
                                              |                },
                                              |                "email": "info@example.com",
                                              |                "mobile": "847663966",
                                              |                "phone": "48390756243"
                                              |            }
                                              |        }
                                              |""".stripMargin))
              .willReturn(
                aResponse()
                  .withHeader("Content-Type", "application/json")
                  .withStatus(CREATED)
                  .withBody(s"""
                       |{
                       |  "success": {
                       |    "processingDate": "2001-12-17T09:30:47Z",
                       |    "dprsReference": "XSP1234567890"
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
                |  "id": "XSP1234567890"
                |}
                |""".stripMargin
            )
          )
          verifyThatDownstreamApiWasCalled()
        }
      }
      "valid but the integration call fails with response:" - {
        "bad request" in {
          stubFor(
            post(urlEqualTo(baseConnectorPath))
              .withRequestBody(equalToJson(s"""
                   |{
                   |    "idType": "NINO",
                   |    "idNumber": "AA000000A",
                   |    "tradingName": "Harold Winter",
                   |    "gbUser": true,
                   |    "primaryContact": {
                   |        "individual": {
                   |            "firstName": "Patrick",
                   |            "middleName": "John",
                   |            "lastName": "Dyson"
                   |        },
                   |        "email": "Patrick.Dyson@example.com",
                   |        "mobile": "747663966",
                   |        "phone": "38390756243"
                   |    }
                   |}
                   |""".stripMargin))
              .willReturn(
                aResponse()
                  .withHeader("Content-Type", "application/json")
                  .withStatus(INTERNAL_SERVER_ERROR)
                  .withBody(s"""
                       |{
                       |    "errorDetail": {
                       |        "errorCode": "400",
                       |        "errorMessage": "Malformed json request payload",
                       |        "source": "journey-dprs0201-service-camel",
                       |        "sourceFaultDetail": {
                       |            "detail": [
                       |                "Malformed json request payload"
                       |            ]
                       |        },
                       |        "timestamp": "2020-09-28T14:31:41.286Z",
                       |        "correlationId": "d60de98c-f499-47f5-b2d6-e80966e8d19e"
                       |    }
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
        "could not be processed" in {
          stubFor(
            post(urlEqualTo(baseConnectorPath))
              .withRequestBody(equalToJson(s"""
                                              {
                                              |    "idType": "NINO",
                                              |    "idNumber": "AA000000A",
                                              |    "tradingName": "Harold Winter",
                                              |    "gbUser": true,
                                              |    "primaryContact": {
                                              |        "individual": {
                                              |            "firstName": "Patrick",
                                              |            "middleName": "John",
                                              |            "lastName": "Dyson"
                                              |        },
                                              |        "email": "Patrick.Dyson@example.com",
                                              |        "mobile": "747663966",
                                              |        "phone": "38390756243"
                                              |    }
                                              |}
                                              |""".stripMargin))
              .willReturn(
                aResponse()
                  .withHeader("Content-Type", "application/json")
                  .withStatus(UNPROCESSABLE_ENTITY)
                  .withBody(s"""
                               |{
                               |    "errorDetail": {
                               |        "errorCode": "003",
                               |        "errorMessage": "Request Could not be processed",
                               |        "source": "ETMP",
                               |        "sourceFaultDetail": {
                               |            "detail": [
                               |                "Request Could not be processed"
                               |            ]
                               |        },
                               |        "timestamp": "2023-08-31T13:00:21.655Z",
                               |        "correlationId": "d60de98c-f499-47f5-b2d6-e80966e8d19e"
                               |    }
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
                   |    "code": "eis-returned-service-unavailable"
                   |  }
                   |]
                   |""".stripMargin)
          )
          verifyThatDownstreamApiWasCalled()
        }
        "duplicate submission" in {
          stubFor(
            post(urlEqualTo(baseConnectorPath))
              .withRequestBody(equalToJson(s"""
                                              |{
                                              |    "idType": "NINO",
                                              |    "idNumber": "AA000000A",
                                              |    "tradingName": "Harold Winter",
                                              |    "gbUser": true,
                                              |    "primaryContact": {
                                              |        "individual": {
                                              |            "firstName": "Patrick",
                                              |            "middleName": "John",
                                              |            "lastName": "Dyson"
                                              |        },
                                              |        "email": "Patrick.Dyson@example.com",
                                              |        "mobile": "747663966",
                                              |        "phone": "38390756243"
                                              |    }
                                              |}
                                              |""".stripMargin))
              .willReturn(
                aResponse()
                  .withHeader("Content-Type", "application/json")
                  .withStatus(UNPROCESSABLE_ENTITY)
                  .withBody(s"""
                               |{
                               |    "errorDetail": {
                               |        "errorCode": "004",
                               |        "errorMessage": "Duplicate Submission",
                               |        "source": "ETMP",
                               |        "sourceFaultDetail": {
                               |            "detail": [
                               |                "Duplicate Submission"
                               |            ]
                               |        },
                               |        "timestamp": "2023-08-31T13:00:21.655Z",
                               |        "correlationId": "d60de98c-f499-47f5-b2d6-e80966e8d19e"
                               |    }
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
        "invalid ID" in {
          stubFor(
            post(urlEqualTo(baseConnectorPath))
              .withRequestBody(equalToJson(s"""
                                              |{
                                              |    "idType": "NINO",
                                              |    "idNumber": "AA000000A",
                                              |    "tradingName": "Harold Winter",
                                              |    "gbUser": true,
                                              |    "primaryContact": {
                                              |        "individual": {
                                              |            "firstName": "Patrick",
                                              |            "middleName": "John",
                                              |            "lastName": "Dyson"
                                              |        },
                                              |        "email": "Patrick.Dyson@example.com",
                                              |        "mobile": "747663966",
                                              |        "phone": "38390756243"
                                              |    }
                                              |}
                                              |""".stripMargin))
              .willReturn(
                aResponse()
                  .withHeader("Content-Type", "application/json")
                  .withStatus(UNPROCESSABLE_ENTITY)
                  .withBody(s"""
                               |{
                               |    "errorDetail": {
                               |        "errorCode": "016",
                               |        "errorMessage": "Invalid ID",
                               |        "source": "ETMP",
                               |        "sourceFaultDetail": {
                               |            "detail": [
                               |                "Invalid ID"
                               |            ]
                               |        },
                               |        "timestamp": "2023-08-31T13:00:21.655Z",
                               |        "correlationId": "d60de98c-f499-47f5-b2d6-e80966e8d19e"
                               |    }
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
        "unauthorized" in {
          stubFor(
            post(urlEqualTo(baseConnectorPath))
              .withRequestBody(equalToJson(s"""
                                              |{
                                              |    "idType": "NINO",
                                              |    "idNumber": "AA000000A",
                                              |    "tradingName": "Harold Winter",
                                              |    "gbUser": true,
                                              |    "primaryContact": {
                                              |        "individual": {
                                              |            "firstName": "Patrick",
                                              |            "middleName": "John",
                                              |            "lastName": "Dyson"
                                              |        },
                                              |        "email": "Patrick.Dyson@example.com",
                                              |        "mobile": "747663966",
                                              |        "phone": "38390756243"
                                              |    }
                                              |}
                                              |""".stripMargin))
              .willReturn(
                aResponse()
                  .withHeader("Content-Type", "application/json")
                  .withStatus(INTERNAL_SERVER_ERROR)
                  .withBody(s"""
                               |{
                               |    "errorDetail": {
                               |        "errorCode": "401",
                               |        "errorMessage": "Unexpected backend application error",
                               |        "source": "ETMP",
                               |        "sourceFaultDetail": {
                               |            "detail": [
                               |                "Unexpected backend application error"
                               |            ]
                               |        },
                               |        "timestamp": "2023-09-07T14:02:47.029Z",
                               |        "correlationId": "82d0bc78-22d3-4157-8e2d-f718155d0f95"
                               |    }
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
            UNAUTHORIZED,
            Some(
              """
                |[
                |  {
                |    "code": "eis-returned-unauthorised"
                |  }
                |]
                |""".stripMargin
            )
          )
          verifyThatDownstreamApiWasCalled()
        }
        "forbidden" in {
          stubFor(
            post(urlEqualTo(baseConnectorPath))
              .withRequestBody(equalToJson(s"""
                   |{
                   |    "idType": "NINO",
                   |    "idNumber": "AA000000A",
                   |    "tradingName": "Harold Winter",
                   |    "gbUser": true,
                   |    "primaryContact": {
                   |        "individual": {
                   |            "firstName": "Patrick",
                   |            "middleName": "John",
                   |            "lastName": "Dyson"
                   |        },
                   |        "email": "Patrick.Dyson@example.com",
                   |        "mobile": "747663966",
                   |        "phone": "38390756243"
                   |    }
                   |}
                   |""".stripMargin))
              .willReturn(
                aResponse()
                  .withHeader("Content-Type", "application/json")
                  .withStatus(INTERNAL_SERVER_ERROR)
                  .withBody(s"""
                       |{
                       |    "errorDetail": {
                       |        "errorCode": "403",
                       |        "errorMessage": "Unexpected backend application error",
                       |        "source": "ETMP",
                       |        "sourceFaultDetail": {
                       |            "detail": [
                       |                "Unexpected backend application error"
                       |            ]
                       |        },
                       |        "timestamp": "2023-09-07T14:02:47.029Z",
                       |        "correlationId": "82d0bc78-22d3-4157-8e2d-f718155d0f95"
                       |    }
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
            FORBIDDEN,
            Some(
              """
                |[
                |  {
                |    "code": "eis-returned-forbidden"
                |  }
                |]
                |""".stripMargin
            )
          )
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
