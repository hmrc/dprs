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

package uk.gov.hmrc.dprs

import com.github.tomakehurst.wiremock.client.WireMock._
import org.scalatest.matchers.{MatchResult, Matcher}
import play.api.http.Status.{BAD_REQUEST, IM_A_TEAPOT, INTERNAL_SERVER_ERROR, OK, SERVICE_UNAVAILABLE}
import play.api.libs.json._
import play.api.libs.ws.WSResponse
import uk.gov.hmrc.dprs.RegistrationWithIdSpec.CustomMatchers.{haveJsonBody, haveNoBody, haveStatus}

import java.time.Instant
import scala.jdk.CollectionConverters.CollectionHasAsScala

class RegistrationWithIdSpec extends BaseIntegrationSpec {

  private val currentDateAndTime: String       = Instant.now(fixedClock).toString
  private val acknowledgementReference: String = fixedAcknowledgeReferenceGenerator.generate()
  private val connectorPath                    = "/dac6/dct70b/v1"

  override def extraApplicationConfig: Map[String, Any] = Map(
    "microservice.services.registration-with-id.host"    -> wireMockServerHost,
    "microservice.services.registration-with-id.port"    -> wireMockServer.port(),
    "microservice.services.registration-with-id.context" -> connectorPath
  )

  "attempting to register with an ID, as an" - {
    "individual, when" - {
      "the request is" - {
        "valid" in {
          stubFor(
            post(urlEqualTo(connectorPath))
              .withRequestBody(equalToJson(s"""
                  |{
                  |  "registerWithIDRequest": {
                  |    "requestCommon": {
                  |      "receiptDate": "$currentDateAndTime",
                  |      "regime": "MDR",
                  |      "acknowledgementReference": "$acknowledgementReference"
                  |    },
                  |    "requestDetail": {
                  |      "IDType": "NINO",
                  |      "IDNumber": "AA000000A",
                  |      "requiresNameMatch": true,
                  |      "isAnAgent": false,
                  |      "individual": {
                  |        "firstName": "Patrick",
                  |        "lastName": "Dyson",
                  |        "dateOfBirth": "1970-10-04"
                  |      }
                  |    }
                  |  }
                  |}
                  |""".stripMargin))
              .willReturn(
                aResponse()
                  .withHeader("Content-Type", "application/json")
                  .withStatus(OK)
                  .withBody(s"""
                    |{
                    |      "registerWithIDResponse": {
                    |        "responseCommon": {
                    |          "status": "OK",
                    |          "statusText": "",
                    |          "processingDate": "$currentDateAndTime",
                    |          "returnParameters": [
                    |            {
                    |              "paramName": "SAP_NUMBER",
                    |              "paramValue": "1960629967"
                    |            }
                    |          ]
                    |        },
                    |        "responseDetail": {
                    |          "SAFEID": "XE0000200775706",
                    |          "ARN": "WARN3849921",
                    |          "isEditable": true,
                    |          "isAnAgent": false,
                    |          "isAnIndividual": true,
                    |          "individual": {
                    |            "firstName": "Patrick",
                    |            "middleName": "John",
                    |            "lastName": "Dyson",
                    |            "dateOfBirth": "1970-10-04"
                    |          },
                    |          "address": {
                    |            "addressLine1": "26424 Cecelia Junction",
                    |            "addressLine2": "Suite 858",
                    |            "addressLine3": "",
                    |            "addressLine4": "West Siobhanberg",
                    |            "postalCode": "OX2 3HD",
                    |            "countryCode": "AD"
                    |          },
                    |          "contactDetails": {
                    |            "phoneNumber": "747663966",
                    |            "mobileNumber": "38390756243",
                    |            "faxNumber": "58371813020",
                    |            "emailAddress": "Patrick.Dyson@example.com"
                    |          }
                    |        }
                    |      }
                    |    }
                    |""".stripMargin)
              )
          )

          val response = wsClient
            .url(fullUrl("/registrations/withId/individual"))
            .withHttpHeaders(("Content-Type", "application/json"))
            .post("""
                |{
                |  "id": {
                |    "type": "NINO",
                |    "value": "AA000000A"
                |  },
                |  "firstName": "Patrick",
                |  "lastName": "Dyson",
                |  "dateOfBirth": "1970-10-04"
                |}
                |""".stripMargin)
            .futureValue

          assertAsExpected(
            response = response,
            status = OK,
            jsonBodyOpt = Some(
              """
                |{
                |  "ids": [
                |    {
                |      "type": "ARN",
                |      "value": "WARN3849921"
                |    },
                |    {
                |      "type": "SAFE",
                |      "value": "XE0000200775706"
                |    },
                |    {
                |      "type": "SAP",
                |      "value": "1960629967"
                |    }
                |  ],
                |  "firstName": "Patrick",
                |  "middleName": "John",
                |  "lastName": "Dyson",
                |  "dateOfBirth": "1970-10-04",
                |  "address": {
                |    "lineOne": "26424 Cecelia Junction",
                |    "lineTwo": "Suite 858",
                |    "lineThree": "",
                |    "lineFour": "West Siobhanberg",
                |    "postalCode": "OX2 3HD",
                |    "countryCode": "AD"
                |  },
                |  "contactDetails": {
                |    "landline": "747663966",
                |    "mobile": "38390756243",
                |    "fax": "58371813020",
                |    "emailAddress": "Patrick.Dyson@example.com"
                |  }
                |}
                |""".stripMargin
            )
          )
          verifyThatDownstreamApiWasCalled()
        }
        "valid but the integration call fails with response:" - {
          "internal server error" in {
            stubFor(
              post(urlEqualTo(connectorPath))
                .withRequestBody(equalToJson(s"""
                     |{
                     |  "registerWithIDRequest": {
                     |    "requestCommon": {
                     |      "receiptDate": "$currentDateAndTime",
                     |      "regime": "MDR",
                     |      "acknowledgementReference": "$acknowledgementReference"
                     |    },
                     |    "requestDetail": {
                     |      "IDType": "NINO",
                     |      "IDNumber": "AA000000A",
                     |      "requiresNameMatch": true,
                     |      "isAnAgent": false,
                     |      "individual": {
                     |        "firstName": "Patrick",
                     |        "lastName": "Dyson",
                     |        "dateOfBirth": "1970-10-04"
                     |      }
                     |    }
                     |  }
                     |}
                     |""".stripMargin))
                .willReturn(
                  aResponse()
                    .withHeader("Content-Type", "application/json")
                    .withStatus(INTERNAL_SERVER_ERROR)
                    .withBody("""
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
              .url(fullUrl("/registrations/withId/individual"))
              .withHttpHeaders(("Content-Type", "application/json"))
              .post("""
                  |{
                  |  "id": {
                  |    "type": "NINO",
                  |    "value": "AA000000A"
                  |  },
                  |  "firstName": "Patrick",
                  |  "lastName": "Dyson",
                  |  "dateOfBirth": "1970-10-04"
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
                  |    "code": "eis-returned-internal-server-error"
                  |  }
                  |]
                  |""".stripMargin
              )
            )
            verifyThatDownstreamApiWasCalled()
          }
          "bad request" in {
            stubFor(
              post(urlEqualTo(connectorPath))
                .withRequestBody(equalToJson(s"""
                     |{
                     |  "registerWithIDRequest": {
                     |    "requestCommon": {
                     |      "receiptDate": "$currentDateAndTime",
                     |      "regime": "MDR",
                     |      "acknowledgementReference": "$acknowledgementReference"
                     |    },
                     |    "requestDetail": {
                     |      "IDType": "NINO",
                     |      "IDNumber": "AA000000A",
                     |      "requiresNameMatch": true,
                     |      "isAnAgent": false,
                     |      "individual": {
                     |        "firstName": "Patrick",
                     |        "lastName": "Dyson",
                     |        "dateOfBirth": "1970-10-04"
                     |      }
                     |    }
                     |  }
                     |}
                     |""".stripMargin))
                .willReturn(
                  aResponse()
                    .withHeader("Content-Type", "application/json")
                    .withStatus(BAD_REQUEST)
                    .withBody("""
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
              .url(fullUrl("/registrations/withId/individual"))
              .withHttpHeaders(("Content-Type", "application/json"))
              .post("""
                  |{
                  |  "id": {
                  |    "type": "NINO",
                  |    "value": "AA000000A"
                  |  },
                  |  "firstName": "Patrick",
                  |  "lastName": "Dyson",
                  |  "dateOfBirth": "1970-10-04"
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
                     |{
                     |  "registerWithIDRequest": {
                     |    "requestCommon": {
                     |      "receiptDate": "$currentDateAndTime",
                     |      "regime": "MDR",
                     |      "acknowledgementReference": "$acknowledgementReference"
                     |    },
                     |    "requestDetail": {
                     |      "IDType": "NINO",
                     |      "IDNumber": "AA000000A",
                     |      "requiresNameMatch": true,
                     |      "isAnAgent": false,
                     |      "individual": {
                     |        "firstName": "Patrick",
                     |        "lastName": "Dyson",
                     |        "dateOfBirth": "1970-10-04"
                     |      }
                     |    }
                     |  }
                     |}
                     |""".stripMargin))
                .willReturn(
                  aResponse()
                    .withHeader("Content-Type", "application/json")
                    .withStatus(SERVICE_UNAVAILABLE)
                    .withBody("""
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
              .url(fullUrl("/registrations/withId/individual"))
              .withHttpHeaders(("Content-Type", "application/json"))
              .post("""
                  |{
                  |  "id": {
                  |    "type": "NINO",
                  |    "value": "AA000000A"
                  |  },
                  |  "firstName": "Patrick",
                  |  "lastName": "Dyson",
                  |  "dateOfBirth": "1970-10-04"
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
          "i'm a teapot" in {
            stubFor(
              post(urlEqualTo(connectorPath))
                .withRequestBody(equalToJson(s"""
                     |{
                     |  "registerWithIDRequest": {
                     |    "requestCommon": {
                     |      "receiptDate": "$currentDateAndTime",
                     |      "regime": "MDR",
                     |      "acknowledgementReference": "$acknowledgementReference"
                     |    },
                     |    "requestDetail": {
                     |      "IDType": "NINO",
                     |      "IDNumber": "AA000000A",
                     |      "requiresNameMatch": true,
                     |      "isAnAgent": false,
                     |      "individual": {
                     |        "firstName": "Patrick",
                     |        "lastName": "Dyson",
                     |        "dateOfBirth": "1970-10-04"
                     |      }
                     |    }
                     |  }
                     |}
                     |""".stripMargin))
                .willReturn(
                  aResponse()
                    .withHeader("Content-Type", "application/json")
                    .withStatus(IM_A_TEAPOT)
                    .withBody("""
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
              .url(fullUrl("/registrations/withId/individual"))
              .withHttpHeaders(("Content-Type", "application/json"))
              .post("""
                  |{
                  |  "id": {
                  |    "type": "NINO",
                  |    "value": "AA000000A"
                  |  },
                  |  "firstName": "Patrick",
                  |  "lastName": "Dyson",
                  |  "dateOfBirth": "1970-10-04"
                  |}
                  |""".stripMargin)
              .futureValue

            assertAsExpected(
              response,
              INTERNAL_SERVER_ERROR,
              None
            )
            verifyThatDownstreamApiWasCalled()
          }
        }
        "invalid, specifically:" - {
          "the id type is" - {
            "absent" in {
              val response = wsClient
                .url(fullUrl("/registrations/withId/individual"))
                .withHttpHeaders(("Content-Type", "application/json"))
                .post("""
                    |{
                    |  "id": {
                    |    "value": "AA000000A"
                    |  },
                    |  "firstName": "Patrick",
                    |  "lastName": "Dyson",
                    |  "dateOfBirth": "1970-10-04"
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
                .url(fullUrl("/registrations/withId/individual"))
                .withHttpHeaders(("Content-Type", "application/json"))
                .post("""
                    |{
                    |  "id": {
                    |    "type": "",
                    |    "value": "AA000000A"
                    |  },
                    |  "firstName": "Patrick",
                    |  "lastName": "Dyson",
                    |  "dateOfBirth": "1970-10-04"
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
                .url(fullUrl("/registrations/withId/individual"))
                .withHttpHeaders(("Content-Type", "application/json"))
                .post("""
                    |{
                    |  "id": {
                    |    "type": "VAT",
                    |    "value": "AA000000A"
                    |  },
                    |  "firstName": "Patrick",
                    |  "lastName": "Dyson",
                    |  "dateOfBirth": "1970-10-04"
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
                .url(fullUrl("/registrations/withId/individual"))
                .withHttpHeaders(("Content-Type", "application/json"))
                .post("""
                    |{
                    |  "id": {
                    |    "type": "NINO"
                    |  },
                    |  "firstName": "Patrick",
                    |  "lastName": "Dyson",
                    |  "dateOfBirth": "1970-10-04"
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
                .url(fullUrl("/registrations/withId/individual"))
                .withHttpHeaders(("Content-Type", "application/json"))
                .post("""
                    |{
                    |  "id": {
                    |    "type": "NINO",
                    |    "value": ""
                    |  },
                    |  "firstName": "Patrick",
                    |  "lastName": "Dyson",
                    |  "dateOfBirth": "1970-10-04"
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
                .url(fullUrl("/registrations/withId/individual"))
                .withHttpHeaders(("Content-Type", "application/json"))
                .post(s"""
                    |{
                    |  "id": {
                    |    "type": "NINO",
                    |    "value": "XX3902342094804482044449234029408242"
                    |  },
                    |  "firstName": "Patrick",
                    |  "lastName": "Dyson",
                    |  "dateOfBirth": "1970-10-04"
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
          "the first name is" - {
            "absent" in {
              val response = wsClient
                .url(fullUrl("/registrations/withId/individual"))
                .withHttpHeaders(("Content-Type", "application/json"))
                .post("""
                    |{
                    |  "id": {
                    |    "type": "NINO",
                    |    "value": "AA000000A"
                    |  },
                    |  "lastName": "Dyson",
                    |  "dateOfBirth": "1970-10-04"
                    |}
                    |""".stripMargin)
                .futureValue

              assertAsExpected(
                response,
                BAD_REQUEST,
                Some("""
                    |[
                    |  {
                    |    "code": "invalid-first-name"
                    |  }
                    |]
                    |""".stripMargin)
              )

              verifyThatDownstreamApiWasNotCalled()
            }
            "blank" in {
              val response = wsClient
                .url(fullUrl("/registrations/withId/individual"))
                .withHttpHeaders(("Content-Type", "application/json"))
                .post("""
                    |{
                    |  "id": {
                    |    "type": "NINO",
                    |    "value": "AA000000A"
                    |  },
                    |  "firstName": "",
                    |  "lastName": "Dyson",
                    |  "dateOfBirth": "1970-10-04"
                    |}
                    |""".stripMargin)
                .futureValue

              assertAsExpected(
                response,
                BAD_REQUEST,
                Some("""
                    |[
                    |  {
                    |    "code": "invalid-first-name"
                    |  }
                    |]
                    |""".stripMargin)
              )

              verifyThatDownstreamApiWasNotCalled()
            }
            "too long" in {
              val response = wsClient
                .url(fullUrl("/registrations/withId/individual"))
                .withHttpHeaders(("Content-Type", "application/json"))
                .post(s"""
                    |{
                    |  "id": {
                    |    "type": "NINO",
                    |    "value": "AA000000A"
                    |  },
                    |  "firstName": "Patrick Alexander John Fitzpatrick James",
                    |  "lastName": "Dyson",
                    |  "dateOfBirth": "1970-10-04"
                    |}
                    |""".stripMargin)
                .futureValue

              assertAsExpected(
                response,
                BAD_REQUEST,
                Some("""
                    |[
                    |  {
                    |    "code": "invalid-first-name"
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
                .url(fullUrl("/registrations/withId/individual"))
                .withHttpHeaders(("Content-Type", "application/json"))
                .post(s"""
                     |{
                     |  "id": {
                     |    "type": "NINO",
                     |    "value": "AA000000A"
                     |  },
                     |  "firstName": "Patrick",
                     |  "middleName": "",
                     |  "lastName": "Dyson",
                     |  "dateOfBirth": "1970-10-04"
                     |}
                     |""".stripMargin)
                .futureValue

              assertAsExpected(
                response,
                BAD_REQUEST,
                Some("""
                    |[
                    |  {
                    |    "code": "invalid-middle-name"
                    |  }
                    |]
                    |""".stripMargin)
              )

              verifyThatDownstreamApiWasNotCalled()
            }
            "too long" in {
              val response = wsClient
                .url(fullUrl("/registrations/withId/individual"))
                .withHttpHeaders(("Content-Type", "application/json"))
                .post(s"""
                     |{
                     |  "id": {
                     |    "type": "NINO",
                     |    "value": "AA000000A"
                     |  },
                     |  "firstName": "Patrick",
                     |  "middleName": "Alexander John Fitzpatrick James Edward",
                     |  "lastName": "Dyson",
                     |  "dateOfBirth": "1970-10-04"
                     |}
                     |""".stripMargin)
                .futureValue

              assertAsExpected(
                response,
                BAD_REQUEST,
                Some("""
                    |[
                    |  {
                    |    "code": "invalid-middle-name"
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
                .url(fullUrl("/registrations/withId/individual"))
                .withHttpHeaders(("Content-Type", "application/json"))
                .post("""
                    |{
                    |  "id": {
                    |    "type": "NINO",
                    |    "value": "AA000000A"
                    |  },
                    |  "firstName": "Patrick",
                    |  "dateOfBirth": "1970-10-04"
                    |}
                    |""".stripMargin)
                .futureValue

              assertAsExpected(
                response,
                BAD_REQUEST,
                Some("""
                    |[
                    |  {
                    |    "code": "invalid-last-name"
                    |  }
                    |]
                    |""".stripMargin)
              )

              verifyThatDownstreamApiWasNotCalled()
            }
            "blank" in {
              val response = wsClient
                .url(fullUrl("/registrations/withId/individual"))
                .withHttpHeaders(("Content-Type", "application/json"))
                .post("""
                    |{
                    |  "id": {
                    |    "type": "NINO",
                    |    "value": "AA000000A"
                    |  },
                    |  "firstName": "Patrick",
                    |  "lastName": "",
                    |  "dateOfBirth": "1970-10-04"
                    |}
                    |""".stripMargin)
                .futureValue

              assertAsExpected(
                response,
                BAD_REQUEST,
                Some("""
                    |[
                    |  {
                    |    "code": "invalid-last-name"
                    |  }
                    |]
                    |""".stripMargin)
              )

              verifyThatDownstreamApiWasNotCalled()
            }
            "too long" in {
              val response = wsClient
                .url(fullUrl("/registrations/withId/individual"))
                .withHttpHeaders(("Content-Type", "application/json"))
                .post("""
                    |{
                    |  "id": {
                    |    "type": "NINO",
                    |    "value": "AA000000A"
                    |  },
                    |  "firstName": "Patrick",
                    |  "lastName": "Alexander III, Earl Of Somewhere Else",
                    |  "dateOfBirth": "1970-10-04"
                    |}
                    |""".stripMargin)
                .futureValue

              assertAsExpected(
                response,
                BAD_REQUEST,
                Some("""
                    |[
                    |  {
                    |    "code": "invalid-last-name"
                    |  }
                    |]
                    |""".stripMargin)
              )

              verifyThatDownstreamApiWasNotCalled()
            }
          }
          "the date of birth is" - {
            "absent" in {
              val response = wsClient
                .url(fullUrl("/registrations/withId/individual"))
                .withHttpHeaders(("Content-Type", "application/json"))
                .post("""
                    |{
                    |  "id": {
                    |    "type": "NINO",
                    |    "value": "AA000000A"
                    |  },
                    |  "firstName": "Patrick",
                    |  "lastName": "Dyson"
                    |}
                    |""".stripMargin)
                .futureValue

              assertAsExpected(
                response,
                BAD_REQUEST,
                Some("""
                    |[
                    |  {
                    |    "code": "invalid-date-of-birth"
                    |  }
                    |]
                    |""".stripMargin)
              )

              verifyThatDownstreamApiWasNotCalled()
            }
            "blank" in {
              val response = wsClient
                .url(fullUrl("/registrations/withId/individual"))
                .withHttpHeaders(("Content-Type", "application/json"))
                .post("""
                    |{
                    |  "id": {
                    |    "type": "NINO",
                    |    "value": "AA000000A"
                    |  },
                    |  "firstName": "Patrick",
                    |  "lastName": "Dyson",
                    |  "dateOfBirth": ""
                    |}
                    |""".stripMargin)
                .futureValue

              assertAsExpected(
                response,
                BAD_REQUEST,
                Some("""
                    |[
                    |  {
                    |    "code": "invalid-date-of-birth"
                    |  }
                    |]
                    |""".stripMargin)
              )

              verifyThatDownstreamApiWasNotCalled()
            }
            "of an invalid format" in {
              val response = wsClient
                .url(fullUrl("/registrations/withId/individual"))
                .withHttpHeaders(("Content-Type", "application/json"))
                .post("""
                    |{
                    |  "id": {
                    |    "type": "NINO",
                    |    "value": "AA000000A"
                    |  },
                    |  "firstName": "Patrick",
                    |  "lastName": "Dyson",
                    |  "dateOfBirth": "10-04-1970"
                    |}
                    |""".stripMargin)
                .futureValue

              assertAsExpected(
                response,
                BAD_REQUEST,
                Some("""
                    |[
                    |  {
                    |    "code": "invalid-date-of-birth"
                    |  }
                    |]
                    |""".stripMargin)
              )

              verifyThatDownstreamApiWasNotCalled()
            }
            "doesn't exist" in {
              val response = wsClient
                .url(fullUrl("/registrations/withId/individual"))
                .withHttpHeaders(("Content-Type", "application/json"))
                .post("""
                    |{
                    |  "id": {
                    |    "type": "NINO",
                    |    "value": "AA000000A"
                    |  },
                    |  "firstName": "Patrick",
                    |  "lastName": "Dyson",
                    |  "dateOfBirth": "1977-02-29"
                    |}
                    |""".stripMargin)
                .futureValue

              assertAsExpected(
                response,
                BAD_REQUEST,
                Some("""
                    |[
                    |  {
                    |    "code": "invalid-date-of-birth"
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
    "organisation, when" - {
      "the request is" - {
        "valid" in {
          stubFor(
            post(urlEqualTo(connectorPath))
              .withRequestBody(equalToJson(s"""
                   |{
                   |  "registerWithIDRequest": {
                   |    "requestCommon": {
                   |      "receiptDate": "$currentDateAndTime",
                   |      "regime": "MDR",
                   |      "acknowledgementReference": "$acknowledgementReference"
                   |    },
                   |    "requestDetail": {
                   |      "IDType": "UTR",
                   |      "IDNumber": "1234567890",
                   |      "requiresNameMatch": true,
                   |      "isAnAgent": false,
                   |      "organisation": {
                   |        "organisationName": "Dyson",
                   |        "organisationType": "0003"
                   |      }
                   |    }
                   |  }
                   |}
                   |""".stripMargin))
              .willReturn(
                aResponse()
                  .withHeader("Content-Type", "application/json")
                  .withStatus(OK)
                  .withBody(s"""
                      {
                      |  "registerWithIDResponse" : {
                      |    "responseCommon" : {
                      |      "status" : "OK",
                      |      "statusText" : "",
                      |      "processingDate" : "$currentDateAndTime",
                      |      "returnParameters" : [ {
                      |        "paramName" : "SAP_NUMBER",
                      |        "paramValue" : "8231791429"
                      |      } ]
                      |    },
                      |    "responseDetail" : {
                      |      "SAFEID" : "XE0000586571722",
                      |      "ARN" : "WARN1442450",
                      |      "isEditable" : true,
                      |      "isAnAgent" : false,
                      |      "isAnIndividual" : false,
                      |      "organisation" : {
                      |        "organisationName" : "Dyson",
                      |        "isAGroup" : false,
                      |        "organisationType" : "Unincorporated Body",
                      |        "code" : "0003"
                      |      },
                      |      "address" : {
                      |        "addressLine1" : "2627 Gus Hill",
                      |        "addressLine2" : "Apt. 898",
                      |        "addressLine3" : "",
                      |        "addressLine4" : "West Corrinamouth",
                      |        "postalCode" : "OX2 3HD",
                      |        "countryCode" : "AD"
                      |      },
                      |      "contactDetails" : {
                      |        "phoneNumber" : "176905117",
                      |        "mobileNumber" : "62281724761",
                      |        "faxNumber" : "08959633679",
                      |        "emailAddress" : "edward.goodenough@example.com"
                      |      }
                      |    }
                      |  }
                      |}
                      |""".stripMargin)
              )
          )

          val response = wsClient
            .url(fullUrl("/registrations/withId/organisation"))
            .withHttpHeaders(("Content-Type", "application/json"))
            .post("""
                |{
                |  "id": {
                |    "type": "UTR",
                |    "value": "1234567890"
                |  },
                |  "name": "Dyson",
                |  "type": "CorporateBody"
                |}
                |""".stripMargin)
            .futureValue

          assertAsExpected(
            response,
            OK,
            Some("""
              |{
              |  "name": "Dyson",
              |  "type": "CorporateBody",
              |  "ids": [
              |    {
              |      "type": "ARN",
              |      "value": "WARN1442450"
              |    },
              |    {
              |      "type": "SAFE",
              |      "value": "XE0000586571722"
              |    },
              |    {
              |      "type": "SAP",
              |      "value": "8231791429"
              |    }
              |  ],
              |  "address": {
              |    "lineOne": "2627 Gus Hill",
              |    "lineTwo": "Apt. 898",
              |    "lineThree": "",
              |    "lineFour": "West Corrinamouth",
              |    "postalCode": "OX2 3HD",
              |    "countryCode": "AD"
              |  },
              |  "contactDetails": {
              |    "landline": "176905117",
              |    "mobile": "62281724761",
              |    "fax": "08959633679",
              |    "emailAddress": "edward.goodenough@example.com"
              |  }
              |}
              |""".stripMargin)
          )
          verifyThatDownstreamApiWasCalled()
        }
        "valid, the integration call fails with response:" - {
          "internal server error" in {
            stubFor(
              post(urlEqualTo(connectorPath))
                .withRequestBody(equalToJson(s"""
                     |{
                     |  "registerWithIDRequest": {
                     |    "requestCommon": {
                     |      "receiptDate": "$currentDateAndTime",
                     |      "regime": "MDR",
                     |      "acknowledgementReference": "$acknowledgementReference"
                     |    },
                     |    "requestDetail": {
                     |      "IDType": "UTR",
                     |      "IDNumber": "1234567890",
                     |      "requiresNameMatch": true,
                     |      "isAnAgent": false,
                     |      "organisation": {
                     |        "organisationName": "Dyson",
                     |        "organisationType": "0003"
                     |      }
                     |    }
                     |  }
                     |}
                     |""".stripMargin))
                .willReturn(
                  aResponse()
                    .withHeader("Content-Type", "application/json")
                    .withStatus(INTERNAL_SERVER_ERROR)
                    .withBody("""
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
              .url(fullUrl("/registrations/withId/organisation"))
              .withHttpHeaders(("Content-Type", "application/json"))
              .post("""
                  |{
                  |  "id": {
                  |    "type": "UTR",
                  |    "value": "1234567890"
                  |  },
                  |  "name": "Dyson",
                  |  "type": "CorporateBody"
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
                |    "code": "eis-returned-internal-server-error"
                |  }
                |]
                |""".stripMargin
              )
            )
            verifyThatDownstreamApiWasCalled()
          }
          "bad request" in {
            stubFor(
              post(urlEqualTo(connectorPath))
                .withRequestBody(equalToJson(s"""
                     |{
                     |  "registerWithIDRequest": {
                     |    "requestCommon": {
                     |      "receiptDate": "$currentDateAndTime",
                     |      "regime": "MDR",
                     |      "acknowledgementReference": "$acknowledgementReference"
                     |    },
                     |    "requestDetail": {
                     |      "IDType": "UTR",
                     |      "IDNumber": "1234567890",
                     |      "requiresNameMatch": true,
                     |      "isAnAgent": false,
                     |      "organisation": {
                     |        "organisationName": "Dyson",
                     |        "organisationType": "0003"
                     |      }
                     |    }
                     |  }
                     |}
                     |""".stripMargin))
                .willReturn(
                  aResponse()
                    .withHeader("Content-Type", "application/json")
                    .withStatus(BAD_REQUEST)
                    .withBody("""
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
              .url(fullUrl("/registrations/withId/organisation"))
              .withHttpHeaders(("Content-Type", "application/json"))
              .post("""
                  |{
                  |  "id": {
                  |    "type": "UTR",
                  |    "value": "1234567890"
                  |  },
                  |  "name": "Dyson",
                  |  "type": "CorporateBody"
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
                     |{
                     |  "registerWithIDRequest": {
                     |    "requestCommon": {
                     |      "receiptDate": "$currentDateAndTime",
                     |      "regime": "MDR",
                     |      "acknowledgementReference": "$acknowledgementReference"
                     |    },
                     |    "requestDetail": {
                     |      "IDType": "UTR",
                     |      "IDNumber": "1234567890",
                     |      "requiresNameMatch": true,
                     |      "isAnAgent": false,
                     |      "organisation": {
                     |        "organisationName": "Dyson",
                     |        "organisationType": "0003"
                     |      }
                     |    }
                     |  }
                     |}
                     |""".stripMargin))
                .willReturn(
                  aResponse()
                    .withHeader("Content-Type", "application/json")
                    .withStatus(SERVICE_UNAVAILABLE)
                    .withBody("""
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
              .url(fullUrl("/registrations/withId/organisation"))
              .withHttpHeaders(("Content-Type", "application/json"))
              .post("""
                  |{
                  |  "id": {
                  |    "type": "UTR",
                  |    "value": "1234567890"
                  |  },
                  |  "name": "Dyson",
                  |  "type": "CorporateBody"
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
          "i'm a teapot" in {
            stubFor(
              post(urlEqualTo(connectorPath))
                .withRequestBody(equalToJson(s"""
                     |{
                     |  "registerWithIDRequest": {
                     |    "requestCommon": {
                     |      "receiptDate": "$currentDateAndTime",
                     |      "regime": "MDR",
                     |      "acknowledgementReference": "$acknowledgementReference"
                     |    },
                     |    "requestDetail": {
                     |      "IDType": "UTR",
                     |      "IDNumber": "1234567890",
                     |      "requiresNameMatch": true,
                     |      "isAnAgent": false,
                     |      "organisation": {
                     |        "organisationName": "Dyson",
                     |        "organisationType": "0003"
                     |      }
                     |    }
                     |  }
                     |}
                     |""".stripMargin))
                .willReturn(
                  aResponse()
                    .withHeader("Content-Type", "application/json")
                    .withStatus(IM_A_TEAPOT)
                    .withBody("""
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
              .url(fullUrl("/registrations/withId/organisation"))
              .withHttpHeaders(("Content-Type", "application/json"))
              .post("""
                  |{
                  |  "id": {
                  |    "type": "UTR",
                  |    "value": "1234567890"
                  |  },
                  |  "name": "Dyson",
                  |  "type": "CorporateBody"
                  |}
                  |""".stripMargin)
              .futureValue

            assertAsExpected(
              response,
              INTERNAL_SERVER_ERROR,
              None
            )
            verifyThatDownstreamApiWasCalled()
          }
        }
        "invalid, specifically:" - {
          "the id type is" - {
            "absent" in {
              val response = wsClient
                .url(fullUrl("/registrations/withId/organisation"))
                .withHttpHeaders(("Content-Type", "application/json"))
                .post("""
                    |{
                    |  "id": {
                    |    "value": "1234567890"
                    |  },
                    |  "name": "Dyson",
                    |  "type": "CorporateBody"
                    |}
                    |""".stripMargin)
                .futureValue

              assertAsExpected(
                response,
                BAD_REQUEST,
                Some(
                  """
                  |[
                  |  {
                  |    "code": "invalid-id-type"
                  |  }
                  |]
                  |""".stripMargin
                )
              )

              verifyThatDownstreamApiWasNotCalled()
            }
            "blank" in {
              val response = wsClient
                .url(fullUrl("/registrations/withId/organisation"))
                .withHttpHeaders(("Content-Type", "application/json"))
                .post("""
                    |{
                    |  "id": {
                    |    "type": "",
                    |    "value": "1234567890"
                    |  },
                    |  "name": "Dyson",
                    |  "type": "CorporateBody"
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
                .url(fullUrl("/registrations/withId/organisation"))
                .withHttpHeaders(("Content-Type", "application/json"))
                .post("""
                    |{
                    |  "id": {
                    |    "type": "XYZ",
                    |    "value": "1234567890"
                    |  },
                    |  "name": "Dyson",
                    |  "type": "CorporateBody"
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
            "unsuitable" in {
              val response = wsClient
                .url(fullUrl("/registrations/withId/organisation"))
                .withHttpHeaders(("Content-Type", "application/json"))
                .post("""
                    |{
                    |  "id": {
                    |    "type": "NINO",
                    |    "value": "1234567890"
                    |  },
                    |  "name": "Dyson",
                    |  "type": "CorporateBody"
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
                .url(fullUrl("/registrations/withId/organisation"))
                .withHttpHeaders(("Content-Type", "application/json"))
                .post("""
                    |{
                    |  "id": {
                    |    "type": "UTR"
                    |  },
                    |  "name": "Dyson",
                    |  "type": "CorporateBody"
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
                .url(fullUrl("/registrations/withId/organisation"))
                .withHttpHeaders(("Content-Type", "application/json"))
                .post("""
                    |{
                    |  "id": {
                    |    "type": "UTR",
                    |    "value": ""
                    |  },
                    |  "name": "Dyson",
                    |  "type": "CorporateBody"
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
                .url(fullUrl("/registrations/withId/organisation"))
                .withHttpHeaders(("Content-Type", "application/json"))
                .post("""
                    |{
                    |  "id": {
                    |    "type": "UTR",
                    |    "value": "XX3902342094804482044449234029408242"
                    |  },
                    |  "name": "Dyson",
                    |  "type": "CorporateBody"
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
            "absent" in {
              val response = wsClient
                .url(fullUrl("/registrations/withId/organisation"))
                .withHttpHeaders(("Content-Type", "application/json"))
                .post("""
                    |{
                    |  "id": {
                    |    "type": "UTR",
                    |    "value": "1234567890"
                    |  },
                    |  "type": "CorporateBody"
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
            "blank" in {
              val response = wsClient
                .url(fullUrl("/registrations/withId/organisation"))
                .withHttpHeaders(("Content-Type", "application/json"))
                .post("""
                    |{
                    |  "id": {
                    |    "type": "UTR",
                    |    "value": "1234567890"
                    |  },
                    |  "name": "",
                    |  "type": "CorporateBody"
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
                .url(fullUrl("/registrations/withId/organisation"))
                .withHttpHeaders(("Content-Type", "application/json"))
                .post("""
                    |{
                    |  "id": {
                    |    "type": "UTR",
                    |    "value": "1234567890"
                    |  },
                    |  "name": "Dyson Home Appliance Corporation Of The United Kingdom",
                    |  "type": "CorporateBody"
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
          "the type is" - {
            "absent" in {
              val response = wsClient
                .url(fullUrl("/registrations/withId/organisation"))
                .withHttpHeaders(("Content-Type", "application/json"))
                .post("""
                    |{
                    |  "id": {
                    |    "type": "UTR",
                    |    "value": "1234567890"
                    |  },
                    |  "name": "Dyson"
                    |}
                    |""".stripMargin)
                .futureValue

              assertAsExpected(
                response,
                BAD_REQUEST,
                Some("""
                  |[
                  |  {
                  |    "code": "invalid-type"
                  |  }
                  |]
                  |""".stripMargin)
              )

              verifyThatDownstreamApiWasNotCalled()
            }
            "blank" in {
              val response = wsClient
                .url(fullUrl("/registrations/withId/organisation"))
                .withHttpHeaders(("Content-Type", "application/json"))
                .post("""
                    |{
                    |  "id": {
                    |    "type": "UTR",
                    |    "value": "1234567890"
                    |  },
                    |  "name": "Dyson",
                    |  "type": ""
                    |}
                    |""".stripMargin)
                .futureValue

              assertAsExpected(
                response,
                BAD_REQUEST,
                Some("""
                  |[
                  |  {
                  |    "code": "invalid-type"
                  |  }
                  |]
                  |""".stripMargin)
              )

              verifyThatDownstreamApiWasNotCalled()
            }
            "unrecognised" in {
              val response = wsClient
                .url(fullUrl("/registrations/withId/organisation"))
                .withHttpHeaders(("Content-Type", "application/json"))
                .post("""
                    |{
                    |  "id": {
                    |    "type": "UTR",
                    |    "value": "1234567890"
                    |  },
                    |  "name": "Dyson",
                    |  "type": "Charity"
                    |}
                    |""".stripMargin)
                .futureValue

              assertAsExpected(
                response,
                BAD_REQUEST,
                Some("""
                    |[
                    |  {
                    |    "code": "invalid-type"
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

  private def fullUrl(path: String): String = baseUrl + "/dprs" + path

  private def verifyThatDownstreamApiWasNotCalled(): Unit =
    verify(0, postRequestedFor(urlEqualTo(connectorPath)))

  private def verifyThatDownstreamApiWasCalled(): Unit = {
    getAllServeEvents.asScala.count(_.getWasMatched) shouldBe 1
  }

  private def assertAsExpected(response: WSResponse, status: Int, jsonBodyOpt: Option[String] = None) = {
    response should haveStatus(status)
    jsonBodyOpt match {
      case Some(body) => response should haveJsonBody(body)
      case None       => response should haveNoBody
    }
  }
}

object RegistrationWithIdSpec {

  object CustomMatchers {

    class HaveStatus(expectedStatus: Int) extends Matcher[WSResponse] {
      override def apply(response: WSResponse): MatchResult =
        MatchResult(
          response.status == expectedStatus,
          s"We expected the response to have status [$expectedStatus], but it was actually [${response.status}].",
          s"We didn't expect the response to have status [$expectedStatus], but it was indeed."
        )
    }

    class HaveJsonBody(expectedRawJsonBody: String) extends Matcher[WSResponse] {
      override def apply(response: WSResponse): MatchResult = {
        val expectedJsonBody = Json.parse(expectedRawJsonBody)
        val actualJsonBody   = Json.parse(response.body)
        MatchResult(
          actualJsonBody == expectedJsonBody,
          s"We expected the response to have a json body [\n${Json.prettyPrint(expectedJsonBody)}\n], but it was actually [\n${Json.prettyPrint(actualJsonBody)}\n].",
          s"We didn't expect the response to have a json body [\n${Json.prettyPrint(expectedJsonBody)}], but it was indeed."
        )
      }
    }

    class HaveNoBody() extends Matcher[WSResponse] {
      override def apply(response: WSResponse): MatchResult = {
        val actualBody = response.body
        MatchResult(
          actualBody == "",
          s"We expected the response to have no body, but it was actually [\n$actualBody\n].",
          s"We expected the response to have a body, but it didn't."
        )
      }
    }

    def haveStatus(expectedStatus: Int) = new HaveStatus(expectedStatus)

    def haveJsonBody(expectedRawJsonBody: String) = new HaveJsonBody(expectedRawJsonBody)

    def haveNoBody = new HaveNoBody()

  }
}
