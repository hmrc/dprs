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

class RegistrationWithIdForAnOrganisationSpec extends BaseRegistrationWithIdSpec {

  "attempting to register with an ID, as an organisation, when" - {
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
