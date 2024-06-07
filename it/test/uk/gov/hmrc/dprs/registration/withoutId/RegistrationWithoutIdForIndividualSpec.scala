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

package uk.gov.hmrc.dprs.registration.withoutId

import com.github.tomakehurst.wiremock.client.WireMock._
import play.api.http.Status._

class RegistrationWithoutIdForIndividualSpec extends BaseRegistrationWithoutIdSpec {

  "attempting to register without an ID, as an individual, when" - {
    "the request is" - {
      "valid, when" - {
        "the country is inside the UK or related territories" in {
          stubFor(
            post(urlEqualTo(baseConnectorPath))
              .withRequestBody(equalToJson(s"""
                                              |{
                                              |  "registerWithoutIDRequest": {
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
                                              |      "individual": {
                                              |        "firstName": "Patrick",
                                              |        "middleName": "John",
                                              |        "lastName": "Dyson",
                                              |        "dateOfBirth": "1970-10-04"
                                              |      },
                                              |      "address": {
                                              |        "addressLine1": "34 Park Lane",
                                              |        "addressLine2": "Building A",
                                              |        "addressLine3": "Suite 100",
                                              |        "addressLine4": "Manchester",
                                              |        "postalCode": "M54 1MQ",
                                              |        "countryCode": "GB"
                                              |      },
                                              |      "contactDetails": {
                                              |        "phoneNumber": "747663966",
                                              |        "mobileNumber": "38390756243",
                                              |        "faxNumber": "58371813020",
                                              |        "emailAddress": "Patrick.Dyson@example.com"
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
                               |  "registerWithoutIDResponse" : {
                               |    "responseCommon" : {
                               |      "status" : "OK",
                               |      "statusText" : "",
                               |      "processingDate" : "$currentDateAndTime",
                               |      "returnParameters" : [ {
                               |        "paramName" : "SAP_NUMBER",
                               |        "paramValue" : "1960629967"
                               |      } ]
                               |    },
                               |    "responseDetail" : {
                               |      "SAFEID" : "XE0000200775706",
                               |      "ARN" : "WARN3849921"
                               |    }
                               |  }
                               |}
                               |""".stripMargin)
              )
          )

          val response = wsClient
            .url(fullUrl("/registrations/withoutId/individual"))
            .withHttpHeaders(("Content-Type", "application/json"))
            .post("""
                    |{
                    |    "firstName": "Patrick",
                    |    "middleName": "John",
                    |    "lastName": "Dyson",
                    |    "dateOfBirth": "1970-10-04",
                    |    "address": {
                    |        "lineOne": "34 Park Lane",
                    |        "lineTwo": "Building A",
                    |        "lineThree": "Suite 100",
                    |        "lineFour": "Manchester",
                    |        "postalCode": "M54 1MQ",
                    |        "countryCode": "GB"
                    |    },
                    |    "contactDetails": {
                    |        "landline": "747663966",
                    |        "mobile": "38390756243",
                    |        "fax": "58371813020",
                    |        "emailAddress": "Patrick.Dyson@example.com"
                    |    }
                    |}
                    |""".stripMargin)
            .futureValue

          assertAsExpected(
            response = response,
            status = OK,
            jsonBodyOpt = Some(
              """
                |{
                |    "ids": [
                |        {
                |            "type": "ARN",
                |            "value": "WARN3849921"
                |        },
                |        {
                |            "type": "SAFE",
                |            "value": "XE0000200775706"
                |        },
                |        {
                |            "type": "SAP",
                |            "value": "1960629967"
                |        }
                |    ]
                |}
                |""".stripMargin
            )
          )
          verifyThatDownstreamApiWasCalled()

        }
        "the country is outside UK and related territories" in {
          stubFor(
            post(urlEqualTo(baseConnectorPath))
              .withRequestBody(equalToJson(s"""
                                              |{
                                              |  "registerWithoutIDRequest": {
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
                                              |      "individual": {
                                              |        "firstName": "Patrick",
                                              |        "middleName": "John",
                                              |        "lastName": "Dyson",
                                              |        "dateOfBirth": "1970-10-04"
                                              |      },
                                              |      "address": {
                                              |        "addressLine1": "78 Rue Marie De Médicis",
                                              |        "addressLine2": "Cambrai",
                                              |        "addressLine3": "Nord-Pas-de-Calais",
                                              |        "countryCode": "FR"
                                              |      },
                                              |      "contactDetails": {
                                              |        "phoneNumber": "747663966",
                                              |        "mobileNumber": "38390756243",
                                              |        "faxNumber": "58371813020",
                                              |        "emailAddress": "Patrick.Dyson@example.com"
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
                               |  "registerWithoutIDResponse" : {
                               |    "responseCommon" : {
                               |      "status" : "OK",
                               |      "statusText" : "",
                               |      "processingDate" : "$currentDateAndTime",
                               |      "returnParameters" : [ {
                               |        "paramName" : "SAP_NUMBER",
                               |        "paramValue" : "1960629967"
                               |      } ]
                               |    },
                               |    "responseDetail" : {
                               |      "SAFEID" : "XE0000200775706",
                               |      "ARN" : "WARN3849921"
                               |    }
                               |  }
                               |}
                               |""".stripMargin)
              )
          )

          val response = wsClient
            .url(fullUrl("/registrations/withoutId/individual"))
            .withHttpHeaders(("Content-Type", "application/json"))
            .post("""
                    |{
                    |    "firstName": "Patrick",
                    |    "middleName": "John",
                    |    "lastName": "Dyson",
                    |    "dateOfBirth": "1970-10-04",
                    |    "address": {
                    |        "lineOne": "78 Rue Marie De Médicis",
                    |        "lineTwo": "Cambrai",
                    |        "lineThree": "Nord-Pas-de-Calais",
                    |        "countryCode": "FR"
                    |    },
                    |    "contactDetails": {
                    |        "landline": "747663966",
                    |        "mobile": "38390756243",
                    |        "fax": "58371813020",
                    |        "emailAddress": "Patrick.Dyson@example.com"
                    |    }
                    |}
                    |""".stripMargin)
            .futureValue

          assertAsExpected(
            response = response,
            status = OK,
            jsonBodyOpt = Some(
              """
                |{
                |    "ids": [
                |        {
                |            "type": "ARN",
                |            "value": "WARN3849921"
                |        },
                |        {
                |            "type": "SAFE",
                |            "value": "XE0000200775706"
                |        },
                |        {
                |            "type": "SAP",
                |            "value": "1960629967"
                |        }
                |    ]
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
            post(urlEqualTo(baseConnectorPath))
              .withRequestBody(equalToJson(s"""
                                              |{
                                              |  "registerWithoutIDRequest": {
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
                                              |      "individual": {
                                              |        "firstName": "Patrick",
                                              |        "middleName": "John",
                                              |        "lastName": "Dyson",
                                              |        "dateOfBirth": "1970-10-04"
                                              |      },
                                              |      "address": {
                                              |        "addressLine1": "34 Park Lane",
                                              |        "addressLine2": "Building A",
                                              |        "addressLine3": "Suite 100",
                                              |        "addressLine4": "Manchester",
                                              |        "postalCode": "M54 1MQ",
                                              |        "countryCode": "GB"
                                              |      },
                                              |      "contactDetails": {
                                              |        "phoneNumber": "747663966",
                                              |        "mobileNumber": "38390756243",
                                              |        "faxNumber": "58371813020",
                                              |        "emailAddress": "Patrick.Dyson@example.com"
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
            .url(fullUrl("/registrations/withoutId/individual"))
            .withHttpHeaders(("Content-Type", "application/json"))
            .post("""
                    |{
                    |    "firstName": "Patrick",
                    |    "middleName": "John",
                    |    "lastName": "Dyson",
                    |    "dateOfBirth": "1970-10-04",
                    |    "address": {
                    |        "lineOne": "34 Park Lane",
                    |        "lineTwo": "Building A",
                    |        "lineThree": "Suite 100",
                    |        "lineFour": "Manchester",
                    |        "postalCode": "M54 1MQ",
                    |        "countryCode": "GB"
                    |    },
                    |    "contactDetails": {
                    |        "landline": "747663966",
                    |        "mobile": "38390756243",
                    |        "fax": "58371813020",
                    |        "emailAddress": "Patrick.Dyson@example.com"
                    |    }
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
            post(urlEqualTo(baseConnectorPath))
              .withRequestBody(equalToJson(s"""
                                              {
                                              |  "registerWithoutIDRequest": {
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
                                              |      "individual": {
                                              |        "firstName": "Patrick",
                                              |        "middleName": "John",
                                              |        "lastName": "Dyson",
                                              |        "dateOfBirth": "1970-10-04"
                                              |      },
                                              |      "address": {
                                              |        "addressLine1": "34 Park Lane",
                                              |        "addressLine2": "Building A",
                                              |        "addressLine3": "Suite 100",
                                              |        "addressLine4": "Manchester",
                                              |        "postalCode": "M54 1MQ",
                                              |        "countryCode": "GB"
                                              |      },
                                              |      "contactDetails": {
                                              |        "phoneNumber": "747663966",
                                              |        "mobileNumber": "38390756243",
                                              |        "faxNumber": "58371813020",
                                              |        "emailAddress": "Patrick.Dyson@example.com"
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
            .url(fullUrl("/registrations/withoutId/individual"))
            .withHttpHeaders(("Content-Type", "application/json"))
            .post("""
                    |{
                    |    "firstName": "Patrick",
                    |    "middleName": "John",
                    |    "lastName": "Dyson",
                    |    "dateOfBirth": "1970-10-04",
                    |    "address": {
                    |        "lineOne": "34 Park Lane",
                    |        "lineTwo": "Building A",
                    |        "lineThree": "Suite 100",
                    |        "lineFour": "Manchester",
                    |        "postalCode": "M54 1MQ",
                    |        "countryCode": "GB"
                    |    },
                    |    "contactDetails": {
                    |        "landline": "747663966",
                    |        "mobile": "38390756243",
                    |        "fax": "58371813020",
                    |        "emailAddress": "Patrick.Dyson@example.com"
                    |    }
                    |}
                    |""".stripMargin)
            .futureValue

          assertAsExpected(response, INTERNAL_SERVER_ERROR)
          verifyThatDownstreamApiWasCalled()
        }
        "service unavailable" in {
          stubFor(
            post(urlEqualTo(baseConnectorPath))
              .withRequestBody(equalToJson(s"""
                                              |{
                                              |  "registerWithoutIDRequest": {
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
                                              |      "individual": {
                                              |        "firstName": "Patrick",
                                              |        "middleName": "John",
                                              |        "lastName": "Dyson",
                                              |        "dateOfBirth": "1970-10-04"
                                              |      },
                                              |      "address": {
                                              |        "addressLine1": "34 Park Lane",
                                              |        "addressLine2": "Building A",
                                              |        "addressLine3": "Suite 100",
                                              |        "addressLine4": "Manchester",
                                              |        "postalCode": "M54 1MQ",
                                              |        "countryCode": "GB"
                                              |      },
                                              |      "contactDetails": {
                                              |        "phoneNumber": "747663966",
                                              |        "mobileNumber": "38390756243",
                                              |        "faxNumber": "58371813020",
                                              |        "emailAddress": "Patrick.Dyson@example.com"
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
            .url(fullUrl("/registrations/withoutId/individual"))
            .withHttpHeaders(("Content-Type", "application/json"))
            .post("""
                    |{
                    |    "firstName": "Patrick",
                    |    "middleName": "John",
                    |    "lastName": "Dyson",
                    |    "dateOfBirth": "1970-10-04",
                    |    "address": {
                    |        "lineOne": "34 Park Lane",
                    |        "lineTwo": "Building A",
                    |        "lineThree": "Suite 100",
                    |        "lineFour": "Manchester",
                    |        "postalCode": "M54 1MQ",
                    |        "countryCode": "GB"
                    |    },
                    |    "contactDetails": {
                    |        "landline": "747663966",
                    |        "mobile": "38390756243",
                    |        "fax": "58371813020",
                    |        "emailAddress": "Patrick.Dyson@example.com"
                    |    }
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
            post(urlEqualTo(baseConnectorPath))
              .withRequestBody(equalToJson(s"""
                                              |{
                                              |  "registerWithoutIDRequest": {
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
                                              |      "individual": {
                                              |        "firstName": "Patrick",
                                              |        "middleName": "John",
                                              |        "lastName": "Dyson",
                                              |        "dateOfBirth": "1970-10-04"
                                              |      },
                                              |      "address": {
                                              |        "addressLine1": "34 Park Lane",
                                              |        "addressLine2": "Building A",
                                              |        "addressLine3": "Suite 100",
                                              |        "addressLine4": "Manchester",
                                              |        "postalCode": "M54 1MQ",
                                              |        "countryCode": "GB"
                                              |      },
                                              |      "contactDetails": {
                                              |        "phoneNumber": "747663966",
                                              |        "mobileNumber": "38390756243",
                                              |        "faxNumber": "58371813020",
                                              |        "emailAddress": "Patrick.Dyson@example.com"
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
            .url(fullUrl("/registrations/withoutId/individual"))
            .withHttpHeaders(("Content-Type", "application/json"))
            .post("""
                    |{
                    |    "firstName": "Patrick",
                    |    "middleName": "John",
                    |    "lastName": "Dyson",
                    |    "dateOfBirth": "1970-10-04",
                    |    "address": {
                    |        "lineOne": "34 Park Lane",
                    |        "lineTwo": "Building A",
                    |        "lineThree": "Suite 100",
                    |        "lineFour": "Manchester",
                    |        "postalCode": "M54 1MQ",
                    |        "countryCode": "GB"
                    |    },
                    |    "contactDetails": {
                    |        "landline": "747663966",
                    |        "mobile": "38390756243",
                    |        "fax": "58371813020",
                    |        "emailAddress": "Patrick.Dyson@example.com"
                    |    }
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
            post(urlEqualTo(baseConnectorPath))
              .withRequestBody(equalToJson(s"""
                                              |{
                                              |  "registerWithoutIDRequest": {
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
                                              |      "individual": {
                                              |        "firstName": "Patrick",
                                              |        "middleName": "John",
                                              |        "lastName": "Dyson",
                                              |        "dateOfBirth": "1970-10-04"
                                              |      },
                                              |      "address": {
                                              |        "addressLine1": "34 Park Lane",
                                              |        "addressLine2": "Building A",
                                              |        "addressLine3": "Suite 100",
                                              |        "addressLine4": "Manchester",
                                              |        "postalCode": "M54 1MQ",
                                              |        "countryCode": "GB"
                                              |      },
                                              |      "contactDetails": {
                                              |        "phoneNumber": "747663966",
                                              |        "mobileNumber": "38390756243",
                                              |        "faxNumber": "58371813020",
                                              |        "emailAddress": "Patrick.Dyson@example.com"
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
            .url(fullUrl("/registrations/withoutId/individual"))
            .withHttpHeaders(("Content-Type", "application/json"))
            .post("""
                    |{
                    |    "firstName": "Patrick",
                    |    "middleName": "John",
                    |    "lastName": "Dyson",
                    |    "dateOfBirth": "1970-10-04",
                    |    "address": {
                    |        "lineOne": "34 Park Lane",
                    |        "lineTwo": "Building A",
                    |        "lineThree": "Suite 100",
                    |        "lineFour": "Manchester",
                    |        "postalCode": "M54 1MQ",
                    |        "countryCode": "GB"
                    |    },
                    |    "contactDetails": {
                    |        "landline": "747663966",
                    |        "mobile": "38390756243",
                    |        "fax": "58371813020",
                    |        "emailAddress": "Patrick.Dyson@example.com"
                    |    }
                    |}
                    |""".stripMargin)
            .futureValue

          assertAsExpected(response, INTERNAL_SERVER_ERROR)
          verifyThatDownstreamApiWasCalled()
        }
      }
      "invalid, specifically:" - {
        "the first name is" - {
          "absent" in {
            val response = wsClient
              .url(fullUrl("/registrations/withoutId/individual"))
              .withHttpHeaders(("Content-Type", "application/json"))
              .post("""
                      |{
                      |    "middleName": "John",
                      |    "lastName": "Dyson",
                      |    "dateOfBirth": "1970-10-04",
                      |    "address": {
                      |        "lineOne": "34 Park Lane",
                      |        "lineTwo": "Building A",
                      |        "lineThree": "Suite 100",
                      |        "lineFour": "Manchester",
                      |        "postalCode": "M54 1MQ",
                      |        "countryCode": "GB"
                      |    },
                      |    "contactDetails": {
                      |        "landline": "747663966",
                      |        "mobile": "38390756243",
                      |        "fax": "58371813020",
                      |        "emailAddress": "Patrick.Dyson@example.com"
                      |    }
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
              .url(fullUrl("/registrations/withoutId/individual"))
              .withHttpHeaders(("Content-Type", "application/json"))
              .post("""
                      |{
                      |    "firstName": "",
                      |    "middleName": "John",
                      |    "lastName": "Dyson",
                      |    "dateOfBirth": "1970-10-04",
                      |    "address": {
                      |        "lineOne": "34 Park Lane",
                      |        "lineTwo": "Building A",
                      |        "lineThree": "Suite 100",
                      |        "lineFour": "Manchester",
                      |        "postalCode": "M54 1MQ",
                      |        "countryCode": "GB"
                      |    },
                      |    "contactDetails": {
                      |        "landline": "747663966",
                      |        "mobile": "38390756243",
                      |        "fax": "58371813020",
                      |        "emailAddress": "Patrick.Dyson@example.com"
                      |    }
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
              .url(fullUrl("/registrations/withoutId/individual"))
              .withHttpHeaders(("Content-Type", "application/json"))
              .post("""
                      |{
                      |    "firstName": "Patrick Alexander John Fitzpatrick James",
                      |    "middleName": "John",
                      |    "lastName": "Dyson",
                      |    "dateOfBirth": "1970-10-04",
                      |    "address": {
                      |        "lineOne": "34 Park Lane",
                      |        "lineTwo": "Building A",
                      |        "lineThree": "Suite 100",
                      |        "lineFour": "Manchester",
                      |        "postalCode": "M54 1MQ",
                      |        "countryCode": "GB"
                      |    },
                      |    "contactDetails": {
                      |        "landline": "747663966",
                      |        "mobile": "38390756243",
                      |        "fax": "58371813020",
                      |        "emailAddress": "Patrick.Dyson@example.com"
                      |    }
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
              .url(fullUrl("/registrations/withoutId/individual"))
              .withHttpHeaders(("Content-Type", "application/json"))
              .post("""
                      |{
                      |    "firstName": "Patrick",
                      |    "middleName": "",
                      |    "lastName": "Dyson",
                      |    "dateOfBirth": "1970-10-04",
                      |    "address": {
                      |        "lineOne": "34 Park Lane",
                      |        "lineTwo": "Building A",
                      |        "lineThree": "Suite 100",
                      |        "lineFour": "Manchester",
                      |        "postalCode": "M54 1MQ",
                      |        "countryCode": "GB"
                      |    },
                      |    "contactDetails": {
                      |        "landline": "747663966",
                      |        "mobile": "38390756243",
                      |        "fax": "58371813020",
                      |        "emailAddress": "Patrick.Dyson@example.com"
                      |    }
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
              .url(fullUrl("/registrations/withoutId/individual"))
              .withHttpHeaders(("Content-Type", "application/json"))
              .post("""
                      |{
                      |    "firstName": "Patrick",
                      |    "middleName": "Alexander John Fitzpatrick James Edward",
                      |    "lastName": "Dyson",
                      |    "dateOfBirth": "1970-10-04",
                      |    "address": {
                      |        "lineOne": "34 Park Lane",
                      |        "lineTwo": "Building A",
                      |        "lineThree": "Suite 100",
                      |        "lineFour": "Manchester",
                      |        "postalCode": "M54 1MQ",
                      |        "countryCode": "GB"
                      |    },
                      |    "contactDetails": {
                      |        "landline": "747663966",
                      |        "mobile": "38390756243",
                      |        "fax": "58371813020",
                      |        "emailAddress": "Patrick.Dyson@example.com"
                      |    }
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
              .url(fullUrl("/registrations/withoutId/individual"))
              .withHttpHeaders(("Content-Type", "application/json"))
              .post("""
                      |{
                      |    "firstName": "Patrick",
                      |    "middleName": "John",
                      |    "dateOfBirth": "1970-10-04",
                      |    "address": {
                      |        "lineOne": "34 Park Lane",
                      |        "lineTwo": "Building A",
                      |        "lineThree": "Suite 100",
                      |        "lineFour": "Manchester",
                      |        "postalCode": "M54 1MQ",
                      |        "countryCode": "GB"
                      |    },
                      |    "contactDetails": {
                      |        "landline": "747663966",
                      |        "mobile": "38390756243",
                      |        "fax": "58371813020",
                      |        "emailAddress": "Patrick.Dyson@example.com"
                      |    }
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
              .url(fullUrl("/registrations/withoutId/individual"))
              .withHttpHeaders(("Content-Type", "application/json"))
              .post("""
                      |{
                      |    "firstName": "Patrick",
                      |    "middleName": "John",
                      |    "lastName": "",
                      |    "dateOfBirth": "1970-10-04",
                      |    "address": {
                      |        "lineOne": "34 Park Lane",
                      |        "lineTwo": "Building A",
                      |        "lineThree": "Suite 100",
                      |        "lineFour": "Manchester",
                      |        "postalCode": "M54 1MQ",
                      |        "countryCode": "GB"
                      |    },
                      |    "contactDetails": {
                      |        "landline": "747663966",
                      |        "mobile": "38390756243",
                      |        "fax": "58371813020",
                      |        "emailAddress": "Patrick.Dyson@example.com"
                      |    }
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
              .url(fullUrl("/registrations/withoutId/individual"))
              .withHttpHeaders(("Content-Type", "application/json"))
              .post("""
                      |{
                      |    "firstName": "Patrick",
                      |    "middleName": "John",
                      |    "lastName": "Alexander III, Earl Of Somewhere Else",
                      |    "dateOfBirth": "1970-10-04",
                      |    "address": {
                      |        "lineOne": "34 Park Lane",
                      |        "lineTwo": "Building A",
                      |        "lineThree": "Suite 100",
                      |        "lineFour": "Manchester",
                      |        "postalCode": "M54 1MQ",
                      |        "countryCode": "GB"
                      |    },
                      |    "contactDetails": {
                      |        "landline": "747663966",
                      |        "mobile": "38390756243",
                      |        "fax": "58371813020",
                      |        "emailAddress": "Patrick.Dyson@example.com"
                      |    }
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
              .url(fullUrl("/registrations/withoutId/individual"))
              .withHttpHeaders(("Content-Type", "application/json"))
              .post("""
                      |{
                      |    "firstName": "Patrick",
                      |    "middleName": "John",
                      |    "lastName": "Dyson",
                      |    "address": {
                      |        "lineOne": "34 Park Lane",
                      |        "lineTwo": "Building A",
                      |        "lineThree": "Suite 100",
                      |        "lineFour": "Manchester",
                      |        "postalCode": "M54 1MQ",
                      |        "countryCode": "GB"
                      |    },
                      |    "contactDetails": {
                      |        "landline": "747663966",
                      |        "mobile": "38390756243",
                      |        "fax": "58371813020",
                      |        "emailAddress": "Patrick.Dyson@example.com"
                      |    }
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
              .url(fullUrl("/registrations/withoutId/individual"))
              .withHttpHeaders(("Content-Type", "application/json"))
              .post("""
                      |{
                      |    "firstName": "Patrick",
                      |    "middleName": "John",
                      |    "lastName": "Dyson",
                      |    "dateOfBirth": "",
                      |    "address": {
                      |        "lineOne": "34 Park Lane",
                      |        "lineTwo": "Building A",
                      |        "lineThree": "Suite 100",
                      |        "lineFour": "Manchester",
                      |        "postalCode": "M54 1MQ",
                      |        "countryCode": "GB"
                      |    },
                      |    "contactDetails": {
                      |        "landline": "747663966",
                      |        "mobile": "38390756243",
                      |        "fax": "58371813020",
                      |        "emailAddress": "Patrick.Dyson@example.com"
                      |    }
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
              .url(fullUrl("/registrations/withoutId/individual"))
              .withHttpHeaders(("Content-Type", "application/json"))
              .post("""
                      |{
                      |    "firstName": "Patrick",
                      |    "middleName": "John",
                      |    "lastName": "Dyson",
                      |    "dateOfBirth": "10-04-1970",
                      |    "address": {
                      |        "lineOne": "34 Park Lane",
                      |        "lineTwo": "Building A",
                      |        "lineThree": "Suite 100",
                      |        "lineFour": "Manchester",
                      |        "postalCode": "M54 1MQ",
                      |        "countryCode": "GB"
                      |    },
                      |    "contactDetails": {
                      |        "landline": "747663966",
                      |        "mobile": "38390756243",
                      |        "fax": "58371813020",
                      |        "emailAddress": "Patrick.Dyson@example.com"
                      |    }
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
              .url(fullUrl("/registrations/withoutId/individual"))
              .withHttpHeaders(("Content-Type", "application/json"))
              .post("""
                      |{
                      |    "firstName": "Patrick",
                      |    "middleName": "John",
                      |    "lastName": "Dyson",
                      |    "dateOfBirth": "1977-02-29",
                      |    "address": {
                      |        "lineOne": "34 Park Lane",
                      |        "lineTwo": "Building A",
                      |        "lineThree": "Suite 100",
                      |        "lineFour": "Manchester",
                      |        "postalCode": "M54 1MQ",
                      |        "countryCode": "GB"
                      |    },
                      |    "contactDetails": {
                      |        "landline": "747663966",
                      |        "mobile": "38390756243",
                      |        "fax": "58371813020",
                      |        "emailAddress": "Patrick.Dyson@example.com"
                      |    }
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
        "in the address, the" - {
          "first line is" - {
            "absent" in {
              val response = wsClient
                .url(fullUrl("/registrations/withoutId/individual"))
                .withHttpHeaders(("Content-Type", "application/json"))
                .post("""
                        |{
                        |    "firstName": "Patrick",
                        |    "middleName": "John",
                        |    "lastName": "Dyson",
                        |    "dateOfBirth": "1970-10-04",
                        |    "address": {
                        |        "lineTwo": "Building A",
                        |        "lineThree": "Suite 100",
                        |        "lineFour": "Manchester",
                        |        "postalCode": "M54 1MQ",
                        |        "countryCode": "GB"
                        |    },
                        |    "contactDetails": {
                        |        "landline": "747663966",
                        |        "mobile": "38390756243",
                        |        "fax": "58371813020",
                        |        "emailAddress": "Patrick.Dyson@example.com"
                        |    }
                        |}
                        |""".stripMargin)
                .futureValue

              assertAsExpected(
                response,
                BAD_REQUEST,
                Some("""
                       |[
                       |  {
                       |    "code": "invalid-address-line-one"
                       |  }
                       |]
                       |""".stripMargin)
              )
              verifyThatDownstreamApiWasNotCalled()
            }
            "blank" in {
              val response = wsClient
                .url(fullUrl("/registrations/withoutId/individual"))
                .withHttpHeaders(("Content-Type", "application/json"))
                .post("""
                        |{
                        |    "firstName": "Patrick",
                        |    "middleName": "John",
                        |    "lastName": "Dyson",
                        |    "dateOfBirth": "1970-10-04",
                        |    "address": {
                        |        "lineOne": "",
                        |        "lineTwo": "Building A",
                        |        "lineThree": "Suite 100",
                        |        "lineFour": "Manchester",
                        |        "postalCode": "M54 1MQ",
                        |        "countryCode": "GB"
                        |    },
                        |    "contactDetails": {
                        |        "landline": "747663966",
                        |        "mobile": "38390756243",
                        |        "fax": "58371813020",
                        |        "emailAddress": "Patrick.Dyson@example.com"
                        |    }
                        |}
                        |""".stripMargin)
                .futureValue

              assertAsExpected(
                response,
                BAD_REQUEST,
                Some("""
                       |[
                       |  {
                       |    "code": "invalid-address-line-one"
                       |  }
                       |]
                       |""".stripMargin)
              )
              verifyThatDownstreamApiWasNotCalled()
            }
            "too long" in {
              val response = wsClient
                .url(fullUrl("/registrations/withoutId/individual"))
                .withHttpHeaders(("Content-Type", "application/json"))
                .post("""
                        |{
                        |    "firstName": "Patrick",
                        |    "middleName": "John",
                        |    "lastName": "Dyson",
                        |    "dateOfBirth": "1970-10-04",
                        |    "address": {
                        |        "lineOne": "6000-6600 Great Peter Boulevard North",
                        |        "lineTwo": "Building A",
                        |        "lineThree": "Suite 100",
                        |        "lineFour": "Manchester",
                        |        "postalCode": "M54 1MQ",
                        |        "countryCode": "GB"
                        |    },
                        |    "contactDetails": {
                        |        "landline": "747663966",
                        |        "mobile": "38390756243",
                        |        "fax": "58371813020",
                        |        "emailAddress": "Patrick.Dyson@example.com"
                        |    }
                        |}
                        |""".stripMargin)
                .futureValue

              assertAsExpected(
                response,
                BAD_REQUEST,
                Some("""
                       |[
                       |  {
                       |    "code": "invalid-address-line-one"
                       |  }
                       |]
                       |""".stripMargin)
              )
              verifyThatDownstreamApiWasNotCalled()
            }
          }
          "second line is" - {
            "absent" in {
              val response = wsClient
                .url(fullUrl("/registrations/withoutId/individual"))
                .withHttpHeaders(("Content-Type", "application/json"))
                .post("""
                        |{
                        |    "firstName": "Patrick",
                        |    "middleName": "John",
                        |    "lastName": "Dyson",
                        |    "dateOfBirth": "1970-10-04",
                        |    "address": {
                        |        "lineOne": "34 Park Lane",
                        |        "lineThree": "Suite 100",
                        |        "lineFour": "Manchester",
                        |        "postalCode": "M54 1MQ",
                        |        "countryCode": "GB"
                        |    },
                        |    "contactDetails": {
                        |        "landline": "747663966",
                        |        "mobile": "38390756243",
                        |        "fax": "58371813020",
                        |        "emailAddress": "Patrick.Dyson@example.com"
                        |    }
                        |}
                        |""".stripMargin)
                .futureValue

              assertAsExpected(
                response,
                BAD_REQUEST,
                Some("""
                       |[
                       |  {
                       |    "code": "invalid-address-line-two"
                       |  }
                       |]
                       |""".stripMargin)
              )
              verifyThatDownstreamApiWasNotCalled()
            }
            "blank" in {
              val response = wsClient
                .url(fullUrl("/registrations/withoutId/individual"))
                .withHttpHeaders(("Content-Type", "application/json"))
                .post("""
                        |{
                        |    "firstName": "Patrick",
                        |    "middleName": "John",
                        |    "lastName": "Dyson",
                        |    "dateOfBirth": "1970-10-04",
                        |    "address": {
                        |        "lineOne": "34 Park Lane",
                        |        "lineTwo": "",
                        |        "lineThree": "Suite 100",
                        |        "lineFour": "Manchester",
                        |        "postalCode": "M54 1MQ",
                        |        "countryCode": "GB"
                        |    },
                        |    "contactDetails": {
                        |        "landline": "747663966",
                        |        "mobile": "38390756243",
                        |        "fax": "58371813020",
                        |        "emailAddress": "Patrick.Dyson@example.com"
                        |    }
                        |}
                        |""".stripMargin)
                .futureValue

              assertAsExpected(
                response,
                BAD_REQUEST,
                Some("""
                       |[
                       |  {
                       |    "code": "invalid-address-line-two"
                       |  }
                       |]
                       |""".stripMargin)
              )
              verifyThatDownstreamApiWasNotCalled()
            }
            "too long" in {
              val response = wsClient
                .url(fullUrl("/registrations/withoutId/individual"))
                .withHttpHeaders(("Content-Type", "application/json"))
                .post("""
                        |{
                        |    "firstName": "Patrick",
                        |    "middleName": "John",
                        |    "lastName": "Dyson",
                        |    "dateOfBirth": "1970-10-04",
                        |    "address": {
                        |        "lineOne": "34 Park Lane",
                        |        "lineTwo": "Building Number Two, Northwest Corner",
                        |        "lineThree": "Suite 100",
                        |        "lineFour": "Manchester",
                        |        "postalCode": "M54 1MQ",
                        |        "countryCode": "GB"
                        |    },
                        |    "contactDetails": {
                        |        "landline": "747663966",
                        |        "mobile": "38390756243",
                        |        "fax": "58371813020",
                        |        "emailAddress": "Patrick.Dyson@example.com"
                        |    }
                        |}
                        |""".stripMargin)
                .futureValue

              assertAsExpected(
                response,
                BAD_REQUEST,
                Some("""
                       |[
                       |  {
                       |    "code": "invalid-address-line-two"
                       |  }
                       |]
                       |""".stripMargin)
              )
              verifyThatDownstreamApiWasNotCalled()
            }
          }
          "third line is" - {
            "absent" in {
              val response = wsClient
                .url(fullUrl("/registrations/withoutId/individual"))
                .withHttpHeaders(("Content-Type", "application/json"))
                .post("""
                        |{
                        |    "firstName": "Patrick",
                        |    "middleName": "John",
                        |    "lastName": "Dyson",
                        |    "dateOfBirth": "1970-10-04",
                        |    "address": {
                        |        "lineOne": "34 Park Lane",
                        |        "lineTwo": "Building A",
                        |        "lineFour": "Manchester",
                        |        "postalCode": "M54 1MQ",
                        |        "countryCode": "GB"
                        |    },
                        |    "contactDetails": {
                        |        "landline": "747663966",
                        |        "mobile": "38390756243",
                        |        "fax": "58371813020",
                        |        "emailAddress": "Patrick.Dyson@example.com"
                        |    }
                        |}
                        |""".stripMargin)
                .futureValue

              assertAsExpected(
                response,
                BAD_REQUEST,
                Some("""
                       |[
                       |  {
                       |    "code": "invalid-address-line-three"
                       |  }
                       |]
                       |""".stripMargin)
              )
              verifyThatDownstreamApiWasNotCalled()
            }
            "blank" in {
              val response = wsClient
                .url(fullUrl("/registrations/withoutId/individual"))
                .withHttpHeaders(("Content-Type", "application/json"))
                .post("""
                        |{
                        |    "firstName": "Patrick",
                        |    "middleName": "John",
                        |    "lastName": "Dyson",
                        |    "dateOfBirth": "1970-10-04",
                        |    "address": {
                        |        "lineOne": "34 Park Lane",
                        |        "lineTwo": "Building A",
                        |        "lineThree": "",
                        |        "lineFour": "Manchester",
                        |        "postalCode": "M54 1MQ",
                        |        "countryCode": "GB"
                        |    },
                        |    "contactDetails": {
                        |        "landline": "747663966",
                        |        "mobile": "38390756243",
                        |        "fax": "58371813020",
                        |        "emailAddress": "Patrick.Dyson@example.com"
                        |    }
                        |}
                        |""".stripMargin)
                .futureValue

              assertAsExpected(
                response,
                BAD_REQUEST,
                Some("""
                       |[
                       |  {
                       |    "code": "invalid-address-line-three"
                       |  }
                       |]
                       |""".stripMargin)
              )
              verifyThatDownstreamApiWasNotCalled()
            }
            "too long" in {
              val response = wsClient
                .url(fullUrl("/registrations/withoutId/individual"))
                .withHttpHeaders(("Content-Type", "application/json"))
                .post("""
                          |{
                          |    "firstName": "Patrick",
                          |    "middleName": "John",
                          |    "lastName": "Dyson",
                          |    "dateOfBirth": "1970-10-04",
                          |    "address": {
                          |        "lineOne": "34 Park Lane",
                          |        "lineTwo": "Building A",
                          |        "lineThree": "Suite 100, formerly the Presidential Suite",
                          |        "lineFour": "Manchester",
                          |        "postalCode": "M54 1MQ",
                          |        "countryCode": "GB"
                          |    },
                          |    "contactDetails": {
                          |        "landline": "747663966",
                          |        "mobile": "38390756243",
                          |        "fax": "58371813020",
                          |        "emailAddress": "Patrick.Dyson@example.com"
                          |    }
                          |}
                          |""".stripMargin)
                .futureValue

              assertAsExpected(
                response,
                BAD_REQUEST,
                Some("""
                         |[
                         |  {
                         |    "code": "invalid-address-line-three"
                         |  }
                         |]
                         |""".stripMargin)
              )
              verifyThatDownstreamApiWasNotCalled()

            }
          }
          "fourth line is" - {
            "blank" in {
              val response = wsClient
                .url(fullUrl("/registrations/withoutId/individual"))
                .withHttpHeaders(("Content-Type", "application/json"))
                .post("""
                        |{
                        |    "firstName": "Patrick",
                        |    "middleName": "John",
                        |    "lastName": "Dyson",
                        |    "dateOfBirth": "1970-10-04",
                        |    "address": {
                        |        "lineOne": "34 Park Lane",
                        |        "lineTwo": "Building A",
                        |        "lineThree": "Suite 100",
                        |        "lineFour": "",
                        |        "postalCode": "M54 1MQ",
                        |        "countryCode": "GB"
                        |    },
                        |    "contactDetails": {
                        |        "landline": "747663966",
                        |        "mobile": "38390756243",
                        |        "fax": "58371813020",
                        |        "emailAddress": "Patrick.Dyson@example.com"
                        |    }
                        |}
                        |""".stripMargin)
                .futureValue

              assertAsExpected(
                response,
                BAD_REQUEST,
                Some("""
                       |[
                       |  {
                       |    "code": "invalid-address-line-four"
                       |  }
                       |]
                       |""".stripMargin)
              )
              verifyThatDownstreamApiWasNotCalled()
            }
            "too long" in {
              val response = wsClient
                .url(fullUrl("/registrations/withoutId/individual"))
                .withHttpHeaders(("Content-Type", "application/json"))
                .post("""
                        |{
                        |    "firstName": "Patrick",
                        |    "middleName": "John",
                        |    "lastName": "Dyson",
                        |    "dateOfBirth": "1970-10-04",
                        |    "address": {
                        |        "lineOne": "34 Park Lane",
                        |        "lineTwo": "Building A",
                        |        "lineThree": "Suite 100",
                        |        "lineFour": "Manchester, United Kingdom of Great Britain and Northern Ireland",
                        |        "postalCode": "M54 1MQ",
                        |        "countryCode": "GB"
                        |    },
                        |    "contactDetails": {
                        |        "landline": "747663966",
                        |        "mobile": "38390756243",
                        |        "fax": "58371813020",
                        |        "emailAddress": "Patrick.Dyson@example.com"
                        |    }
                        |}
                        |""".stripMargin)
                .futureValue

              assertAsExpected(
                response,
                BAD_REQUEST,
                Some("""
                       |[
                       |  {
                       |    "code": "invalid-address-line-four"
                       |  }
                       |]
                       |""".stripMargin)
              )
              verifyThatDownstreamApiWasNotCalled()
            }
          }
          "country code is" - {
            "absent" in {
              val response = wsClient
                .url(fullUrl("/registrations/withoutId/individual"))
                .withHttpHeaders(("Content-Type", "application/json"))
                .post("""
                        |{
                        |    "firstName": "Patrick",
                        |    "middleName": "John",
                        |    "lastName": "Dyson",
                        |    "dateOfBirth": "1970-10-04",
                        |    "address": {
                        |        "lineOne": "34 Park Lane",
                        |        "lineTwo": "Building A",
                        |        "lineThree": "Suite 100",
                        |        "lineFour": "Manchester",
                        |        "postalCode": "M54 1MQ"
                        |    },
                        |    "contactDetails": {
                        |        "landline": "747663966",
                        |        "mobile": "38390756243",
                        |        "fax": "58371813020",
                        |        "emailAddress": "Patrick.Dyson@example.com"
                        |    }
                        |}
                        |""".stripMargin)
                .futureValue

              assertAsExpected(
                response,
                BAD_REQUEST,
                Some("""
                       |[
                       |  {
                       |    "code": "invalid-address-country-code"
                       |  }
                       |]
                       |""".stripMargin)
              )
              verifyThatDownstreamApiWasNotCalled()
            }
            "blank" in {
              val response = wsClient
                .url(fullUrl("/registrations/withoutId/individual"))
                .withHttpHeaders(("Content-Type", "application/json"))
                .post("""
                        |{
                        |    "firstName": "Patrick",
                        |    "middleName": "John",
                        |    "lastName": "Dyson",
                        |    "dateOfBirth": "1970-10-04",
                        |    "address": {
                        |        "lineOne": "34 Park Lane",
                        |        "lineTwo": "Building A",
                        |        "lineThree": "Suite 100",
                        |        "lineFour": "Manchester",
                        |        "postalCode": "M54 1MQ",
                        |        "countryCode": ""
                        |    },
                        |    "contactDetails": {
                        |        "landline": "747663966",
                        |        "mobile": "38390756243",
                        |        "fax": "58371813020",
                        |        "emailAddress": "Patrick.Dyson@example.com"
                        |    }
                        |}
                        |""".stripMargin)
                .futureValue

              assertAsExpected(
                response,
                BAD_REQUEST,
                Some("""
                       |[
                       |  {
                       |    "code": "invalid-address-country-code"
                       |  }
                       |]
                       |""".stripMargin)
              )
              verifyThatDownstreamApiWasNotCalled()
            }
            "too long" in {
              val response = wsClient
                .url(fullUrl("/registrations/withoutId/individual"))
                .withHttpHeaders(("Content-Type", "application/json"))
                .post("""
                        |{
                        |    "firstName": "Patrick",
                        |    "middleName": "John",
                        |    "lastName": "Dyson",
                        |    "dateOfBirth": "1970-10-04",
                        |    "address": {
                        |        "lineOne": "34 Park Lane",
                        |        "lineTwo": "Building A",
                        |        "lineThree": "Suite 100",
                        |        "lineFour": "Manchester",
                        |        "postalCode": "M54 1MQ",
                        |        "countryCode": "GBR"
                        |    },
                        |    "contactDetails": {
                        |        "landline": "747663966",
                        |        "mobile": "38390756243",
                        |        "fax": "58371813020",
                        |        "emailAddress": "Patrick.Dyson@example.com"
                        |    }
                        |}
                        |""".stripMargin)
                .futureValue

              assertAsExpected(
                response,
                BAD_REQUEST,
                Some("""
                       |[
                       |  {
                       |    "code": "invalid-address-country-code"
                       |  }
                       |]
                       |""".stripMargin)
              )
              verifyThatDownstreamApiWasNotCalled()
            }
            "unrecognised" in {
              val response = wsClient
                .url(fullUrl("/registrations/withoutId/individual"))
                .withHttpHeaders(("Content-Type", "application/json"))
                .post("""
                          |{
                          |    "firstName": "Patrick",
                          |    "middleName": "John",
                          |    "lastName": "Dyson",
                          |    "dateOfBirth": "1970-10-04",
                          |    "address": {
                          |        "lineOne": "34 Park Lane",
                          |        "lineTwo": "Building A",
                          |        "lineThree": "Suite 100",
                          |        "lineFour": "Manchester",
                          |        "postalCode": "M54 1MQ",
                          |        "countryCode": "XX"
                          |    },
                          |    "contactDetails": {
                          |        "landline": "747663966",
                          |        "mobile": "38390756243",
                          |        "fax": "58371813020",
                          |        "emailAddress": "Patrick.Dyson@example.com"
                          |    }
                          |}
                          |""".stripMargin)
                .futureValue

              assertAsExpected(
                response,
                BAD_REQUEST,
                Some("""
                         |[
                         |  {
                         |    "code": "invalid-address-country-code"
                         |  }
                         |]
                         |""".stripMargin)
              )
              verifyThatDownstreamApiWasNotCalled()

            }
          }
          "postal code is" - {
            "expected but absent" in {
              val response = wsClient
                .url(fullUrl("/registrations/withoutId/individual"))
                .withHttpHeaders(("Content-Type", "application/json"))
                .post(
                  s"""
                       |{
                       |    "firstName": "Patrick",
                       |    "middleName": "John",
                       |    "lastName": "Dyson",
                       |    "dateOfBirth": "1970-10-04",
                       |    "address": {
                       |        "lineOne": "34 Park Lane",
                       |        "lineTwo": "Building A",
                       |        "lineThree": "Suite 100",
                       |        "lineFour": "Manchester",
                       |        "countryCode": "GB"
                       |    },
                       |    "contactDetails": {
                       |        "landline": "747663966",
                       |        "mobile": "38390756243",
                       |        "fax": "58371813020",
                       |        "emailAddress": "Patrick.Dyson@example.com"
                       |    }
                       |}
                       |""".stripMargin
                )
                .futureValue

              assertAsExpected(
                response,
                BAD_REQUEST,
                Some("""
                      |[
                      |  {
                      |    "code": "invalid-address-postal-code"
                      |  }
                      |]
                      |""".stripMargin)
              )
              verifyThatDownstreamApiWasNotCalled()
            }
            "blank" in {
              val response = wsClient
                .url(fullUrl("/registrations/withoutId/individual"))
                .withHttpHeaders(("Content-Type", "application/json"))
                .post("""
                        |{
                        |    "firstName": "Patrick",
                        |    "middleName": "John",
                        |    "lastName": "Dyson",
                        |    "dateOfBirth": "1970-10-04",
                        |    "address": {
                        |        "lineOne": "34 Park Lane",
                        |        "lineTwo": "Building A",
                        |        "lineThree": "Suite 100",
                        |        "lineFour": "Manchester",
                        |        "postalCode": "",
                        |        "countryCode": "GB"
                        |    },
                        |    "contactDetails": {
                        |        "landline": "747663966",
                        |        "mobile": "38390756243",
                        |        "fax": "58371813020",
                        |        "emailAddress": "Patrick.Dyson@example.com"
                        |    }
                        |}
                        |""".stripMargin)
                .futureValue

              assertAsExpected(
                response,
                BAD_REQUEST,
                Some("""
                       |[
                       |  {
                       |    "code": "invalid-address-postal-code"
                       |  }
                       |]
                       |""".stripMargin)
              )
              verifyThatDownstreamApiWasNotCalled()
            }
            "too long" in {
              val response = wsClient
                .url(fullUrl("/registrations/withoutId/individual"))
                .withHttpHeaders(("Content-Type", "application/json"))
                .post("""
                        |{
                        |    "firstName": "Patrick",
                        |    "middleName": "John",
                        |    "lastName": "Dyson",
                        |    "dateOfBirth": "1970-10-04",
                        |    "address": {
                        |        "lineOne": "34 Park Lane",
                        |        "lineTwo": "Building A",
                        |        "lineThree": "Suite 100",
                        |        "lineFour": "Manchester",
                        |        "postalCode": "509480494049",
                        |        "countryCode": "FR"
                        |    },
                        |    "contactDetails": {
                        |        "landline": "747663966",
                        |        "mobile": "38390756243",
                        |        "fax": "58371813020",
                        |        "emailAddress": "Patrick.Dyson@example.com"
                        |    }
                        |}
                        |""".stripMargin)
                .futureValue

              assertAsExpected(
                response,
                BAD_REQUEST,
                Some("""
                       |[
                       |  {
                       |    "code": "invalid-address-postal-code"
                       |  }
                       |]
                       |""".stripMargin)
              )
              verifyThatDownstreamApiWasNotCalled()
            }
          }
        }
        "in the contact details, the" - {
          "landline number is" - {
            "blank" in {
              val response = wsClient
                .url(fullUrl("/registrations/withoutId/individual"))
                .withHttpHeaders(("Content-Type", "application/json"))
                .post("""
                        |{
                        |    "firstName": "Patrick",
                        |    "middleName": "John",
                        |    "lastName": "Dyson",
                        |    "dateOfBirth": "1970-10-04",
                        |    "address": {
                        |        "lineOne": "34 Park Lane",
                        |        "lineTwo": "Building A",
                        |        "lineThree": "Suite 100",
                        |        "lineFour": "Manchester",
                        |        "postalCode": "M54 1MQ",
                        |        "countryCode": "GB"
                        |    },
                        |    "contactDetails": {
                        |        "landline": "",
                        |        "mobile": "38390756243",
                        |        "fax": "58371813020",
                        |        "emailAddress": "Patrick.Dyson@example.com"
                        |    }
                        |}
                        |""".stripMargin)
                .futureValue

              assertAsExpected(
                response,
                BAD_REQUEST,
                Some("""
                       |[
                       |  {
                       |    "code": "invalid-contact-details-landline"
                       |  }
                       |]
                       |""".stripMargin)
              )
              verifyThatDownstreamApiWasNotCalled()
            }
            "too long" in {
              val response = wsClient
                .url(fullUrl("/registrations/withoutId/individual"))
                .withHttpHeaders(("Content-Type", "application/json"))
                .post("""
                        |{
                        |    "firstName": "Patrick",
                        |    "middleName": "John",
                        |    "lastName": "Dyson",
                        |    "dateOfBirth": "1970-10-04",
                        |    "address": {
                        |        "lineOne": "34 Park Lane",
                        |        "lineTwo": "Building A",
                        |        "lineThree": "Suite 100",
                        |        "lineFour": "Manchester",
                        |        "postalCode": "M54 1MQ",
                        |        "countryCode": "GB"
                        |    },
                        |    "contactDetails": {
                        |        "landline": "747663966747663966747663966",
                        |        "mobile": "38390756243",
                        |        "fax": "58371813020",
                        |        "emailAddress": "Patrick.Dyson@example.com"
                        |    }
                        |}
                        |""".stripMargin)
                .futureValue

              assertAsExpected(
                response,
                BAD_REQUEST,
                Some("""
                       |[
                       |  {
                       |    "code": "invalid-contact-details-landline"
                       |  }
                       |]
                       |""".stripMargin)
              )
              verifyThatDownstreamApiWasNotCalled()
            }
            "of an invalid format" in {
              val response = wsClient
                .url(fullUrl("/registrations/withoutId/individual"))
                .withHttpHeaders(("Content-Type", "application/json"))
                .post("""
                        |{
                        |    "firstName": "Patrick",
                        |    "middleName": "John",
                        |    "lastName": "Dyson",
                        |    "dateOfBirth": "1970-10-04",
                        |    "address": {
                        |        "lineOne": "34 Park Lane",
                        |        "lineTwo": "Building A",
                        |        "lineThree": "Suite 100",
                        |        "lineFour": "Manchester",
                        |        "postalCode": "M54 1MQ",
                        |        "countryCode": "GB"
                        |    },
                        |    "contactDetails": {
                        |        "landline": "£747663966",
                        |        "mobile": "38390756243",
                        |        "fax": "58371813020",
                        |        "emailAddress": "Patrick.Dyson@example.com"
                        |    }
                        |}
                        |""".stripMargin)
                .futureValue

              assertAsExpected(
                response,
                BAD_REQUEST,
                Some("""
                       |[
                       |  {
                       |    "code": "invalid-contact-details-landline"
                       |  }
                       |]
                       |""".stripMargin)
              )
              verifyThatDownstreamApiWasNotCalled()
            }
          }
          "mobile number is" - {
            "blank" in {
              val response = wsClient
                .url(fullUrl("/registrations/withoutId/individual"))
                .withHttpHeaders(("Content-Type", "application/json"))
                .post("""
                        |{
                        |    "firstName": "Patrick",
                        |    "middleName": "John",
                        |    "lastName": "Dyson",
                        |    "dateOfBirth": "1970-10-04",
                        |    "address": {
                        |        "lineOne": "34 Park Lane",
                        |        "lineTwo": "Building A",
                        |        "lineThree": "Suite 100",
                        |        "lineFour": "Manchester",
                        |        "postalCode": "M54 1MQ",
                        |        "countryCode": "GB"
                        |    },
                        |    "contactDetails": {
                        |        "landline": "747663966",
                        |        "mobile": "",
                        |        "fax": "58371813020",
                        |        "emailAddress": "Patrick.Dyson@example.com"
                        |    }
                        |}
                        |""".stripMargin)
                .futureValue

              assertAsExpected(
                response,
                BAD_REQUEST,
                Some("""
                       |[
                       |  {
                       |    "code": "invalid-contact-details-mobile"
                       |  }
                       |]
                       |""".stripMargin)
              )
              verifyThatDownstreamApiWasNotCalled()
            }
            "too long" in {
              val response = wsClient
                .url(fullUrl("/registrations/withoutId/individual"))
                .withHttpHeaders(("Content-Type", "application/json"))
                .post("""
                        |{
                        |    "firstName": "Patrick",
                        |    "middleName": "John",
                        |    "lastName": "Dyson",
                        |    "dateOfBirth": "1970-10-04",
                        |    "address": {
                        |        "lineOne": "34 Park Lane",
                        |        "lineTwo": "Building A",
                        |        "lineThree": "Suite 100",
                        |        "lineFour": "Manchester",
                        |        "postalCode": "M54 1MQ",
                        |        "countryCode": "GB"
                        |    },
                        |    "contactDetails": {
                        |        "landline": "747663966",
                        |        "mobile": "38390756243383907562433839",
                        |        "fax": "58371813020",
                        |        "emailAddress": "Patrick.Dyson@example.com"
                        |    }
                        |}
                        |""".stripMargin)
                .futureValue

              assertAsExpected(
                response,
                BAD_REQUEST,
                Some("""
                       |[
                       |  {
                       |    "code": "invalid-contact-details-mobile"
                       |  }
                       |]
                       |""".stripMargin)
              )
              verifyThatDownstreamApiWasNotCalled()
            }
            "of an invalid format" in {
              val response = wsClient
                .url(fullUrl("/registrations/withoutId/individual"))
                .withHttpHeaders(("Content-Type", "application/json"))
                .post("""
                        |{
                        |    "firstName": "Patrick",
                        |    "middleName": "John",
                        |    "lastName": "Dyson",
                        |    "dateOfBirth": "1970-10-04",
                        |    "address": {
                        |        "lineOne": "34 Park Lane",
                        |        "lineTwo": "Building A",
                        |        "lineThree": "Suite 100",
                        |        "lineFour": "Manchester",
                        |        "postalCode": "M54 1MQ",
                        |        "countryCode": "GB"
                        |    },
                        |    "contactDetails": {
                        |        "landline": "747663966",
                        |        "mobile": "£38390756243",
                        |        "fax": "58371813020",
                        |        "emailAddress": "Patrick.Dyson@example.com"
                        |    }
                        |}
                        |""".stripMargin)
                .futureValue

              assertAsExpected(
                response,
                BAD_REQUEST,
                Some("""
                       |[
                       |  {
                       |    "code": "invalid-contact-details-mobile"
                       |  }
                       |]
                       |""".stripMargin)
              )
              verifyThatDownstreamApiWasNotCalled()
            }
          }
          "fax number is" - {
            "blank" in {
              val response = wsClient
                .url(fullUrl("/registrations/withoutId/individual"))
                .withHttpHeaders(("Content-Type", "application/json"))
                .post("""
                        |{
                        |    "firstName": "Patrick",
                        |    "middleName": "John",
                        |    "lastName": "Dyson",
                        |    "dateOfBirth": "1970-10-04",
                        |    "address": {
                        |        "lineOne": "34 Park Lane",
                        |        "lineTwo": "Building A",
                        |        "lineThree": "Suite 100",
                        |        "lineFour": "Manchester",
                        |        "postalCode": "M54 1MQ",
                        |        "countryCode": "GB"
                        |    },
                        |    "contactDetails": {
                        |        "landline": "747663966",
                        |        "mobile": "38390756243",
                        |        "fax": "",
                        |        "emailAddress": "Patrick.Dyson@example.com"
                        |    }
                        |}
                        |""".stripMargin)
                .futureValue

              assertAsExpected(
                response,
                BAD_REQUEST,
                Some("""
                       |[
                       |  {
                       |    "code": "invalid-contact-details-fax"
                       |  }
                       |]
                       |""".stripMargin)
              )
              verifyThatDownstreamApiWasNotCalled()
            }
            "too long" in {
              val response = wsClient
                .url(fullUrl("/registrations/withoutId/individual"))
                .withHttpHeaders(("Content-Type", "application/json"))
                .post("""
                        |{
                        |    "firstName": "Patrick",
                        |    "middleName": "John",
                        |    "lastName": "Dyson",
                        |    "dateOfBirth": "1970-10-04",
                        |    "address": {
                        |        "lineOne": "34 Park Lane",
                        |        "lineTwo": "Building A",
                        |        "lineThree": "Suite 100",
                        |        "lineFour": "Manchester",
                        |        "postalCode": "M54 1MQ",
                        |        "countryCode": "GB"
                        |    },
                        |    "contactDetails": {
                        |        "landline": "747663966",
                        |        "mobile": "38390756243",
                        |        "fax": "583718130205837181302058371813020",
                        |        "emailAddress": "Patrick.Dyson@example.com"
                        |    }
                        |}
                        |""".stripMargin)
                .futureValue

              assertAsExpected(
                response,
                BAD_REQUEST,
                Some("""
                       |[
                       |  {
                       |    "code": "invalid-contact-details-fax"
                       |  }
                       |]
                       |""".stripMargin)
              )
              verifyThatDownstreamApiWasNotCalled()
            }
            "of an invalid format" in {
              val response = wsClient
                .url(fullUrl("/registrations/withoutId/individual"))
                .withHttpHeaders(("Content-Type", "application/json"))
                .post("""
                        |{
                        |    "firstName": "Patrick",
                        |    "middleName": "John",
                        |    "lastName": "Dyson",
                        |    "dateOfBirth": "1970-10-04",
                        |    "address": {
                        |        "lineOne": "34 Park Lane",
                        |        "lineTwo": "Building A",
                        |        "lineThree": "Suite 100",
                        |        "lineFour": "Manchester",
                        |        "postalCode": "M54 1MQ",
                        |        "countryCode": "GB"
                        |    },
                        |    "contactDetails": {
                        |        "landline": "747663966",
                        |        "mobile": "38390756243",
                        |        "fax": "£58371813020",
                        |        "emailAddress": "Patrick.Dyson@example.com"
                        |    }
                        |}
                        |""".stripMargin)
                .futureValue

              assertAsExpected(
                response,
                BAD_REQUEST,
                Some("""
                       |[
                       |  {
                       |    "code": "invalid-contact-details-fax"
                       |  }
                       |]
                       |""".stripMargin)
              )
              verifyThatDownstreamApiWasNotCalled()
            }
          }
          "email address is" - {
            "blank" in {
              val response = wsClient
                .url(fullUrl("/registrations/withoutId/individual"))
                .withHttpHeaders(("Content-Type", "application/json"))
                .post("""
                        |{
                        |    "firstName": "Patrick",
                        |    "middleName": "John",
                        |    "lastName": "Dyson",
                        |    "dateOfBirth": "1970-10-04",
                        |    "address": {
                        |        "lineOne": "34 Park Lane",
                        |        "lineTwo": "Building A",
                        |        "lineThree": "Suite 100",
                        |        "lineFour": "Manchester",
                        |        "postalCode": "M54 1MQ",
                        |        "countryCode": "GB"
                        |    },
                        |    "contactDetails": {
                        |        "landline": "747663966",
                        |        "mobile": "38390756243",
                        |        "fax": "58371813020",
                        |        "emailAddress": ""
                        |    }
                        |}
                        |""".stripMargin)
                .futureValue

              assertAsExpected(
                response,
                BAD_REQUEST,
                Some("""
                       |[
                       |  {
                       |    "code": "invalid-contact-details-email-address"
                       |  }
                       |]
                       |""".stripMargin)
              )
              verifyThatDownstreamApiWasNotCalled()
            }
            "of an invalid format" in {
              val response = wsClient
                .url(fullUrl("/registrations/withoutId/individual"))
                .withHttpHeaders(("Content-Type", "application/json"))
                .post("""
                        |{
                        |    "firstName": "Patrick",
                        |    "middleName": "John",
                        |    "lastName": "Dyson",
                        |    "dateOfBirth": "1970-10-04",
                        |    "address": {
                        |        "lineOne": "34 Park Lane",
                        |        "lineTwo": "Building A",
                        |        "lineThree": "Suite 100",
                        |        "lineFour": "Manchester",
                        |        "postalCode": "M54 1MQ",
                        |        "countryCode": "GB"
                        |    },
                        |    "contactDetails": {
                        |        "landline": "747663966",
                        |        "mobile": "38390756243",
                        |        "fax": "58371813020",
                        |        "emailAddress": "@example.com"
                        |    }
                        |}
                        |""".stripMargin)
                .futureValue

              assertAsExpected(
                response,
                BAD_REQUEST,
                Some("""
                       |[
                       |  {
                       |    "code": "invalid-contact-details-email-address"
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
}
