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

package uk.gov.hmrc.dprs.registration.withId

import com.github.tomakehurst.wiremock.client.WireMock._
import play.api.http.Status._

class RegistrationWithIdForIndividualSpec extends BaseRegistrationWithIdSpec {

  "attempting to register with an ID, as an individual, when" - {
    "the request is" - {
      "valid" in {
        stubFor(
          post(urlEqualTo(baseConnectorPath))
            .withRequestBody(equalToJson(s"""
                  |{
                  |  "registerWithIDRequest": {
                  |    "requestCommon": {
                  |      "receiptDate": "$currentDateAndTime",
                  |      "regime": "DPRS",
                  |      "acknowledgementReference": "$acknowledgementReference",
                  |      "requestParameters": [
                  |        {
                  |          "paramName": "REGIME",
                  |          "paramValue": "DPRS"
                  |        }
                  |      ]
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
        "internal error" in {
          stubFor(
            post(urlEqualTo(baseConnectorPath))
              .withRequestBody(equalToJson(s"""
                     |{
                     |  "registerWithIDRequest": {
                     |    "requestCommon": {
                     |      "receiptDate": "$currentDateAndTime",
                     |      "regime": "DPRS",
                     |      "acknowledgementReference": "$acknowledgementReference",
                     |      "requestParameters": [
                     |        {
                     |          "paramName": "REGIME",
                     |          "paramValue": "DPRS"
                     |        }
                     |      ]
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
                              |  "errorDetail": {
                              |    "timestamp": "2023-12-13T11:50:35Z",
                              |    "correlationId": "eac14118-57cf-44c5-83f9-63f50c5ff712",
                              |    "errorCode": "500",
                              |    "errorMessage": "Internal error",
                              |    "sourceFaultDetail": {
                              |      "detail": [
                              |        "Internal error"
                              |      ]
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
                  |    "code": "eis-returned-internal-error"
                  |  }
                  |]
                  |""".stripMargin
            )
          )
          verifyThatDownstreamApiWasCalled()
        }
        "bad request" in {
          stubFor(
            post(urlEqualTo(baseConnectorPath))
              .withRequestBody(equalToJson(s"""
                                              |{
                                              |  "registerWithIDRequest": {
                                              |    "requestCommon": {
                                              |      "receiptDate": "$currentDateAndTime",
                                              |      "regime": "DPRS",
                                              |      "acknowledgementReference": "$acknowledgementReference",
                                              |      "requestParameters": [
                                              |        {
                                              |          "paramName": "REGIME",
                                              |          "paramValue": "DPRS"
                                              |        }
                                              |      ]
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
        "could not be processed" in {
          stubFor(
            post(urlEqualTo(baseConnectorPath))
              .withRequestBody(equalToJson(s"""
                                              |{
                                              |  "registerWithIDRequest": {
                                              |    "requestCommon": {
                                              |      "receiptDate": "$currentDateAndTime",
                                              |      "regime": "DPRS",
                                              |      "acknowledgementReference": "$acknowledgementReference",
                                              |      "requestParameters": [
                                              |        {
                                              |          "paramName": "REGIME",
                                              |          "paramValue": "DPRS"
                                              |        }
                                              |      ]
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
                              |  "errorDetail": {
                              |    "timestamp": "2023-12-13T11:50:35Z",
                              |    "correlationId": "d102b24f-767c-4620-826b-d068f86e4abc",
                              |    "errorCode": "503",
                              |    "errorMessage": "Request could not be processed",
                              |    "source": "Back End",
                              |    "sourceFaultDetail": {
                              |      "detail": [
                              |        "001 - Request could not be processed"
                              |      ]
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
                |    "code": "eis-returned-could-not-be-processed"
                |  }
                |]
                |""".stripMargin
            )
          )
          verifyThatDownstreamApiWasCalled()
        }
        "duplicate submission" in {
          stubFor(
            post(urlEqualTo(baseConnectorPath))
              .withRequestBody(equalToJson(s"""
                                              |{
                                              |  "registerWithIDRequest": {
                                              |    "requestCommon": {
                                              |      "receiptDate": "$currentDateAndTime",
                                              |      "regime": "DPRS",
                                              |      "acknowledgementReference": "$acknowledgementReference",
                                              |      "requestParameters": [
                                              |        {
                                              |          "paramName": "REGIME",
                                              |          "paramValue": "DPRS"
                                              |        }
                                              |      ]
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
                  .withStatus(CONFLICT)
                  .withBody("""
                              |{
                              |  "errorDetail": {
                              |    "timestamp": "2023-12-13T11:50:35Z",
                              |    "correlationId": "d102b24f-767c-4620-826b-d068f86e4abc",
                              |    "errorCode": "409",
                              |    "errorMessage": "Duplicate submission",
                              |    "source": "Back End",
                              |    "sourceFaultDetail": {
                              |      "detail": [
                              |        "Duplicate submission"
                              |      ]
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
            CONFLICT,
            Some(
              """
                |[
                |  {
                |    "code": "eis-returned-duplicate-submission"
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
                                              |  "registerWithIDRequest": {
                                              |    "requestCommon": {
                                              |      "receiptDate": "$currentDateAndTime",
                                              |      "regime": "DPRS",
                                              |      "acknowledgementReference": "$acknowledgementReference",
                                              |      "requestParameters": [
                                              |        {
                                              |          "paramName": "REGIME",
                                              |          "paramValue": "DPRS"
                                              |        }
                                              |      ]
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
                  .withStatus(FORBIDDEN)
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
        "i'm a teapot" in {
          stubFor(
            post(urlEqualTo(baseConnectorPath))
              .withRequestBody(equalToJson(s"""
                                              |{
                                              |  "registerWithIDRequest": {
                                              |    "requestCommon": {
                                              |      "receiptDate": "$currentDateAndTime",
                                              |      "regime": "DPRS",
                                              |      "acknowledgementReference": "$acknowledgementReference",
                                              |      "requestParameters": [
                                              |        {
                                              |          "paramName": "REGIME",
                                              |          "paramValue": "DPRS"
                                              |        }
                                              |      ]
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
        "no match" in {
          stubFor(
            post(urlEqualTo(baseConnectorPath))
              .withRequestBody(equalToJson(s"""
                                              |{
                                              |  "registerWithIDRequest": {
                                              |    "requestCommon": {
                                              |      "receiptDate": "$currentDateAndTime",
                                              |      "regime": "DPRS",
                                              |      "acknowledgementReference": "$acknowledgementReference",
                                              |      "requestParameters": [
                                              |        {
                                              |          "paramName": "REGIME",
                                              |          "paramValue": "DPRS"
                                              |        }
                                              |      ]
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
                  .withStatus(NOT_FOUND)
                  .withBody("""
                              |{
                              |  "errorDetail": {
                              |    "timestamp": "2023-12-13T11:50:35Z",
                              |    "correlationId": "d102b24f-767c-4620-826b-d068f86e4abc",
                              |    "errorCode": "404",
                              |    "errorMessage": "Record not found",
                              |    "source": "journey-DPRS0102-service-camel",
                              |    "sourceFaultDetail": {
                              |      "detail": [
                              |        "Record not found"
                              |      ]
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
        "unauthorised" in {
          stubFor(
            post(urlEqualTo(baseConnectorPath))
              .withRequestBody(equalToJson(s"""
                                              |{
                                              |  "registerWithIDRequest": {
                                              |    "requestCommon": {
                                              |      "receiptDate": "$currentDateAndTime",
                                              |      "regime": "DPRS",
                                              |      "acknowledgementReference": "$acknowledgementReference",
                                              |      "requestParameters": [
                                              |        {
                                              |          "paramName": "REGIME",
                                              |          "paramValue": "DPRS"
                                              |        }
                                              |      ]
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
                  .withStatus(UNAUTHORIZED)
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
}
