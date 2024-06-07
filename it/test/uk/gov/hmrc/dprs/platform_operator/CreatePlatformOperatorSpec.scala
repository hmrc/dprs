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

package uk.gov.hmrc.dprs.platform_operator

import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.matching.EqualToPattern
import play.api.http.Status._
import uk.gov.hmrc.dprs.BaseBackendIntegrationSpec
import uk.gov.hmrc.dprs.connectors.platform_operator.CreatePlatformOperatorConnector

import java.time.Instant

class CreatePlatformOperatorSpec extends BaseBackendIntegrationSpec {

  override val baseConnectorPath: String = CreatePlatformOperatorConnector.connectorPath

  "attempting to create a platform operator, when" - {
    "the request is" - {
      "valid, when" - {
        "the address country of issue is inside the UK or related territories" in {
          val countryCode = "GB"
          val postalCode  = "OX2 3HD"
          stubFor(
            post(urlEqualTo(baseConnectorPath))
              .withHeader("accept", new EqualToPattern("application/json"))
              .withHeader("authorization", new EqualToPattern("Bearer b8a88f78-9220-4d6a-91e3-973ada5be2bd"))
              .withHeader("date", new EqualToPattern(Instant.now(fixedClock).toString))
              .withHeader("x-conversation-id", new EqualToPattern("dfc765eb-4fac-47ed-ae27-cd208a8d584e"))
              .withHeader("x-correlation-id", new EqualToPattern("66c17926-bf76-4959-b6b5-f64b50649ed5"))
              .withHeader("x-forwarded-host", new EqualToPattern("backend.dprs"))
              .withRequestBody(equalToJson(s"""
                                              |{
                                              |  "POManagement": {
                                              |    "RequestCommon": {
                                              |      "OriginatingSystem": "MDTP",
                                              |      "TransmittingSystem": "EIS",
                                              |      "RequestType": "CREATE",
                                              |      "Regime": "DPI",
                                              |      "RequestParameters" : [ ]
                                              |    },
                                              |    "RequestDetails": {
                                              |      "POName": "Dyson",
                                              |      "BusinessName": "Dyson Inc.",
                                              |      "SubscriptionID": "345567808",
                                              |      "TradingName": "Dyson",
                                              |      "TINDetails": [
                                              |        {
                                              |          "TINType": "UTR",
                                              |          "TIN": "1234567890",
                                              |          "IssuedBy": "GB"
                                              |        }
                                              |      ],
                                              |      "NotificationDetails": {
                                              |        "NotificationType": "RPO",
                                              |        "IsActiveSeller": true,
                                              |        "IsDueDiligence": false,
                                              |        "FirstNotifiedReportingPeriod": "2024"
                                              |      },
                                              |      "AddressDetails": {
                                              |        "AddressLine1": "26424 Cecelia Junction",
                                              |        "AddressLine2": "Suite 858",
                                              |        "AddressLine3": "Building Two",
                                              |        "AddressLine4": "West Siobhanberg",
                                              |        "CountryCode": "$countryCode",
                                              |        "PostalCode": "$postalCode"
                                              |      },
                                              |      "PrimaryContactDetails": {
                                              |        "ContactName": "Patrick Dyson",
                                              |        "EmailAddress": "Patrick.Dyson@example.com",
                                              |        "PhoneNumber": "38390756243"
                                              |      },
                                              |      "SecondaryContactDetails": {
                                              |        "ContactName": "Phillipa Dyson",
                                              |        "EmailAddress": "Phillipa.Dyson@example.com",
                                              |        "PhoneNumber": "38390756246"
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
                               |  "success": {
                               |    "processingDate": "2023-12-13T11:50:35Z",
                               |    "ReturnParameters": {
                               |      "Key": "POID",
                               |      "Value": "PO12345"
                               |    }
                               |  }
                               |}
                               |""".stripMargin)
              )
          )

          val response = wsClient
            .url(fullUrl("/platform-operators/345567808"))
            .withHttpHeaders(
              ("Content-Type", "application/json"),
              ("Authorization", "Bearer b8a88f78-9220-4d6a-91e3-973ada5be2bd"),
              ("x-conversation-id", "dfc765eb-4fac-47ed-ae27-cd208a8d584e"),
              ("x-correlation-id", "66c17926-bf76-4959-b6b5-f64b50649ed5"),
              ("x-forwarded-host", "backend.dprs")
            )
            .post(s"""
                    |{
                    |  "internalName": "Dyson",
                    |  "businessName": "Dyson Inc.",
                    |  "tradingName": "Dyson",
                    |  "ids": [
                    |    {
                    |      "type": "UTR",
                    |      "value": "1234567890",
                    |      "countryCodeOfIssue": "GB"
                    |    }
                    |  ],
                    |  "contacts": [
                    |    {
                    |      "name": "Patrick Dyson",
                    |      "phone": "38390756243",
                    |      "emailAddress": "Patrick.Dyson@example.com"
                    |    },
                    |    {
                    |      "name": "Phillipa Dyson",
                    |      "phone": "38390756246",
                    |      "emailAddress": "Phillipa.Dyson@example.com"
                    |    }
                    |  ],
                    |  "address": {
                    |    "lineOne": "26424 Cecelia Junction",
                    |    "lineTwo": "Suite 858",
                    |    "lineThree": "Building Two",
                    |    "lineFour": "West Siobhanberg",
                    |    "postalCode": "$postalCode",
                    |    "countryCode": "$countryCode"
                    |  },
                    |  "reportingNotification": {
                    |    "type": "RPO",
                    |    "isActiveSeller": true,
                    |    "isDueDiligence": false,
                    |    "year": 2024
                    |  }
                    |}
                    |""".stripMargin)
            .futureValue

          assertAsExpected(
            response = response,
            status = CREATED,
            expectedHeaders = Some(Map("Location" -> asFullUrl("/platform-operators/345567808/PO12345")))
          )
          verifyThatDownstreamApiWasCalled()
        }
        "the address country of issue is outside UK and related territories" in {
          val countryCode = "DK"
          stubFor(
            post(urlEqualTo(baseConnectorPath))
              .withHeader("accept", new EqualToPattern("application/json"))
              .withHeader("authorization", new EqualToPattern("Bearer b8a88f78-9220-4d6a-91e3-973ada5be2bd"))
              .withHeader("date", new EqualToPattern(Instant.now(fixedClock).toString))
              .withHeader("x-conversation-id", new EqualToPattern("dfc765eb-4fac-47ed-ae27-cd208a8d584e"))
              .withHeader("x-correlation-id", new EqualToPattern("66c17926-bf76-4959-b6b5-f64b50649ed5"))
              .withHeader("x-forwarded-host", new EqualToPattern("backend.dprs"))
              .withRequestBody(equalToJson(s"""
                                              |{
                                              |  "POManagement": {
                                              |    "RequestCommon": {
                                              |      "OriginatingSystem": "MDTP",
                                              |      "TransmittingSystem": "EIS",
                                              |      "RequestType": "CREATE",
                                              |      "Regime": "DPI",
                                              |      "RequestParameters" : [ ]
                                              |    },
                                              |    "RequestDetails": {
                                              |      "POName": "Dyson",
                                              |      "BusinessName": "Dyson Inc.",
                                              |      "SubscriptionID": "345567810",
                                              |      "TradingName": "Dyson",
                                              |      "TINDetails": [
                                              |        {
                                              |          "TINType": "UTR",
                                              |          "TIN": "1234567890",
                                              |          "IssuedBy": "GB"
                                              |        }
                                              |      ],
                                              |      "NotificationDetails": {
                                              |        "NotificationType": "RPO",
                                              |        "IsActiveSeller": true,
                                              |        "IsDueDiligence": false,
                                              |        "FirstNotifiedReportingPeriod": "2024"
                                              |      },
                                              |      "AddressDetails": {
                                              |        "AddressLine1": "26424 Cecelia Junction",
                                              |        "AddressLine2": "Suite 858",
                                              |        "AddressLine3": "Building Two",
                                              |        "AddressLine4": "West Siobhanberg",
                                              |        "CountryCode": "$countryCode"
                                              |      },
                                              |      "PrimaryContactDetails": {
                                              |        "ContactName": "Patrick Dyson",
                                              |        "EmailAddress": "Patrick.Dyson@example.com",
                                              |        "PhoneNumber": "38390756243"
                                              |      },
                                              |      "SecondaryContactDetails": {
                                              |        "ContactName": "Phillipa Dyson",
                                              |        "EmailAddress": "Phillipa.Dyson@example.com",
                                              |        "PhoneNumber": "38390756246"
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
                               |  "success": {
                               |    "processingDate": "2023-12-13T11:50:35Z",
                               |    "ReturnParameters": {
                               |      "Key": "POID",
                               |      "Value": "PO12346"
                               |    }
                               |  }
                               |}
                               |""".stripMargin)
              )
          )

          val response = wsClient
            .url(fullUrl("/platform-operators/345567810"))
            .withHttpHeaders(
              ("Content-Type", "application/json"),
              ("Authorization", "Bearer b8a88f78-9220-4d6a-91e3-973ada5be2bd"),
              ("x-conversation-id", "dfc765eb-4fac-47ed-ae27-cd208a8d584e"),
              ("x-correlation-id", "66c17926-bf76-4959-b6b5-f64b50649ed5"),
              ("x-forwarded-host", "backend.dprs")
            )
            .post(s"""
                    |{
                    |  "internalName": "Dyson",
                    |  "businessName": "Dyson Inc.",
                    |  "tradingName": "Dyson",
                    |  "ids": [
                    |    {
                    |      "type": "UTR",
                    |      "value": "1234567890",
                    |      "countryCodeOfIssue": "GB"
                    |    }
                    |  ],
                    |  "contacts": [
                    |    {
                    |      "name": "Patrick Dyson",
                    |      "phone": "38390756243",
                    |      "emailAddress": "Patrick.Dyson@example.com"
                    |    },
                    |    {
                    |      "name": "Phillipa Dyson",
                    |      "phone": "38390756246",
                    |      "emailAddress": "Phillipa.Dyson@example.com"
                    |    }
                    |  ],
                    |  "address": {
                    |    "lineOne": "26424 Cecelia Junction",
                    |    "lineTwo": "Suite 858",
                    |    "lineThree": "Building Two",
                    |    "lineFour": "West Siobhanberg",
                    |    "countryCode": "$countryCode"
                    |  },
                    |  "reportingNotification": {
                    |    "type": "RPO",
                    |    "isActiveSeller": true,
                    |    "isDueDiligence": false,
                    |    "year": 2024
                    |  }
                    |}
                    |""".stripMargin)
            .futureValue

          assertAsExpected(
            response = response,
            status = CREATED,
            expectedHeaders = Some(Map("Location" -> asFullUrl("/platform-operators/345567810/PO12346")))
          )
          verifyThatDownstreamApiWasCalled()
        }
      }
    }
    "valid but the integration call fails with response:" - {
      "bad request" in {
        stubFor(
          post(urlEqualTo(baseConnectorPath))
            .withHeader("accept", new EqualToPattern("application/json"))
            .withHeader("authorization", new EqualToPattern("Bearer b8a88f78-9220-4d6a-91e3-973ada5be2bd"))
            .withHeader("date", new EqualToPattern(Instant.now(fixedClock).toString))
            .withHeader("x-conversation-id", new EqualToPattern("dfc765eb-4fac-47ed-ae27-cd208a8d584e"))
            .withHeader("x-correlation-id", new EqualToPattern("66c17926-bf76-4959-b6b5-f64b50649ed5"))
            .withHeader("x-forwarded-host", new EqualToPattern("backend.dprs"))
            .withRequestBody(equalToJson("""
                |{
                |  "POManagement": {
                |    "RequestCommon": {
                |      "OriginatingSystem": "MDTP",
                |      "TransmittingSystem": "EIS",
                |      "RequestType": "CREATE",
                |      "Regime": "DPI",
                |      "RequestParameters" : [ ]
                |    },
                |    "RequestDetails": {
                |      "POName": "Dyson",
                |      "BusinessName": "Dyson Inc.",
                |      "SubscriptionID": "345567808",
                |      "TradingName": "Dyson",
                |      "TINDetails": [
                |        {
                |          "TINType": "UTR",
                |          "TIN": "1234567890",
                |          "IssuedBy": "GB"
                |        }
                |      ],
                |      "NotificationDetails": {
                |        "NotificationType": "RPO",
                |        "IsActiveSeller": true,
                |        "IsDueDiligence": false,
                |        "FirstNotifiedReportingPeriod": "2024"
                |      },
                |      "AddressDetails": {
                |        "AddressLine1": "26424 Cecelia Junction",
                |        "AddressLine2": "Suite 858",
                |        "AddressLine3": "Building Two",
                |        "AddressLine4": "West Siobhanberg",
                |        "CountryCode": "GB",
                |        "PostalCode": "OX2 3HD"
                |      },
                |      "PrimaryContactDetails": {
                |        "ContactName": "Patrick Dyson",
                |        "EmailAddress": "Patrick.Dyson@example.com",
                |        "PhoneNumber": "38390756243"
                |      },
                |      "SecondaryContactDetails": {
                |        "ContactName": "Phillipa Dyson",
                |        "EmailAddress": "Phillipa.Dyson@example.com",
                |        "PhoneNumber": "38390756246"
                |      }
                |    }
                |  }
                |}
                |""".stripMargin))
            .willReturn(
              aResponse()
                .withHeader("Content-Type", "application/json")
                .withStatus(BAD_REQUEST)
            )
        )

        val response = wsClient
          .url(fullUrl("/platform-operators/345567808"))
          .withHttpHeaders(
            ("Content-Type", "application/json"),
            ("Authorization", "Bearer b8a88f78-9220-4d6a-91e3-973ada5be2bd"),
            ("x-conversation-id", "dfc765eb-4fac-47ed-ae27-cd208a8d584e"),
            ("x-correlation-id", "66c17926-bf76-4959-b6b5-f64b50649ed5"),
            ("x-forwarded-host", "backend.dprs")
          )
          .post("""
              |{
              |  "internalName": "Dyson",
              |  "businessName": "Dyson Inc.",
              |  "tradingName": "Dyson",
              |  "ids": [
              |    {
              |      "type": "UTR",
              |      "value": "1234567890",
              |      "countryCodeOfIssue": "GB"
              |    }
              |  ],
              |  "contacts": [
              |    {
              |      "name": "Patrick Dyson",
              |      "phone": "38390756243",
              |      "emailAddress": "Patrick.Dyson@example.com"
              |    },
              |    {
              |      "name": "Phillipa Dyson",
              |      "phone": "38390756246",
              |      "emailAddress": "Phillipa.Dyson@example.com"
              |    }
              |  ],
              |  "address": {
              |    "lineOne": "26424 Cecelia Junction",
              |    "lineTwo": "Suite 858",
              |    "lineThree": "Building Two",
              |    "lineFour": "West Siobhanberg",
              |    "postalCode": "OX2 3HD",
              |    "countryCode": "GB"
              |  },
              |  "reportingNotification": {
              |    "type": "RPO",
              |    "isActiveSeller": true,
              |    "isDueDiligence": false,
              |    "year": 2024
              |  }
              |}
              |""".stripMargin)
          .futureValue

        assertAsExpected(
          response = response,
          status = INTERNAL_SERVER_ERROR
        )
        verifyThatDownstreamApiWasCalled()
      }
      "forbidden" in {
        stubFor(
          post(urlEqualTo(baseConnectorPath))
            .withHeader("accept", new EqualToPattern("application/json"))
            .withHeader("authorization", new EqualToPattern("Bearer b8a88f78-9220-4d6a-91e3-973ada5be2bd"))
            .withHeader("date", new EqualToPattern(Instant.now(fixedClock).toString))
            .withHeader("x-conversation-id", new EqualToPattern("dfc765eb-4fac-47ed-ae27-cd208a8d584e"))
            .withHeader("x-correlation-id", new EqualToPattern("66c17926-bf76-4959-b6b5-f64b50649ed5"))
            .withHeader("x-forwarded-host", new EqualToPattern("backend.dprs"))
            .withRequestBody(equalToJson("""
                                           |{
                                           |  "POManagement": {
                                           |    "RequestCommon": {
                                           |      "OriginatingSystem": "MDTP",
                                           |      "TransmittingSystem": "EIS",
                                           |      "RequestType": "CREATE",
                                           |      "Regime": "DPI",
                                           |      "RequestParameters" : [ ]
                                           |    },
                                           |    "RequestDetails": {
                                           |      "POName": "Dyson",
                                           |      "BusinessName": "Dyson Inc.",
                                           |      "SubscriptionID": "345567808",
                                           |      "TradingName": "Dyson",
                                           |      "TINDetails": [
                                           |        {
                                           |          "TINType": "UTR",
                                           |          "TIN": "1234567890",
                                           |          "IssuedBy": "GB"
                                           |        }
                                           |      ],
                                           |      "NotificationDetails": {
                                           |        "NotificationType": "RPO",
                                           |        "IsActiveSeller": true,
                                           |        "IsDueDiligence": false,
                                           |        "FirstNotifiedReportingPeriod": "2024"
                                           |      },
                                           |      "AddressDetails": {
                                           |        "AddressLine1": "26424 Cecelia Junction",
                                           |        "AddressLine2": "Suite 858",
                                           |        "AddressLine3": "Building Two",
                                           |        "AddressLine4": "West Siobhanberg",
                                           |        "CountryCode": "GB",
                                           |        "PostalCode": "OX2 3HD"
                                           |      },
                                           |      "PrimaryContactDetails": {
                                           |        "ContactName": "Patrick Dyson",
                                           |        "EmailAddress": "Patrick.Dyson@example.com",
                                           |        "PhoneNumber": "38390756243"
                                           |      },
                                           |      "SecondaryContactDetails": {
                                           |        "ContactName": "Phillipa Dyson",
                                           |        "EmailAddress": "Phillipa.Dyson@example.com",
                                           |        "PhoneNumber": "38390756246"
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
          .url(fullUrl("/platform-operators/345567808"))
          .withHttpHeaders(
            ("Content-Type", "application/json"),
            ("Authorization", "Bearer b8a88f78-9220-4d6a-91e3-973ada5be2bd"),
            ("x-conversation-id", "dfc765eb-4fac-47ed-ae27-cd208a8d584e"),
            ("x-correlation-id", "66c17926-bf76-4959-b6b5-f64b50649ed5"),
            ("x-forwarded-host", "backend.dprs")
          )
          .post("""
                  |{
                  |  "internalName": "Dyson",
                  |  "businessName": "Dyson Inc.",
                  |  "tradingName": "Dyson",
                  |  "ids": [
                  |    {
                  |      "type": "UTR",
                  |      "value": "1234567890",
                  |      "countryCodeOfIssue": "GB"
                  |    }
                  |  ],
                  |  "contacts": [
                  |    {
                  |      "name": "Patrick Dyson",
                  |      "phone": "38390756243",
                  |      "emailAddress": "Patrick.Dyson@example.com"
                  |    },
                  |    {
                  |      "name": "Phillipa Dyson",
                  |      "phone": "38390756246",
                  |      "emailAddress": "Phillipa.Dyson@example.com"
                  |    }
                  |  ],
                  |  "address": {
                  |    "lineOne": "26424 Cecelia Junction",
                  |    "lineTwo": "Suite 858",
                  |    "lineThree": "Building Two",
                  |    "lineFour": "West Siobhanberg",
                  |    "postalCode": "OX2 3HD",
                  |    "countryCode": "GB"
                  |  },
                  |  "reportingNotification": {
                  |    "type": "RPO",
                  |    "isActiveSeller": true,
                  |    "isDueDiligence": false,
                  |    "year": 2024
                  |  }
                  |}
                  |""".stripMargin)
          .futureValue

        assertAsExpected(
          response = response,
          status = FORBIDDEN,
          jsonBodyOpt = Some("""
              |[
              |  {
              |    "code": "eis-returned-forbidden"
              |  }
              |]
              |""".stripMargin)
        )
        verifyThatDownstreamApiWasCalled()
      }
      "insufficient data" in {
        stubFor(
          post(urlEqualTo(baseConnectorPath))
            .withHeader("accept", new EqualToPattern("application/json"))
            .withHeader("authorization", new EqualToPattern("Bearer b8a88f78-9220-4d6a-91e3-973ada5be2bd"))
            .withHeader("date", new EqualToPattern(Instant.now(fixedClock).toString))
            .withHeader("x-conversation-id", new EqualToPattern("dfc765eb-4fac-47ed-ae27-cd208a8d584e"))
            .withHeader("x-correlation-id", new EqualToPattern("66c17926-bf76-4959-b6b5-f64b50649ed5"))
            .withHeader("x-forwarded-host", new EqualToPattern("backend.dprs"))
            .withRequestBody(equalToJson("""
                                           |{
                                           |  "POManagement": {
                                           |    "RequestCommon": {
                                           |      "OriginatingSystem": "MDTP",
                                           |      "TransmittingSystem": "EIS",
                                           |      "RequestType": "CREATE",
                                           |      "Regime": "DPI",
                                           |      "RequestParameters" : [ ]
                                           |    },
                                           |    "RequestDetails": {
                                           |      "POName": "Dyson",
                                           |      "BusinessName": "Dyson Inc.",
                                           |      "SubscriptionID": "345567808",
                                           |      "TradingName": "Dyson",
                                           |      "TINDetails": [
                                           |        {
                                           |          "TINType": "UTR",
                                           |          "TIN": "1234567890",
                                           |          "IssuedBy": "GB"
                                           |        }
                                           |      ],
                                           |      "NotificationDetails": {
                                           |        "NotificationType": "RPO",
                                           |        "IsActiveSeller": true,
                                           |        "IsDueDiligence": false,
                                           |        "FirstNotifiedReportingPeriod": "2024"
                                           |      },
                                           |      "AddressDetails": {
                                           |        "AddressLine1": "26424 Cecelia Junction",
                                           |        "AddressLine2": "Suite 858",
                                           |        "AddressLine3": "Building Two",
                                           |        "AddressLine4": "West Siobhanberg",
                                           |        "CountryCode": "GB",
                                           |        "PostalCode": "OX2 3HD"
                                           |      },
                                           |      "PrimaryContactDetails": {
                                           |        "ContactName": "Patrick Dyson",
                                           |        "EmailAddress": "Patrick.Dyson@example.com",
                                           |        "PhoneNumber": "38390756243"
                                           |      },
                                           |      "SecondaryContactDetails": {
                                           |        "ContactName": "Phillipa Dyson",
                                           |        "EmailAddress": "Phillipa.Dyson@example.com",
                                           |        "PhoneNumber": "38390756246"
                                           |      }
                                           |    }
                                           |  }
                                           |}
                                           |""".stripMargin))
            .willReturn(
              aResponse()
                .withHeader("Content-Type", "application/json")
                .withStatus(UNPROCESSABLE_ENTITY)
                .withBody("""
                    |{
                    |  "errorDetail": {
                    |    "errorCode": "005",
                    |    "errorMessage": "Request could not be processed due to insufficient or invalid data",
                    |    "source": "CADX",
                    |    "sourceFaultDetail": {
                    |      "detail": [
                    |        "Request could not be processed due to insufficient or invalid data"
                    |      ]
                    |    },
                    |    "timestamp": "{{request.headers.date}}",
                    |    "correlationId": "{{request.headers.x-correlation-id}}"
                    |  }
                    |}
                    |""".stripMargin)
            )
        )

        val response = wsClient
          .url(fullUrl("/platform-operators/345567808"))
          .withHttpHeaders(
            ("Content-Type", "application/json"),
            ("Authorization", "Bearer b8a88f78-9220-4d6a-91e3-973ada5be2bd"),
            ("x-conversation-id", "dfc765eb-4fac-47ed-ae27-cd208a8d584e"),
            ("x-correlation-id", "66c17926-bf76-4959-b6b5-f64b50649ed5"),
            ("x-forwarded-host", "backend.dprs")
          )
          .post("""
                  |{
                  |  "internalName": "Dyson",
                  |  "businessName": "Dyson Inc.",
                  |  "tradingName": "Dyson",
                  |  "ids": [
                  |    {
                  |      "type": "UTR",
                  |      "value": "1234567890",
                  |      "countryCodeOfIssue": "GB"
                  |    }
                  |  ],
                  |  "contacts": [
                  |    {
                  |      "name": "Patrick Dyson",
                  |      "phone": "38390756243",
                  |      "emailAddress": "Patrick.Dyson@example.com"
                  |    },
                  |    {
                  |      "name": "Phillipa Dyson",
                  |      "phone": "38390756246",
                  |      "emailAddress": "Phillipa.Dyson@example.com"
                  |    }
                  |  ],
                  |  "address": {
                  |    "lineOne": "26424 Cecelia Junction",
                  |    "lineTwo": "Suite 858",
                  |    "lineThree": "Building Two",
                  |    "lineFour": "West Siobhanberg",
                  |    "postalCode": "OX2 3HD",
                  |    "countryCode": "GB"
                  |  },
                  |  "reportingNotification": {
                  |    "type": "RPO",
                  |    "isActiveSeller": true,
                  |    "isDueDiligence": false,
                  |    "year": 2024
                  |  }
                  |}
                  |""".stripMargin)
          .futureValue

        assertAsExpected(
          response = response,
          status = INTERNAL_SERVER_ERROR
        )
        verifyThatDownstreamApiWasCalled()
      }
      "internal server error" in {
        stubFor(
          post(urlEqualTo(baseConnectorPath))
            .withHeader("accept", new EqualToPattern("application/json"))
            .withHeader("authorization", new EqualToPattern("Bearer b8a88f78-9220-4d6a-91e3-973ada5be2bd"))
            .withHeader("date", new EqualToPattern(Instant.now(fixedClock).toString))
            .withHeader("x-conversation-id", new EqualToPattern("dfc765eb-4fac-47ed-ae27-cd208a8d584e"))
            .withHeader("x-correlation-id", new EqualToPattern("66c17926-bf76-4959-b6b5-f64b50649ed5"))
            .withHeader("x-forwarded-host", new EqualToPattern("backend.dprs"))
            .withRequestBody(equalToJson("""
                                           |{
                                           |  "POManagement": {
                                           |    "RequestCommon": {
                                           |      "OriginatingSystem": "MDTP",
                                           |      "TransmittingSystem": "EIS",
                                           |      "RequestType": "CREATE",
                                           |      "Regime": "DPI",
                                           |      "RequestParameters" : [ ]
                                           |    },
                                           |    "RequestDetails": {
                                           |      "POName": "Dyson",
                                           |      "BusinessName": "Dyson Inc.",
                                           |      "SubscriptionID": "345567808",
                                           |      "TradingName": "Dyson",
                                           |      "TINDetails": [
                                           |        {
                                           |          "TINType": "UTR",
                                           |          "TIN": "1234567890",
                                           |          "IssuedBy": "GB"
                                           |        }
                                           |      ],
                                           |      "NotificationDetails": {
                                           |        "NotificationType": "RPO",
                                           |        "IsActiveSeller": true,
                                           |        "IsDueDiligence": false,
                                           |        "FirstNotifiedReportingPeriod": "2024"
                                           |      },
                                           |      "AddressDetails": {
                                           |        "AddressLine1": "26424 Cecelia Junction",
                                           |        "AddressLine2": "Suite 858",
                                           |        "AddressLine3": "Building Two",
                                           |        "AddressLine4": "West Siobhanberg",
                                           |        "CountryCode": "GB",
                                           |        "PostalCode": "OX2 3HD"
                                           |      },
                                           |      "PrimaryContactDetails": {
                                           |        "ContactName": "Patrick Dyson",
                                           |        "EmailAddress": "Patrick.Dyson@example.com",
                                           |        "PhoneNumber": "38390756243"
                                           |      },
                                           |      "SecondaryContactDetails": {
                                           |        "ContactName": "Phillipa Dyson",
                                           |        "EmailAddress": "Phillipa.Dyson@example.com",
                                           |        "PhoneNumber": "38390756246"
                                           |      }
                                           |    }
                                           |  }
                                           |}
                                           |""".stripMargin))
            .willReturn(
              aResponse()
                .withHeader("Content-Type", "application/json")
                .withStatus(INTERNAL_SERVER_ERROR)
            )
        )

        val response = wsClient
          .url(fullUrl("/platform-operators/345567808"))
          .withHttpHeaders(
            ("Content-Type", "application/json"),
            ("Authorization", "Bearer b8a88f78-9220-4d6a-91e3-973ada5be2bd"),
            ("x-conversation-id", "dfc765eb-4fac-47ed-ae27-cd208a8d584e"),
            ("x-correlation-id", "66c17926-bf76-4959-b6b5-f64b50649ed5"),
            ("x-forwarded-host", "backend.dprs")
          )
          .post("""
                  |{
                  |  "internalName": "Dyson",
                  |  "businessName": "Dyson Inc.",
                  |  "tradingName": "Dyson",
                  |  "ids": [
                  |    {
                  |      "type": "UTR",
                  |      "value": "1234567890",
                  |      "countryCodeOfIssue": "GB"
                  |    }
                  |  ],
                  |  "contacts": [
                  |    {
                  |      "name": "Patrick Dyson",
                  |      "phone": "38390756243",
                  |      "emailAddress": "Patrick.Dyson@example.com"
                  |    },
                  |    {
                  |      "name": "Phillipa Dyson",
                  |      "phone": "38390756246",
                  |      "emailAddress": "Phillipa.Dyson@example.com"
                  |    }
                  |  ],
                  |  "address": {
                  |    "lineOne": "26424 Cecelia Junction",
                  |    "lineTwo": "Suite 858",
                  |    "lineThree": "Building Two",
                  |    "lineFour": "West Siobhanberg",
                  |    "postalCode": "OX2 3HD",
                  |    "countryCode": "GB"
                  |  },
                  |  "reportingNotification": {
                  |    "type": "RPO",
                  |    "isActiveSeller": true,
                  |    "isDueDiligence": false,
                  |    "year": 2024
                  |  }
                  |}
                  |""".stripMargin)
          .futureValue

        assertAsExpected(
          response = response,
          status = SERVICE_UNAVAILABLE,
          jsonBodyOpt = Some(
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
      "missing request type or regime" in {
        stubFor(
          post(urlEqualTo(baseConnectorPath))
            .withHeader("accept", new EqualToPattern("application/json"))
            .withHeader("authorization", new EqualToPattern("Bearer b8a88f78-9220-4d6a-91e3-973ada5be2bd"))
            .withHeader("date", new EqualToPattern(Instant.now(fixedClock).toString))
            .withHeader("x-conversation-id", new EqualToPattern("dfc765eb-4fac-47ed-ae27-cd208a8d584e"))
            .withHeader("x-correlation-id", new EqualToPattern("66c17926-bf76-4959-b6b5-f64b50649ed5"))
            .withHeader("x-forwarded-host", new EqualToPattern("backend.dprs"))
            .withRequestBody(equalToJson("""
                                           |{
                                           |  "POManagement": {
                                           |    "RequestCommon": {
                                           |      "OriginatingSystem": "MDTP",
                                           |      "TransmittingSystem": "EIS",
                                           |      "RequestType": "CREATE",
                                           |      "Regime": "DPI",
                                           |      "RequestParameters" : [ ]
                                           |    },
                                           |    "RequestDetails": {
                                           |      "POName": "Dyson",
                                           |      "BusinessName": "Dyson Inc.",
                                           |      "SubscriptionID": "345567808",
                                           |      "TradingName": "Dyson",
                                           |      "TINDetails": [
                                           |        {
                                           |          "TINType": "UTR",
                                           |          "TIN": "1234567890",
                                           |          "IssuedBy": "GB"
                                           |        }
                                           |      ],
                                           |      "NotificationDetails": {
                                           |        "NotificationType": "RPO",
                                           |        "IsActiveSeller": true,
                                           |        "IsDueDiligence": false,
                                           |        "FirstNotifiedReportingPeriod": "2024"
                                           |      },
                                           |      "AddressDetails": {
                                           |        "AddressLine1": "26424 Cecelia Junction",
                                           |        "AddressLine2": "Suite 858",
                                           |        "AddressLine3": "Building Two",
                                           |        "AddressLine4": "West Siobhanberg",
                                           |        "CountryCode": "GB",
                                           |        "PostalCode": "OX2 3HD"
                                           |      },
                                           |      "PrimaryContactDetails": {
                                           |        "ContactName": "Patrick Dyson",
                                           |        "EmailAddress": "Patrick.Dyson@example.com",
                                           |        "PhoneNumber": "38390756243"
                                           |      },
                                           |      "SecondaryContactDetails": {
                                           |        "ContactName": "Phillipa Dyson",
                                           |        "EmailAddress": "Phillipa.Dyson@example.com",
                                           |        "PhoneNumber": "38390756246"
                                           |      }
                                           |    }
                                           |  }
                                           |}
                                           |""".stripMargin))
            .willReturn(
              aResponse()
                .withHeader("Content-Type", "application/json")
                .withStatus(UNPROCESSABLE_ENTITY)
                .withBody("""
                    |{
                    |  "errorDetail": {
                    |    "errorCode": "001",
                    |    "errorMessage": "Request type or Regime is missing",
                    |    "source": "CADX",
                    |    "sourceFaultDetail": {
                    |      "detail": [
                    |        "Request type or Regime is missing"
                    |      ]
                    |    },
                    |    "timestamp": "{{request.headers.date}}",
                    |    "correlationId": "{{request.headers.x-correlation-id}}"
                    |  }
                    |}
                    |""".stripMargin)
            )
        )

        val response = wsClient
          .url(fullUrl("/platform-operators/345567808"))
          .withHttpHeaders(
            ("Content-Type", "application/json"),
            ("Authorization", "Bearer b8a88f78-9220-4d6a-91e3-973ada5be2bd"),
            ("x-conversation-id", "dfc765eb-4fac-47ed-ae27-cd208a8d584e"),
            ("x-correlation-id", "66c17926-bf76-4959-b6b5-f64b50649ed5"),
            ("x-forwarded-host", "backend.dprs")
          )
          .post("""
                  |{
                  |  "internalName": "Dyson",
                  |  "businessName": "Dyson Inc.",
                  |  "tradingName": "Dyson",
                  |  "ids": [
                  |    {
                  |      "type": "UTR",
                  |      "value": "1234567890",
                  |      "countryCodeOfIssue": "GB"
                  |    }
                  |  ],
                  |  "contacts": [
                  |    {
                  |      "name": "Patrick Dyson",
                  |      "phone": "38390756243",
                  |      "emailAddress": "Patrick.Dyson@example.com"
                  |    },
                  |    {
                  |      "name": "Phillipa Dyson",
                  |      "phone": "38390756246",
                  |      "emailAddress": "Phillipa.Dyson@example.com"
                  |    }
                  |  ],
                  |  "address": {
                  |    "lineOne": "26424 Cecelia Junction",
                  |    "lineTwo": "Suite 858",
                  |    "lineThree": "Building Two",
                  |    "lineFour": "West Siobhanberg",
                  |    "postalCode": "OX2 3HD",
                  |    "countryCode": "GB"
                  |  },
                  |  "reportingNotification": {
                  |    "type": "RPO",
                  |    "isActiveSeller": true,
                  |    "isDueDiligence": false,
                  |    "year": 2024
                  |  }
                  |}
                  |""".stripMargin)
          .futureValue

        assertAsExpected(
          response = response,
          status = INTERNAL_SERVER_ERROR
        )
        verifyThatDownstreamApiWasCalled()
      }
      "i'm a teapot" in {
        stubFor(
          post(urlEqualTo(baseConnectorPath))
            .withHeader("accept", new EqualToPattern("application/json"))
            .withHeader("authorization", new EqualToPattern("Bearer b8a88f78-9220-4d6a-91e3-973ada5be2bd"))
            .withHeader("date", new EqualToPattern(Instant.now(fixedClock).toString))
            .withHeader("x-conversation-id", new EqualToPattern("dfc765eb-4fac-47ed-ae27-cd208a8d584e"))
            .withHeader("x-correlation-id", new EqualToPattern("66c17926-bf76-4959-b6b5-f64b50649ed5"))
            .withHeader("x-forwarded-host", new EqualToPattern("backend.dprs"))
            .withRequestBody(equalToJson("""
                                           |{
                                           |  "POManagement": {
                                           |    "RequestCommon": {
                                           |      "OriginatingSystem": "MDTP",
                                           |      "TransmittingSystem": "EIS",
                                           |      "RequestType": "CREATE",
                                           |      "Regime": "DPI",
                                           |      "RequestParameters" : [ ]
                                           |    },
                                           |    "RequestDetails": {
                                           |      "POName": "Dyson",
                                           |      "BusinessName": "Dyson Inc.",
                                           |      "SubscriptionID": "345567808",
                                           |      "TradingName": "Dyson",
                                           |      "TINDetails": [
                                           |        {
                                           |          "TINType": "UTR",
                                           |          "TIN": "1234567890",
                                           |          "IssuedBy": "GB"
                                           |        }
                                           |      ],
                                           |      "NotificationDetails": {
                                           |        "NotificationType": "RPO",
                                           |        "IsActiveSeller": true,
                                           |        "IsDueDiligence": false,
                                           |        "FirstNotifiedReportingPeriod": "2024"
                                           |      },
                                           |      "AddressDetails": {
                                           |        "AddressLine1": "26424 Cecelia Junction",
                                           |        "AddressLine2": "Suite 858",
                                           |        "AddressLine3": "Building Two",
                                           |        "AddressLine4": "West Siobhanberg",
                                           |        "CountryCode": "GB",
                                           |        "PostalCode": "OX2 3HD"
                                           |      },
                                           |      "PrimaryContactDetails": {
                                           |        "ContactName": "Patrick Dyson",
                                           |        "EmailAddress": "Patrick.Dyson@example.com",
                                           |        "PhoneNumber": "38390756243"
                                           |      },
                                           |      "SecondaryContactDetails": {
                                           |        "ContactName": "Phillipa Dyson",
                                           |        "EmailAddress": "Phillipa.Dyson@example.com",
                                           |        "PhoneNumber": "38390756246"
                                           |      }
                                           |    }
                                           |  }
                                           |}
                                           |""".stripMargin))
            .willReturn(
              aResponse()
                .withHeader("Content-Type", "application/json")
                .withStatus(IM_A_TEAPOT)
            )
        )

        val response = wsClient
          .url(fullUrl("/platform-operators/345567808"))
          .withHttpHeaders(
            ("Content-Type", "application/json"),
            ("Authorization", "Bearer b8a88f78-9220-4d6a-91e3-973ada5be2bd"),
            ("x-conversation-id", "dfc765eb-4fac-47ed-ae27-cd208a8d584e"),
            ("x-correlation-id", "66c17926-bf76-4959-b6b5-f64b50649ed5"),
            ("x-forwarded-host", "backend.dprs")
          )
          .post("""
                  |{
                  |  "internalName": "Dyson",
                  |  "businessName": "Dyson Inc.",
                  |  "tradingName": "Dyson",
                  |  "ids": [
                  |    {
                  |      "type": "UTR",
                  |      "value": "1234567890",
                  |      "countryCodeOfIssue": "GB"
                  |    }
                  |  ],
                  |  "contacts": [
                  |    {
                  |      "name": "Patrick Dyson",
                  |      "phone": "38390756243",
                  |      "emailAddress": "Patrick.Dyson@example.com"
                  |    },
                  |    {
                  |      "name": "Phillipa Dyson",
                  |      "phone": "38390756246",
                  |      "emailAddress": "Phillipa.Dyson@example.com"
                  |    }
                  |  ],
                  |  "address": {
                  |    "lineOne": "26424 Cecelia Junction",
                  |    "lineTwo": "Suite 858",
                  |    "lineThree": "Building Two",
                  |    "lineFour": "West Siobhanberg",
                  |    "postalCode": "OX2 3HD",
                  |    "countryCode": "GB"
                  |  },
                  |  "reportingNotification": {
                  |    "type": "RPO",
                  |    "isActiveSeller": true,
                  |    "isDueDiligence": false,
                  |    "year": 2024
                  |  }
                  |}
                  |""".stripMargin)
          .futureValue

        assertAsExpected(
          response = response,
          status = INTERNAL_SERVER_ERROR
        )
        verifyThatDownstreamApiWasCalled()
      }
    }
    "valid, but the response is invalid, as it" - {
      "has no body" in {
        stubFor(
          post(urlEqualTo(baseConnectorPath))
            .withHeader("accept", new EqualToPattern("application/json"))
            .withHeader("authorization", new EqualToPattern("Bearer b8a88f78-9220-4d6a-91e3-973ada5be2bd"))
            .withHeader("date", new EqualToPattern(Instant.now(fixedClock).toString))
            .withHeader("x-conversation-id", new EqualToPattern("dfc765eb-4fac-47ed-ae27-cd208a8d584e"))
            .withHeader("x-correlation-id", new EqualToPattern("66c17926-bf76-4959-b6b5-f64b50649ed5"))
            .withHeader("x-forwarded-host", new EqualToPattern("backend.dprs"))
            .withRequestBody(equalToJson("""
                |{
                |  "POManagement": {
                |    "RequestCommon": {
                |      "OriginatingSystem": "MDTP",
                |      "TransmittingSystem": "EIS",
                |      "RequestType": "CREATE",
                |      "Regime": "DPI",
                |      "RequestParameters" : [ ]
                |    },
                |    "RequestDetails": {
                |      "POName": "Dyson",
                |      "BusinessName": "Dyson Inc.",
                |      "SubscriptionID": "345567808",
                |      "TradingName": "Dyson",
                |      "TINDetails": [
                |        {
                |          "TINType": "UTR",
                |          "TIN": "1234567890",
                |          "IssuedBy": "GB"
                |        }
                |      ],
                |      "NotificationDetails": {
                |        "NotificationType": "RPO",
                |        "IsActiveSeller": true,
                |        "IsDueDiligence": false,
                |        "FirstNotifiedReportingPeriod": "2024"
                |      },
                |      "AddressDetails": {
                |        "AddressLine1": "26424 Cecelia Junction",
                |        "AddressLine2": "Suite 858",
                |        "AddressLine3": "Building Two",
                |        "AddressLine4": "West Siobhanberg",
                |        "CountryCode": "GB",
                |        "PostalCode": "OX2 3HD"
                |      },
                |      "PrimaryContactDetails": {
                |        "ContactName": "Patrick Dyson",
                |        "EmailAddress": "Patrick.Dyson@example.com",
                |        "PhoneNumber": "38390756243"
                |      },
                |      "SecondaryContactDetails": {
                |        "ContactName": "Phillipa Dyson",
                |        "EmailAddress": "Phillipa.Dyson@example.com",
                |        "PhoneNumber": "38390756246"
                |      }
                |    }
                |  }
                |}
                |""".stripMargin))
            .willReturn(
              aResponse()
                .withHeader("Content-Type", "application/json")
                .withStatus(OK)
            )
        )

        val response = wsClient
          .url(fullUrl("/platform-operators/345567808"))
          .withHttpHeaders(
            ("Content-Type", "application/json"),
            ("Authorization", "Bearer b8a88f78-9220-4d6a-91e3-973ada5be2bd"),
            ("x-conversation-id", "dfc765eb-4fac-47ed-ae27-cd208a8d584e"),
            ("x-correlation-id", "66c17926-bf76-4959-b6b5-f64b50649ed5"),
            ("x-forwarded-host", "backend.dprs")
          )
          .post("""
              |{
              |  "internalName": "Dyson",
              |  "businessName": "Dyson Inc.",
              |  "tradingName": "Dyson",
              |  "ids": [
              |    {
              |      "type": "UTR",
              |      "value": "1234567890",
              |      "countryCodeOfIssue": "GB"
              |    }
              |  ],
              |  "contacts": [
              |    {
              |      "name": "Patrick Dyson",
              |      "phone": "38390756243",
              |      "emailAddress": "Patrick.Dyson@example.com"
              |    },
              |    {
              |      "name": "Phillipa Dyson",
              |      "phone": "38390756246",
              |      "emailAddress": "Phillipa.Dyson@example.com"
              |    }
              |  ],
              |  "address": {
              |    "lineOne": "26424 Cecelia Junction",
              |    "lineTwo": "Suite 858",
              |    "lineThree": "Building Two",
              |    "lineFour": "West Siobhanberg",
              |    "postalCode": "OX2 3HD",
              |    "countryCode": "GB"
              |  },
              |  "reportingNotification": {
              |    "type": "RPO",
              |    "isActiveSeller": true,
              |    "isDueDiligence": false,
              |    "year": 2024
              |  }
              |}
              |""".stripMargin)
          .futureValue

        assertAsExpected(
          response = response,
          status = INTERNAL_SERVER_ERROR,
          jsonBodyOpt = Some("""
                             |{
                             |  "statusCode": 500,
                             |  "message": ""
                             |}
                             |""".stripMargin)
        )

        verifyThatDownstreamApiWasCalled()
      }
      "has a body, but there's no Platform Operator ID" in {
        stubFor(
          post(urlEqualTo(baseConnectorPath))
            .withHeader("accept", new EqualToPattern("application/json"))
            .withHeader("authorization", new EqualToPattern("Bearer b8a88f78-9220-4d6a-91e3-973ada5be2bd"))
            .withHeader("date", new EqualToPattern(Instant.now(fixedClock).toString))
            .withHeader("x-conversation-id", new EqualToPattern("dfc765eb-4fac-47ed-ae27-cd208a8d584e"))
            .withHeader("x-correlation-id", new EqualToPattern("66c17926-bf76-4959-b6b5-f64b50649ed5"))
            .withHeader("x-forwarded-host", new EqualToPattern("backend.dprs"))
            .withRequestBody(equalToJson("""
                                           |{
                                           |  "POManagement": {
                                           |    "RequestCommon": {
                                           |      "OriginatingSystem": "MDTP",
                                           |      "TransmittingSystem": "EIS",
                                           |      "RequestType": "CREATE",
                                           |      "Regime": "DPI",
                                           |      "RequestParameters" : [ ]
                                           |    },
                                           |    "RequestDetails": {
                                           |      "POName": "Dyson",
                                           |      "BusinessName": "Dyson Inc.",
                                           |      "SubscriptionID": "345567808",
                                           |      "TradingName": "Dyson",
                                           |      "TINDetails": [
                                           |        {
                                           |          "TINType": "UTR",
                                           |          "TIN": "1234567890",
                                           |          "IssuedBy": "GB"
                                           |        }
                                           |      ],
                                           |      "NotificationDetails": {
                                           |        "NotificationType": "RPO",
                                           |        "IsActiveSeller": true,
                                           |        "IsDueDiligence": false,
                                           |        "FirstNotifiedReportingPeriod": "2024"
                                           |      },
                                           |      "AddressDetails": {
                                           |        "AddressLine1": "26424 Cecelia Junction",
                                           |        "AddressLine2": "Suite 858",
                                           |        "AddressLine3": "Building Two",
                                           |        "AddressLine4": "West Siobhanberg",
                                           |        "CountryCode": "GB",
                                           |        "PostalCode": "OX2 3HD"
                                           |      },
                                           |      "PrimaryContactDetails": {
                                           |        "ContactName": "Patrick Dyson",
                                           |        "EmailAddress": "Patrick.Dyson@example.com",
                                           |        "PhoneNumber": "38390756243"
                                           |      },
                                           |      "SecondaryContactDetails": {
                                           |        "ContactName": "Phillipa Dyson",
                                           |        "EmailAddress": "Phillipa.Dyson@example.com",
                                           |        "PhoneNumber": "38390756246"
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
                             |  "success": {
                             |    "processingDate": "2023-12-13T11:50:35Z",
                             |    "ReturnParameters": {
                             |      "Key": "PLATFORM_OPERATOR_ID",
                             |      "Value": "PO12345"
                             |    }
                             |  }
                             |}
                             |""".stripMargin)
            )
        )

        val response = wsClient
          .url(fullUrl("/platform-operators/345567808"))
          .withHttpHeaders(
            ("Content-Type", "application/json"),
            ("Authorization", "Bearer b8a88f78-9220-4d6a-91e3-973ada5be2bd"),
            ("x-conversation-id", "dfc765eb-4fac-47ed-ae27-cd208a8d584e"),
            ("x-correlation-id", "66c17926-bf76-4959-b6b5-f64b50649ed5"),
            ("x-forwarded-host", "backend.dprs")
          )
          .post("""
                  |{
                  |  "internalName": "Dyson",
                  |  "businessName": "Dyson Inc.",
                  |  "tradingName": "Dyson",
                  |  "ids": [
                  |    {
                  |      "type": "UTR",
                  |      "value": "1234567890",
                  |      "countryCodeOfIssue": "GB"
                  |    }
                  |  ],
                  |  "contacts": [
                  |    {
                  |      "name": "Patrick Dyson",
                  |      "phone": "38390756243",
                  |      "emailAddress": "Patrick.Dyson@example.com"
                  |    },
                  |    {
                  |      "name": "Phillipa Dyson",
                  |      "phone": "38390756246",
                  |      "emailAddress": "Phillipa.Dyson@example.com"
                  |    }
                  |  ],
                  |  "address": {
                  |    "lineOne": "26424 Cecelia Junction",
                  |    "lineTwo": "Suite 858",
                  |    "lineThree": "Building Two",
                  |    "lineFour": "West Siobhanberg",
                  |    "postalCode": "OX2 3HD",
                  |    "countryCode": "GB"
                  |  },
                  |  "reportingNotification": {
                  |    "type": "RPO",
                  |    "isActiveSeller": true,
                  |    "isDueDiligence": false,
                  |    "year": 2024
                  |  }
                  |}
                  |""".stripMargin)
          .futureValue

        assertAsExpected(response, INTERNAL_SERVER_ERROR)
        verifyThatDownstreamApiWasCalled()
      }
    }
    "invalid, specifically:" - {
      "the internal name, which is" - {
        "absent" in {
          val response = wsClient
            .url(fullUrl("/platform-operators/345567808"))
            .withHttpHeaders(("Content-Type", "application/json"))
            .post("""
                |{
                |  "businessName": "Dyson Inc.",
                |  "tradingName": "Dyson",
                |  "ids": [
                |    {
                |      "type": "UTR",
                |      "value": "1234567890",
                |      "countryCodeOfIssue": "GB"
                |    }
                |  ],
                |  "contacts": [
                |    {
                |      "name": "Patrick Dyson",
                |      "phone": "38390756243",
                |      "emailAddress": "Patrick.Dyson@example.com"
                |    },
                |    {
                |      "name": "Phillipa Dyson",
                |      "phone": "38390756246",
                |      "emailAddress": "Phillipa.Dyson@example.com"
                |    }
                |  ],
                |  "address": {
                |    "lineOne": "26424 Cecelia Junction",
                |    "lineTwo": "Suite 858",
                |    "lineThree": "Building Two",
                |    "lineFour": "West Siobhanberg",
                |    "postalCode": "OX2 3HD",
                |    "countryCode": "GB"
                |  },
                |  "reportingNotification": {
                |    "type": "RPO",
                |    "isActiveSeller": true,
                |    "isDueDiligence": false,
                |    "year": 2024
                |  }
                |}
                |""".stripMargin)
            .futureValue

          assertAsExpected(
            response = response,
            status = BAD_REQUEST,
            jsonBodyOpt = Some("""
                |[
                |  {
                |    "code": "invalid-internal-name"
                |  }
                |]
                |""".stripMargin)
          )
          verifyThatDownstreamApiWasNotCalled()
        }
        "invalid (too short)" in {
          val response = wsClient
            .url(fullUrl("/platform-operators/345567808"))
            .withHttpHeaders(("Content-Type", "application/json"))
            .post("""
                |{
                |  "internalName": "",
                |  "businessName": "Dyson Inc.",
                |  "tradingName": "Dyson",
                |  "ids": [
                |    {
                |      "type": "UTR",
                |      "value": "1234567890",
                |      "countryCodeOfIssue": "GB"
                |    }
                |  ],
                |  "contacts": [
                |    {
                |      "name": "Patrick Dyson",
                |      "phone": "38390756243",
                |      "emailAddress": "Patrick.Dyson@example.com"
                |    },
                |    {
                |      "name": "Phillipa Dyson",
                |      "phone": "38390756246",
                |      "emailAddress": "Phillipa.Dyson@example.com"
                |    }
                |  ],
                |  "address": {
                |    "lineOne": "26424 Cecelia Junction",
                |    "lineTwo": "Suite 858",
                |    "lineThree": "Building Two",
                |    "lineFour": "West Siobhanberg",
                |    "postalCode": "OX2 3HD",
                |    "countryCode": "GB"
                |  },
                |  "reportingNotification": {
                |    "type": "RPO",
                |    "isActiveSeller": true,
                |    "isDueDiligence": false,
                |    "year": 2024
                |  }
                |}
                |""".stripMargin)
            .futureValue

          assertAsExpected(
            response = response,
            status = BAD_REQUEST,
            jsonBodyOpt = Some("""
                |[
                |  {
                |    "code": "invalid-internal-name"
                |  }
                |]
                |""".stripMargin)
          )
          verifyThatDownstreamApiWasNotCalled()
        }
      }
      "the business name, which is" - {
        "invalid (too long)" in {
          val response = wsClient
            .url(fullUrl("/platform-operators/345567808"))
            .withHttpHeaders(("Content-Type", "application/json"))
            .post("""
                |{
                |  "internalName": "Dyson",
                |  "businessName": "A long business name for the Platform Operator. A long business name for the Platform Operator. Maximum is 105.",
                |  "tradingName": "Dyson",
                |  "ids": [
                |    {
                |      "type": "UTR",
                |      "value": "1234567890",
                |      "countryCodeOfIssue": "GB"
                |    }
                |  ],
                |  "contacts": [
                |    {
                |      "name": "Patrick Dyson",
                |      "phone": "38390756243",
                |      "emailAddress": "Patrick.Dyson@example.com"
                |    },
                |    {
                |      "name": "Phillipa Dyson",
                |      "phone": "38390756246",
                |      "emailAddress": "Phillipa.Dyson@example.com"
                |    }
                |  ],
                |  "address": {
                |    "lineOne": "26424 Cecelia Junction",
                |    "lineTwo": "Suite 858",
                |    "lineThree": "Building Two",
                |    "lineFour": "West Siobhanberg",
                |    "postalCode": "OX2 3HD",
                |    "countryCode": "GB"
                |  },
                |  "reportingNotification": {
                |    "type": "RPO",
                |    "isActiveSeller": true,
                |    "isDueDiligence": false,
                |    "year": 2024
                |  }
                |}
                |""".stripMargin)
            .futureValue

          assertAsExpected(
            response = response,
            status = BAD_REQUEST,
            jsonBodyOpt = Some("""
                |[
                |  {
                |    "code": "invalid-business-name"
                |  }
                |]
                |""".stripMargin)
          )
          verifyThatDownstreamApiWasNotCalled()
        }
      }
      "the trading name, which is" - {
        "invalid (too short)" in {
          val response = wsClient
            .url(fullUrl("/platform-operators/345567808"))
            .withHttpHeaders(("Content-Type", "application/json"))
            .post("""
                |{
                |  "internalName": "Dyson",
                |  "businessName": "Dyson Inc.",
                |  "tradingName": "",
                |  "ids": [
                |    {
                |      "type": "UTR",
                |      "value": "1234567890",
                |      "countryCodeOfIssue": "GB"
                |    }
                |  ],
                |  "contacts": [
                |    {
                |      "name": "Patrick Dyson",
                |      "phone": "38390756243",
                |      "emailAddress": "Patrick.Dyson@example.com"
                |    },
                |    {
                |      "name": "Phillipa Dyson",
                |      "phone": "38390756246",
                |      "emailAddress": "Phillipa.Dyson@example.com"
                |    }
                |  ],
                |  "address": {
                |    "lineOne": "26424 Cecelia Junction",
                |    "lineTwo": "Suite 858",
                |    "lineThree": "Building Two",
                |    "lineFour": "West Siobhanberg",
                |    "postalCode": "OX2 3HD",
                |    "countryCode": "GB"
                |  },
                |  "reportingNotification": {
                |    "type": "RPO",
                |    "isActiveSeller": true,
                |    "isDueDiligence": false,
                |    "year": 2024
                |  }
                |}
                |""".stripMargin)
            .futureValue

          assertAsExpected(
            response = response,
            status = BAD_REQUEST,
            jsonBodyOpt = Some("""
                |[
                |  {
                |    "code": "invalid-trading-name"
                |  }
                |]
                |""".stripMargin)
          )
          verifyThatDownstreamApiWasNotCalled()
        }
      }
      "the ids, when some of them has" - {
        "a type, which is" - {
          "absent" in {
            val response = wsClient
              .url(fullUrl("/platform-operators/345567808"))
              .withHttpHeaders(("Content-Type", "application/json"))
              .post("""
                  |{
                  |  "internalName": "Dyson",
                  |  "businessName": "Dyson Inc.",
                  |  "tradingName": "Dyson",
                  |  "ids": [
                  |    {
                  |      "value": "1234567890",
                  |      "countryCodeOfIssue": "GB"
                  |    },
                  |    {
                  |      "value": "007",
                  |      "countryCodeOfIssue": "GB"
                  |    }
                  |  ],
                  |  "contacts": [
                  |    {
                  |      "name": "Patrick Dyson",
                  |      "phone": "38390756243",
                  |      "emailAddress": "Patrick.Dyson@example.com"
                  |    },
                  |    {
                  |      "name": "Phillipa Dyson",
                  |      "phone": "38390756246",
                  |      "emailAddress": "Phillipa.Dyson@example.com"
                  |    }
                  |  ],
                  |  "address": {
                  |    "lineOne": "26424 Cecelia Junction",
                  |    "lineTwo": "Suite 858",
                  |    "lineThree": "Building Two",
                  |    "lineFour": "West Siobhanberg",
                  |    "postalCode": "OX2 3HD",
                  |    "countryCode": "GB"
                  |  },
                  |  "reportingNotification": {
                  |    "type": "RPO",
                  |    "isActiveSeller": true,
                  |    "isDueDiligence": false,
                  |    "year": 2024
                  |  }
                  |}
                  |""".stripMargin)
              .futureValue

            assertAsExpected(
              response = response,
              status = BAD_REQUEST,
              jsonBodyOpt = Some("""
                  |[
                  |  {
                  |    "code": "invalid-id-1-type"
                  |  },
                  |  {
                  |    "code": "invalid-id-2-type"
                  |  }
                  |]
                  |""".stripMargin)
            )
            verifyThatDownstreamApiWasNotCalled()
          }
          "invalid (unrecognised)" in {
            val response = wsClient
              .url(fullUrl("/platform-operators/345567808"))
              .withHttpHeaders(("Content-Type", "application/json"))
              .post("""
                  |{
                  |  "internalName": "Dyson",
                  |  "businessName": "Dyson Inc.",
                  |  "tradingName": "Dyson",
                  |  "ids": [
                  |    {
                  |      "type": "PASSPORT",
                  |      "value": "1234567890",
                  |      "countryCodeOfIssue": "GB"
                  |    },
                  |    {
                  |      "type": "DRV",
                  |      "value": "007",
                  |      "countryCodeOfIssue": "GB"
                  |    }
                  |  ],
                  |  "contacts": [
                  |    {
                  |      "name": "Patrick Dyson",
                  |      "phone": "38390756243",
                  |      "emailAddress": "Patrick.Dyson@example.com"
                  |    },
                  |    {
                  |      "name": "Phillipa Dyson",
                  |      "phone": "38390756246",
                  |      "emailAddress": "Phillipa.Dyson@example.com"
                  |    }
                  |  ],
                  |  "address": {
                  |    "lineOne": "26424 Cecelia Junction",
                  |    "lineTwo": "Suite 858",
                  |    "lineThree": "Building Two",
                  |    "lineFour": "West Siobhanberg",
                  |    "postalCode": "OX2 3HD",
                  |    "countryCode": "GB"
                  |  },
                  |  "reportingNotification": {
                  |    "type": "RPO",
                  |    "isActiveSeller": true,
                  |    "isDueDiligence": false,
                  |    "year": 2024
                  |  }
                  |}
                  |""".stripMargin)
              .futureValue

            assertAsExpected(
              response = response,
              status = BAD_REQUEST,
              jsonBodyOpt = Some("""
                  |[
                  |  {
                  |    "code": "invalid-id-1-type"
                  |  },
                  |  {
                  |    "code": "invalid-id-2-type"
                  |  }
                  |]
                  |""".stripMargin)
            )
            verifyThatDownstreamApiWasNotCalled()
          }
        }
        "a value, which is" - {
          "absent" in {
            val response = wsClient
              .url(fullUrl("/platform-operators/345567808"))
              .withHttpHeaders(("Content-Type", "application/json"))
              .post("""
                  |{
                  |  "internalName": "Dyson",
                  |  "businessName": "Dyson Inc.",
                  |  "tradingName": "Dyson",
                  |  "ids": [
                  |    {
                  |      "type": "UTR",
                  |      "countryCodeOfIssue": "GB"
                  |    },
                  |    {
                  |      "type": "OTHER",
                  |      "countryCodeOfIssue": "GB"
                  |    }
                  |  ],
                  |  "contacts": [
                  |    {
                  |      "name": "Patrick Dyson",
                  |      "phone": "38390756243",
                  |      "emailAddress": "Patrick.Dyson@example.com"
                  |    },
                  |    {
                  |      "name": "Phillipa Dyson",
                  |      "phone": "38390756246",
                  |      "emailAddress": "Phillipa.Dyson@example.com"
                  |    }
                  |  ],
                  |  "address": {
                  |    "lineOne": "26424 Cecelia Junction",
                  |    "lineTwo": "Suite 858",
                  |    "lineThree": "Building Two",
                  |    "lineFour": "West Siobhanberg",
                  |    "postalCode": "OX2 3HD",
                  |    "countryCode": "GB"
                  |  },
                  |  "reportingNotification": {
                  |    "type": "RPO",
                  |    "isActiveSeller": true,
                  |    "isDueDiligence": false,
                  |    "year": 2024
                  |  }
                  |}
                  |""".stripMargin)
              .futureValue

            assertAsExpected(
              response = response,
              status = BAD_REQUEST,
              jsonBodyOpt = Some("""
                  |[
                  |  {
                  |    "code": "invalid-id-1-value"
                  |  },
                  |  {
                  |    "code": "invalid-id-2-value"
                  |  }
                  |]
                  |""".stripMargin)
            )
            verifyThatDownstreamApiWasNotCalled()
          }
          "invalid (too long)" in {
            val response = wsClient
              .url(fullUrl("/platform-operators/345567808"))
              .withHttpHeaders(("Content-Type", "application/json"))
              .post("""
                  |{
                  |  "internalName": "Dyson",
                  |  "businessName": "Dyson Inc.",
                  |  "tradingName": "Dyson",
                  |  "ids": [
                  |    {
                  |      "type": "UTR",
                  |      "value": "123456789012345678901234567890",
                  |      "countryCodeOfIssue": "GB"
                  |    },
                  |    {
                  |      "type": "OTHER",
                  |      "value": "123456789012345678901234567890",
                  |      "countryCodeOfIssue": "GB"
                  |    }
                  |  ],
                  |  "contacts": [
                  |    {
                  |      "name": "Patrick Dyson",
                  |      "phone": "38390756243",
                  |      "emailAddress": "Patrick.Dyson@example.com"
                  |    },
                  |    {
                  |      "name": "Phillipa Dyson",
                  |      "phone": "38390756246",
                  |      "emailAddress": "Phillipa.Dyson@example.com"
                  |    }
                  |  ],
                  |  "address": {
                  |    "lineOne": "26424 Cecelia Junction",
                  |    "lineTwo": "Suite 858",
                  |    "lineThree": "Building Two",
                  |    "lineFour": "West Siobhanberg",
                  |    "postalCode": "OX2 3HD",
                  |    "countryCode": "GB"
                  |  },
                  |  "reportingNotification": {
                  |    "type": "RPO",
                  |    "isActiveSeller": true,
                  |    "isDueDiligence": false,
                  |    "year": 2024
                  |  }
                  |}
                  |""".stripMargin)
              .futureValue

            assertAsExpected(
              response = response,
              status = BAD_REQUEST,
              jsonBodyOpt = Some("""
                  |[
                  |  {
                  |    "code": "invalid-id-1-value"
                  |  },
                  |  {
                  |    "code": "invalid-id-2-value"
                  |  }
                  |]
                  |""".stripMargin)
            )
            verifyThatDownstreamApiWasNotCalled()
          }
        }
        "a country code of issue, which is" - {
          "absent" in {
            val response = wsClient
              .url(fullUrl("/platform-operators/345567808"))
              .withHttpHeaders(("Content-Type", "application/json"))
              .post("""
                  |{
                  |  "internalName": "Dyson",
                  |  "businessName": "Dyson Inc.",
                  |  "tradingName": "Dyson",
                  |  "ids": [
                  |    {
                  |      "type": "UTR",
                  |      "value": "1234567890"
                  |    },
                  |    {
                  |      "type": "OTHER",
                  |      "value": "007"
                  |    }
                  |  ],
                  |  "contacts": [
                  |    {
                  |      "name": "Patrick Dyson",
                  |      "phone": "38390756243",
                  |      "emailAddress": "Patrick.Dyson@example.com"
                  |    },
                  |    {
                  |      "name": "Phillipa Dyson",
                  |      "phone": "38390756246",
                  |      "emailAddress": "Phillipa.Dyson@example.com"
                  |    }
                  |  ],
                  |  "address": {
                  |    "lineOne": "26424 Cecelia Junction",
                  |    "lineTwo": "Suite 858",
                  |    "lineThree": "Building Two",
                  |    "lineFour": "West Siobhanberg",
                  |    "postalCode": "OX2 3HD",
                  |    "countryCode": "GB"
                  |  },
                  |  "reportingNotification": {
                  |    "type": "RPO",
                  |    "isActiveSeller": true,
                  |    "isDueDiligence": false,
                  |    "year": 2024
                  |  }
                  |}
                  |""".stripMargin)
              .futureValue

            assertAsExpected(
              response = response,
              status = BAD_REQUEST,
              jsonBodyOpt = Some("""
                  |[
                  |  {
                  |    "code": "invalid-id-1-country-code-of-issue"
                  |  },
                  |  {
                  |    "code": "invalid-id-2-country-code-of-issue"
                  |  }
                  |]
                  |""".stripMargin)
            )
            verifyThatDownstreamApiWasNotCalled()
          }
          "invalid (unrecognised)" in {
            val response = wsClient
              .url(fullUrl("/platform-operators/345567808"))
              .withHttpHeaders(("Content-Type", "application/json"))
              .post("""
                  |{
                  |  "internalName": "Dyson",
                  |  "businessName": "Dyson Inc.",
                  |  "tradingName": "Dyson",
                  |  "ids": [
                  |    {
                  |      "type": "UTR",
                  |      "value": "1234567890",
                  |      "countryCodeOfIssue": "XX"
                  |    },
                  |    {
                  |      "type": "OTHER",
                  |      "value": "007",
                  |      "countryCodeOfIssue": "ZZ"
                  |    }
                  |  ],
                  |  "contacts": [
                  |    {
                  |      "name": "Patrick Dyson",
                  |      "phone": "38390756243",
                  |      "emailAddress": "Patrick.Dyson@example.com"
                  |    },
                  |    {
                  |      "name": "Phillipa Dyson",
                  |      "phone": "38390756246",
                  |      "emailAddress": "Phillipa.Dyson@example.com"
                  |    }
                  |  ],
                  |  "address": {
                  |    "lineOne": "26424 Cecelia Junction",
                  |    "lineTwo": "Suite 858",
                  |    "lineThree": "Building Two",
                  |    "lineFour": "West Siobhanberg",
                  |    "postalCode": "OX2 3HD",
                  |    "countryCode": "GB"
                  |  },
                  |  "reportingNotification": {
                  |    "type": "RPO",
                  |    "isActiveSeller": true,
                  |    "isDueDiligence": false,
                  |    "year": 2024
                  |  }
                  |}
                  |""".stripMargin)
              .futureValue

            assertAsExpected(
              response = response,
              status = BAD_REQUEST,
              jsonBodyOpt = Some("""
                  |[
                  |  {
                  |    "code": "invalid-id-1-country-code-of-issue"
                  |  },
                  |  {
                  |    "code": "invalid-id-2-country-code-of-issue"
                  |  }
                  |]
                  |""".stripMargin)
            )
            verifyThatDownstreamApiWasNotCalled()
          }
        }
      }
      "the contacts, when" - {
        "there are none" in {
          val response = wsClient
            .url(fullUrl("/platform-operators/345567808"))
            .withHttpHeaders(("Content-Type", "application/json"))
            .post("""
                |{
                |  "internalName": "Dyson",
                |  "businessName": "Dyson Inc.",
                |  "tradingName": "Dyson",
                |  "ids": [
                |    {
                |      "type": "UTR",
                |      "value": "1234567890",
                |      "countryCodeOfIssue": "GB"
                |    },
                |    {
                |      "type": "OTHER",
                |      "value": "007",
                |      "countryCodeOfIssue": "GB"
                |    }
                |  ],
                |  "contacts": [
                |  ],
                |  "address": {
                |    "lineOne": "26424 Cecelia Junction",
                |    "lineTwo": "Suite 858",
                |    "lineThree": "Building Two",
                |    "lineFour": "West Siobhanberg",
                |    "postalCode": "OX2 3HD",
                |    "countryCode": "GB"
                |  },
                |  "reportingNotification": {
                |    "type": "RPO",
                |    "isActiveSeller": true,
                |    "isDueDiligence": false,
                |    "year": 2024
                |  }
                |}
                |""".stripMargin)
            .futureValue

          assertAsExpected(
            response = response,
            status = BAD_REQUEST,
            jsonBodyOpt = Some("""
                |[
                |  {
                |    "code": "invalid-number-of-contacts"
                |  }
                |]
                |""".stripMargin)
          )
          verifyThatDownstreamApiWasNotCalled()
        }
        "there are three" in {
          val response = wsClient
            .url(fullUrl("/platform-operators/345567808"))
            .withHttpHeaders(("Content-Type", "application/json"))
            .post("""
                |{
                |  "internalName": "Dyson",
                |  "businessName": "Dyson Inc.",
                |  "tradingName": "Dyson",
                |  "ids": [
                |    {
                |      "type": "UTR",
                |      "value": "1234567890",
                |      "countryCodeOfIssue": "GB"
                |    },
                |    {
                |      "type": "OTHER",
                |      "value": "007",
                |      "countryCodeOfIssue": "GB"
                |    }
                |  ],
                |  "contacts": [
                |    {
                |      "name": "Patrick Dyson",
                |      "phone": "38390756243",
                |      "emailAddress": "Patrick.Dyson@example.com"
                |    },
                |    {
                |      "name": "Phillipa Dyson",
                |      "phone": "38390756246",
                |      "emailAddress": "Phillipa.Dyson@example.com"
                |    },
                |    {
                |      "name": "Paul Dyson",
                |      "phone": "38390756248",
                |      "emailAddress": "Paul.Dyson@example.com"
                |    }
                |  ],
                |  "address": {
                |    "lineOne": "26424 Cecelia Junction",
                |    "lineTwo": "Suite 858",
                |    "lineThree": "Building Two",
                |    "lineFour": "West Siobhanberg",
                |    "postalCode": "OX2 3HD",
                |    "countryCode": "GB"
                |  },
                |  "reportingNotification": {
                |    "type": "RPO",
                |    "isActiveSeller": true,
                |    "isDueDiligence": false,
                |    "year": 2024
                |  }
                |}
                |""".stripMargin)
            .futureValue

          assertAsExpected(
            response = response,
            status = BAD_REQUEST,
            jsonBodyOpt = Some("""
                |[
                |  {
                |    "code": "invalid-number-of-contacts"
                |  }
                |]
                |""".stripMargin)
          )
          verifyThatDownstreamApiWasNotCalled()
        }
        "one of them has" - {
          "a name, which is" - {
            "absent" in {
              val response = wsClient
                .url(fullUrl("/platform-operators/345567808"))
                .withHttpHeaders(("Content-Type", "application/json"))
                .post("""
                    |{
                    |  "internalName": "Dyson",
                    |  "businessName": "Dyson Inc.",
                    |  "tradingName": "Dyson",
                    |  "ids": [
                    |    {
                    |      "type": "UTR",
                    |      "value": "1234567890",
                    |      "countryCodeOfIssue": "GB"
                    |    },
                    |    {
                    |      "type": "OTHER",
                    |      "value": "007",
                    |      "countryCodeOfIssue": "GB"
                    |    }
                    |  ],
                    |  "contacts": [
                    |    {
                    |      "name": "Patrick Dyson",
                    |      "phone": "38390756243",
                    |      "emailAddress": "Patrick.Dyson@example.com"
                    |    },
                    |    {
                    |      "phone": "38390756246",
                    |      "emailAddress": "Phillipa.Dyson@example.com"
                    |    }
                    |  ],
                    |  "address": {
                    |    "lineOne": "26424 Cecelia Junction",
                    |    "lineTwo": "Suite 858",
                    |    "lineThree": "Building Two",
                    |    "lineFour": "West Siobhanberg",
                    |    "postalCode": "OX2 3HD",
                    |    "countryCode": "GB"
                    |  },
                    |  "reportingNotification": {
                    |    "type": "RPO",
                    |    "isActiveSeller": true,
                    |    "isDueDiligence": false,
                    |    "year": 2024
                    |  }
                    |}
                    |""".stripMargin)
                .futureValue

              assertAsExpected(
                response = response,
                status = BAD_REQUEST,
                jsonBodyOpt = Some("""
                    |[
                    |  {
                    |    "code": "invalid-contact-2-name"
                    |  }
                    |]
                    |""".stripMargin)
              )
              verifyThatDownstreamApiWasNotCalled()
            }
            "invalid (too long)" in {
              val response = wsClient
                .url(fullUrl("/platform-operators/345567808"))
                .withHttpHeaders(("Content-Type", "application/json"))
                .post("""
                    |{
                    |  "internalName": "Dyson",
                    |  "businessName": "Dyson Inc.",
                    |  "tradingName": "Dyson",
                    |  "ids": [
                    |    {
                    |      "type": "UTR",
                    |      "value": "1234567890",
                    |      "countryCodeOfIssue": "GB"
                    |    },
                    |    {
                    |      "type": "OTHER",
                    |      "value": "007",
                    |      "countryCodeOfIssue": "GB"
                    |    }
                    |  ],
                    |  "contacts": [
                    |    {
                    |      "name": "Phillipa Elisabeth Louis Parker Stephenson Musk Yeti Elisabeth Louis Parker Stephenson Musk Yeti Elisabeth Louis Parker Stephenson Musk Yeti Dyson",
                    |      "phone": "38390756243",
                    |      "emailAddress": "Patrick.Dyson@example.com"
                    |    },
                    |    {
                    |      "name": "Phillipa Dyson",
                    |      "phone": "38390756246",
                    |      "emailAddress": "Phillipa.Dyson@example.com"
                    |    }
                    |  ],
                    |  "address": {
                    |    "lineOne": "26424 Cecelia Junction",
                    |    "lineTwo": "Suite 858",
                    |    "lineThree": "Building Two",
                    |    "lineFour": "West Siobhanberg",
                    |    "postalCode": "OX2 3HD",
                    |    "countryCode": "GB"
                    |  },
                    |  "reportingNotification": {
                    |    "type": "RPO",
                    |    "isActiveSeller": true,
                    |    "isDueDiligence": false,
                    |    "year": 2024
                    |  }
                    |}
                    |""".stripMargin)
                .futureValue

              assertAsExpected(
                response = response,
                status = BAD_REQUEST,
                jsonBodyOpt = Some("""
                    |[
                    |  {
                    |    "code": "invalid-contact-1-name"
                    |  }
                    |]
                    |""".stripMargin)
              )
              verifyThatDownstreamApiWasNotCalled()
            }
          }
          "a phone number, which is" - {
            "invalid (format)" in {
              val response = wsClient
                .url(fullUrl("/platform-operators/345567808"))
                .withHttpHeaders(("Content-Type", "application/json"))
                .post("""
                    |{
                    |  "internalName": "Dyson",
                    |  "businessName": "Dyson Inc.",
                    |  "tradingName": "Dyson",
                    |  "ids": [
                    |    {
                    |      "type": "UTR",
                    |      "value": "1234567890",
                    |      "countryCodeOfIssue": "GB"
                    |    },
                    |    {
                    |      "type": "OTHER",
                    |      "value": "007",
                    |      "countryCodeOfIssue": "GB"
                    |    }
                    |  ],
                    |  "contacts": [
                    |    {
                    |      "name": "Patrick Dyson",
                    |      "phone": "38390756243",
                    |      "emailAddress": "Patrick.Dyson@example.com"
                    |    },
                    |    {
                    |      "name": "Phillipa Dyson",
                    |      "phone": "£38390756243",
                    |      "emailAddress": "Phillipa.Dyson@example.com"
                    |    }
                    |  ],
                    |  "address": {
                    |    "lineOne": "26424 Cecelia Junction",
                    |    "lineTwo": "Suite 858",
                    |    "lineThree": "Building Two",
                    |    "lineFour": "West Siobhanberg",
                    |    "postalCode": "OX2 3HD",
                    |    "countryCode": "GB"
                    |  },
                    |  "reportingNotification": {
                    |    "type": "RPO",
                    |    "isActiveSeller": true,
                    |    "isDueDiligence": false,
                    |    "year": 2024
                    |  }
                    |}
                    |""".stripMargin)
                .futureValue

              assertAsExpected(
                response = response,
                status = BAD_REQUEST,
                jsonBodyOpt = Some("""
                    |[
                    |  {
                    |    "code": "invalid-contact-2-phone"
                    |  }
                    |]
                    |""".stripMargin)
              )
              verifyThatDownstreamApiWasNotCalled()
            }
          }
          "an email address, which is" - {
            "absent" in {
              val response = wsClient
                .url(fullUrl("/platform-operators/345567808"))
                .withHttpHeaders(("Content-Type", "application/json"))
                .post("""
                    |{
                    |  "internalName": "Dyson",
                    |  "businessName": "Dyson Inc.",
                    |  "tradingName": "Dyson",
                    |  "ids": [
                    |    {
                    |      "type": "UTR",
                    |      "value": "1234567890",
                    |      "countryCodeOfIssue": "GB"
                    |    },
                    |    {
                    |      "type": "OTHER",
                    |      "value": "007",
                    |      "countryCodeOfIssue": "GB"
                    |    }
                    |  ],
                    |  "contacts": [
                    |    {
                    |      "name": "Patrick Dyson",
                    |      "phone": "38390756243",
                    |      "emailAddress": "Patrick.Dyson@example.com"
                    |    },
                    |    {
                    |      "name": "Phillipa Dyson",
                    |      "phone": "38390756246"
                    |    }
                    |  ],
                    |  "address": {
                    |    "lineOne": "26424 Cecelia Junction",
                    |    "lineTwo": "Suite 858",
                    |    "lineThree": "Building Two",
                    |    "lineFour": "West Siobhanberg",
                    |    "postalCode": "OX2 3HD",
                    |    "countryCode": "GB"
                    |  },
                    |  "reportingNotification": {
                    |    "type": "RPO",
                    |    "isActiveSeller": true,
                    |    "isDueDiligence": false,
                    |    "year": 2024
                    |  }
                    |}
                    |""".stripMargin)
                .futureValue

              assertAsExpected(
                response = response,
                status = BAD_REQUEST,
                jsonBodyOpt = Some("""
                    |[
                    |  {
                    |    "code": "invalid-contact-2-email-address"
                    |  }
                    |]
                    |""".stripMargin)
              )
              verifyThatDownstreamApiWasNotCalled()
            }
            "invalid (format)" in {
              val response = wsClient
                .url(fullUrl("/platform-operators/345567808"))
                .withHttpHeaders(("Content-Type", "application/json"))
                .post("""
                    |{
                    |  "internalName": "Dyson",
                    |  "businessName": "Dyson Inc.",
                    |  "tradingName": "Dyson",
                    |  "ids": [
                    |    {
                    |      "type": "UTR",
                    |      "value": "1234567890",
                    |      "countryCodeOfIssue": "GB"
                    |    },
                    |    {
                    |      "type": "OTHER",
                    |      "value": "007",
                    |      "countryCodeOfIssue": "GB"
                    |    }
                    |  ],
                    |  "contacts": [
                    |    {
                    |      "name": "Patrick Dyson",
                    |      "phone": "38390756243",
                    |      "emailAddress": "Patrick.Dyson@example.com"
                    |    },
                    |    {
                    |      "name": "Phillipa Dyson",
                    |      "phone": "38390756246",
                    |      "emailAddress": "Phillipa.Dyson@"
                    |    }
                    |  ],
                    |  "address": {
                    |    "lineOne": "26424 Cecelia Junction",
                    |    "lineTwo": "Suite 858",
                    |    "lineThree": "Building Two",
                    |    "lineFour": "West Siobhanberg",
                    |    "postalCode": "OX2 3HD",
                    |    "countryCode": "GB"
                    |  },
                    |  "reportingNotification": {
                    |    "type": "RPO",
                    |    "isActiveSeller": true,
                    |    "isDueDiligence": false,
                    |    "year": 2024
                    |  }
                    |}
                    |""".stripMargin)
                .futureValue

              assertAsExpected(
                response = response,
                status = BAD_REQUEST,
                jsonBodyOpt = Some("""
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
      }
      "the address, where" - {
        "line one is" - {
          "absent" in {
            val response = wsClient
              .url(fullUrl("/platform-operators/345567808"))
              .withHttpHeaders(("Content-Type", "application/json"))
              .post("""
                  |{
                  |  "internalName": "Dyson",
                  |  "businessName": "Dyson Inc.",
                  |  "tradingName": "Dyson",
                  |  "ids": [
                  |    {
                  |      "type": "UTR",
                  |      "value": "1234567890",
                  |      "countryCodeOfIssue": "GB"
                  |    },
                  |    {
                  |      "type": "OTHER",
                  |      "value": "007",
                  |      "countryCodeOfIssue": "GB"
                  |    }
                  |  ],
                  |  "contacts": [
                  |    {
                  |      "name": "Patrick Dyson",
                  |      "phone": "38390756243",
                  |      "emailAddress": "Patrick.Dyson@example.com"
                  |    },
                  |    {
                  |      "name": "Phillipa Dyson",
                  |      "phone": "38390756246",
                  |      "emailAddress": "Phillipa.Dyson@example.com"
                  |    }
                  |  ],
                  |  "address": {
                  |    "lineTwo": "Suite 858",
                  |    "lineThree": "Building Two",
                  |    "lineFour": "West Siobhanberg",
                  |    "postalCode": "OX2 3HD",
                  |    "countryCode": "GB"
                  |  },
                  |  "reportingNotification": {
                  |    "type": "RPO",
                  |    "isActiveSeller": true,
                  |    "isDueDiligence": false,
                  |    "year": 2024
                  |  }
                  |}
                  |""".stripMargin)
              .futureValue

            assertAsExpected(
              response = response,
              status = BAD_REQUEST,
              jsonBodyOpt = Some("""
                  |[
                  |  {
                  |    "code": "invalid-address-line-one"
                  |  }
                  |]
                  |""".stripMargin)
            )
            verifyThatDownstreamApiWasNotCalled()
          }
          "invalid (too short)" in {
            val response = wsClient
              .url(fullUrl("/platform-operators/345567808"))
              .withHttpHeaders(("Content-Type", "application/json"))
              .post("""
                  |{
                  |  "internalName": "Dyson",
                  |  "businessName": "Dyson Inc.",
                  |  "tradingName": "Dyson",
                  |  "ids": [
                  |    {
                  |      "type": "UTR",
                  |      "value": "1234567890",
                  |      "countryCodeOfIssue": "GB"
                  |    },
                  |    {
                  |      "type": "OTHER",
                  |      "value": "007",
                  |      "countryCodeOfIssue": "GB"
                  |    }
                  |  ],
                  |  "contacts": [
                  |    {
                  |      "name": "Patrick Dyson",
                  |      "phone": "38390756243",
                  |      "emailAddress": "Patrick.Dyson@example.com"
                  |    },
                  |    {
                  |      "name": "Phillipa Dyson",
                  |      "phone": "38390756246",
                  |      "emailAddress": "Phillipa.Dyson@example.com"
                  |    }
                  |  ],
                  |  "address": {
                  |    "lineOne": "",
                  |    "lineTwo": "Suite 858",
                  |    "lineThree": "Building Two",
                  |    "lineFour": "West Siobhanberg",
                  |    "postalCode": "OX2 3HD",
                  |    "countryCode": "GB"
                  |  },
                  |  "reportingNotification": {
                  |    "type": "RPO",
                  |    "isActiveSeller": true,
                  |    "isDueDiligence": false,
                  |    "year": 2024
                  |  }
                  |}
                  |""".stripMargin)
              .futureValue

            assertAsExpected(
              response = response,
              status = BAD_REQUEST,
              jsonBodyOpt = Some("""
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
        "line two is" - {
          "absent" in {
            val response = wsClient
              .url(fullUrl("/platform-operators/345567808"))
              .withHttpHeaders(("Content-Type", "application/json"))
              .post("""
                  |{
                  |  "internalName": "Dyson",
                  |  "businessName": "Dyson Inc.",
                  |  "tradingName": "Dyson",
                  |  "ids": [
                  |    {
                  |      "type": "UTR",
                  |      "value": "1234567890",
                  |      "countryCodeOfIssue": "GB"
                  |    },
                  |    {
                  |      "type": "OTHER",
                  |      "value": "007",
                  |      "countryCodeOfIssue": "GB"
                  |    }
                  |  ],
                  |  "contacts": [
                  |    {
                  |      "name": "Patrick Dyson",
                  |      "phone": "38390756243",
                  |      "emailAddress": "Patrick.Dyson@example.com"
                  |    },
                  |    {
                  |      "name": "Phillipa Dyson",
                  |      "phone": "38390756246",
                  |      "emailAddress": "Phillipa.Dyson@example.com"
                  |    }
                  |  ],
                  |  "address": {
                  |    "lineOne": "26424 Cecelia Junction",
                  |    "lineThree": "Building Two",
                  |    "lineFour": "West Siobhanberg",
                  |    "postalCode": "OX2 3HD",
                  |    "countryCode": "GB"
                  |  },
                  |  "reportingNotification": {
                  |    "type": "RPO",
                  |    "isActiveSeller": true,
                  |    "isDueDiligence": false,
                  |    "year": 2024
                  |  }
                  |}
                  |""".stripMargin)
              .futureValue

            assertAsExpected(
              response = response,
              status = BAD_REQUEST,
              jsonBodyOpt = Some("""
                  |[
                  |  {
                  |    "code": "invalid-address-line-two"
                  |  }
                  |]
                  |""".stripMargin)
            )
            verifyThatDownstreamApiWasNotCalled()
          }
          "invalid (too long)" in {
            val response = wsClient
              .url(fullUrl("/platform-operators/345567808"))
              .withHttpHeaders(("Content-Type", "application/json"))
              .post("""
                  |{
                  |  "internalName": "Dyson",
                  |  "businessName": "Dyson Inc.",
                  |  "tradingName": "Dyson",
                  |  "ids": [
                  |    {
                  |      "type": "UTR",
                  |      "value": "1234567890",
                  |      "countryCodeOfIssue": "GB"
                  |    },
                  |    {
                  |      "type": "OTHER",
                  |      "value": "007",
                  |      "countryCodeOfIssue": "GB"
                  |    }
                  |  ],
                  |  "contacts": [
                  |    {
                  |      "name": "Patrick Dyson",
                  |      "phone": "38390756243",
                  |      "emailAddress": "Patrick.Dyson@example.com"
                  |    },
                  |    {
                  |      "name": "Phillipa Dyson",
                  |      "phone": "38390756246",
                  |      "emailAddress": "Phillipa.Dyson@example.com"
                  |    }
                  |  ],
                  |  "address": {
                  |    "lineOne": "26424 Cecelia Junction",
                  |    "lineTwo": "Suite 858834583594358345834593453423",
                  |    "lineThree": "Building Two",
                  |    "lineFour": "West Siobhanberg",
                  |    "postalCode": "OX2 3HD",
                  |    "countryCode": "GB"
                  |  },
                  |  "reportingNotification": {
                  |    "type": "RPO",
                  |    "isActiveSeller": true,
                  |    "isDueDiligence": false,
                  |    "year": 2024
                  |  }
                  |}
                  |""".stripMargin)
              .futureValue

            assertAsExpected(
              response = response,
              status = BAD_REQUEST,
              jsonBodyOpt = Some("""
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
        "line three is" - {
          "absent" in {
            val response = wsClient
              .url(fullUrl("/platform-operators/345567808"))
              .withHttpHeaders(("Content-Type", "application/json"))
              .post("""
                  |{
                  |  "internalName": "Dyson",
                  |  "businessName": "Dyson Inc.",
                  |  "tradingName": "Dyson",
                  |  "ids": [
                  |    {
                  |      "type": "UTR",
                  |      "value": "1234567890",
                  |      "countryCodeOfIssue": "GB"
                  |    },
                  |    {
                  |      "type": "OTHER",
                  |      "value": "007",
                  |      "countryCodeOfIssue": "GB"
                  |    }
                  |  ],
                  |  "contacts": [
                  |    {
                  |      "name": "Patrick Dyson",
                  |      "phone": "38390756243",
                  |      "emailAddress": "Patrick.Dyson@example.com"
                  |    },
                  |    {
                  |      "name": "Phillipa Dyson",
                  |      "phone": "38390756246",
                  |      "emailAddress": "Phillipa.Dyson@example.com"
                  |    }
                  |  ],
                  |  "address": {
                  |    "lineOne": "26424 Cecelia Junction",
                  |    "lineTwo": "Suite 858",
                  |    "lineFour": "West Siobhanberg",
                  |    "postalCode": "OX2 3HD",
                  |    "countryCode": "GB"
                  |  },
                  |  "reportingNotification": {
                  |    "type": "RPO",
                  |    "isActiveSeller": true,
                  |    "isDueDiligence": false,
                  |    "year": 2024
                  |  }
                  |}
                  |""".stripMargin)
              .futureValue

            assertAsExpected(
              response = response,
              status = BAD_REQUEST,
              jsonBodyOpt = Some("""
                  |[
                  |  {
                  |    "code": "invalid-address-line-three"
                  |  }
                  |]
                  |""".stripMargin)
            )
            verifyThatDownstreamApiWasNotCalled()
          }
          "invalid (too short)" in {
            val response = wsClient
              .url(fullUrl("/platform-operators/345567808"))
              .withHttpHeaders(("Content-Type", "application/json"))
              .post("""
                  |{
                  |  "internalName": "Dyson",
                  |  "businessName": "Dyson Inc.",
                  |  "tradingName": "Dyson",
                  |  "ids": [
                  |    {
                  |      "type": "UTR",
                  |      "value": "1234567890",
                  |      "countryCodeOfIssue": "GB"
                  |    },
                  |    {
                  |      "type": "OTHER",
                  |      "value": "007",
                  |      "countryCodeOfIssue": "GB"
                  |    }
                  |  ],
                  |  "contacts": [
                  |    {
                  |      "name": "Patrick Dyson",
                  |      "phone": "38390756243",
                  |      "emailAddress": "Patrick.Dyson@example.com"
                  |    },
                  |    {
                  |      "name": "Phillipa Dyson",
                  |      "phone": "38390756246",
                  |      "emailAddress": "Phillipa.Dyson@example.com"
                  |    }
                  |  ],
                  |  "address": {
                  |    "lineOne": "26424 Cecelia Junction",
                  |    "lineTwo": "Suite 858",
                  |    "lineThree": "",
                  |    "lineFour": "West Siobhanberg",
                  |    "postalCode": "OX2 3HD",
                  |    "countryCode": "GB"
                  |  },
                  |  "reportingNotification": {
                  |    "type": "RPO",
                  |    "isActiveSeller": true,
                  |    "isDueDiligence": false,
                  |    "year": 2024
                  |  }
                  |}
                  |""".stripMargin)
              .futureValue

            assertAsExpected(
              response = response,
              status = BAD_REQUEST,
              jsonBodyOpt = Some("""
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
        "line four is" - {
          "invalid (too long)" in {
            val response = wsClient
              .url(fullUrl("/platform-operators/345567808"))
              .withHttpHeaders(("Content-Type", "application/json"))
              .post("""
                  |{
                  |  "internalName": "Dyson",
                  |  "businessName": "Dyson Inc.",
                  |  "tradingName": "Dyson",
                  |  "ids": [
                  |    {
                  |      "type": "UTR",
                  |      "value": "1234567890",
                  |      "countryCodeOfIssue": "GB"
                  |    },
                  |    {
                  |      "type": "OTHER",
                  |      "value": "007",
                  |      "countryCodeOfIssue": "GB"
                  |    }
                  |  ],
                  |  "contacts": [
                  |    {
                  |      "name": "Patrick Dyson",
                  |      "phone": "38390756243",
                  |      "emailAddress": "Patrick.Dyson@example.com"
                  |    },
                  |    {
                  |      "name": "Phillipa Dyson",
                  |      "phone": "38390756246",
                  |      "emailAddress": "Phillipa.Dyson@example.com"
                  |    }
                  |  ],
                  |  "address": {
                  |    "lineOne": "26424 Cecelia Junction",
                  |    "lineTwo": "Suite 858",
                  |    "lineThree": "Building Two",
                  |    "lineFour": "Just East of the Sun, West of the Moon",
                  |    "postalCode": "OX2 3HD",
                  |    "countryCode": "GB"
                  |  },
                  |  "reportingNotification": {
                  |    "type": "RPO",
                  |    "isActiveSeller": true,
                  |    "isDueDiligence": false,
                  |    "year": 2024
                  |  }
                  |}
                  |""".stripMargin)
              .futureValue

            assertAsExpected(
              response = response,
              status = BAD_REQUEST,
              jsonBodyOpt = Some("""
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
              .url(fullUrl("/platform-operators/345567808"))
              .withHttpHeaders(("Content-Type", "application/json"))
              .post("""
                  |{
                  |  "internalName": "Dyson",
                  |  "businessName": "Dyson Inc.",
                  |  "tradingName": "Dyson",
                  |  "ids": [
                  |    {
                  |      "type": "UTR",
                  |      "value": "1234567890",
                  |      "countryCodeOfIssue": "GB"
                  |    },
                  |    {
                  |      "type": "OTHER",
                  |      "value": "007",
                  |      "countryCodeOfIssue": "GB"
                  |    }
                  |  ],
                  |  "contacts": [
                  |    {
                  |      "name": "Patrick Dyson",
                  |      "phone": "38390756243",
                  |      "emailAddress": "Patrick.Dyson@example.com"
                  |    },
                  |    {
                  |      "name": "Phillipa Dyson",
                  |      "phone": "38390756246",
                  |      "emailAddress": "Phillipa.Dyson@example.com"
                  |    }
                  |  ],
                  |  "address": {
                  |    "lineOne": "26424 Cecelia Junction",
                  |    "lineTwo": "Suite 858",
                  |    "lineThree": "Building Two",
                  |    "lineFour": "West Siobhanberg",
                  |    "postalCode": "OX2 3HD"
                  |  },
                  |  "reportingNotification": {
                  |    "type": "RPO",
                  |    "isActiveSeller": true,
                  |    "isDueDiligence": false,
                  |    "year": 2024
                  |  }
                  |}
                  |""".stripMargin)
              .futureValue

            assertAsExpected(
              response = response,
              status = BAD_REQUEST,
              jsonBodyOpt = Some("""
                  |[
                  |  {
                  |    "code": "invalid-address-country-code"
                  |  }
                  |]
                  |""".stripMargin)
            )
            verifyThatDownstreamApiWasNotCalled()
          }
          "invalid (unrecognised)" in {
            val response = wsClient
              .url(fullUrl("/platform-operators/345567808"))
              .withHttpHeaders(("Content-Type", "application/json"))
              .post("""
                  |{
                  |  "internalName": "Dyson",
                  |  "businessName": "Dyson Inc.",
                  |  "tradingName": "Dyson",
                  |  "ids": [
                  |    {
                  |      "type": "UTR",
                  |      "value": "1234567890",
                  |      "countryCodeOfIssue": "GB"
                  |    },
                  |    {
                  |      "type": "OTHER",
                  |      "value": "007",
                  |      "countryCodeOfIssue": "GB"
                  |    }
                  |  ],
                  |  "contacts": [
                  |    {
                  |      "name": "Patrick Dyson",
                  |      "phone": "38390756243",
                  |      "emailAddress": "Patrick.Dyson@example.com"
                  |    },
                  |    {
                  |      "name": "Phillipa Dyson",
                  |      "phone": "38390756246",
                  |      "emailAddress": "Phillipa.Dyson@example.com"
                  |    }
                  |  ],
                  |  "address": {
                  |    "lineOne": "26424 Cecelia Junction",
                  |    "lineTwo": "Suite 858",
                  |    "lineThree": "Building Two",
                  |    "lineFour": "West Siobhanberg",
                  |    "postalCode": "OX2 3HD",
                  |    "countryCode": "XX"
                  |  },
                  |  "reportingNotification": {
                  |    "type": "RPO",
                  |    "isActiveSeller": true,
                  |    "isDueDiligence": false,
                  |    "year": 2024
                  |  }
                  |}
                  |""".stripMargin)
              .futureValue

            assertAsExpected(
              response = response,
              status = BAD_REQUEST,
              jsonBodyOpt = Some("""
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
          "required, but" - {
            "absent" in {
              val response = wsClient
                .url(fullUrl("/platform-operators/345567808"))
                .withHttpHeaders(("Content-Type", "application/json"))
                .post("""
                    |{
                    |  "internalName": "Dyson",
                    |  "businessName": "Dyson Inc.",
                    |  "tradingName": "Dyson",
                    |  "ids": [
                    |    {
                    |      "type": "UTR",
                    |      "value": "1234567890",
                    |      "countryCodeOfIssue": "GB"
                    |    },
                    |    {
                    |      "type": "OTHER",
                    |      "value": "007",
                    |      "countryCodeOfIssue": "GB"
                    |    }
                    |  ],
                    |  "contacts": [
                    |    {
                    |      "name": "Patrick Dyson",
                    |      "phone": "38390756243",
                    |      "emailAddress": "Patrick.Dyson@example.com"
                    |    },
                    |    {
                    |      "name": "Phillipa Dyson",
                    |      "phone": "38390756246",
                    |      "emailAddress": "Phillipa.Dyson@example.com"
                    |    }
                    |  ],
                    |  "address": {
                    |    "lineOne": "26424 Cecelia Junction",
                    |    "lineTwo": "Suite 858",
                    |    "lineThree": "Building Two",
                    |    "lineFour": "West Siobhanberg",
                    |    "countryCode": "GB"
                    |  },
                    |  "reportingNotification": {
                    |    "type": "RPO",
                    |    "isActiveSeller": true,
                    |    "isDueDiligence": false,
                    |    "year": 2024
                    |  }
                    |}
                    |""".stripMargin)
                .futureValue

              assertAsExpected(
                response = response,
                status = BAD_REQUEST,
                jsonBodyOpt = Some("""
                    |[
                    |  {
                    |    "code": "invalid-address-postal-code"
                    |  }
                    |]
                    |""".stripMargin)
              )
              verifyThatDownstreamApiWasNotCalled()
            }
            "invalid (too long)" in {
              val response = wsClient
                .url(fullUrl("/platform-operators/345567808"))
                .withHttpHeaders(("Content-Type", "application/json"))
                .post("""
                    |{
                    |  "internalName": "Dyson",
                    |  "businessName": "Dyson Inc.",
                    |  "tradingName": "Dyson",
                    |  "ids": [
                    |    {
                    |      "type": "UTR",
                    |      "value": "1234567890",
                    |      "countryCodeOfIssue": "GB"
                    |    },
                    |    {
                    |      "type": "OTHER",
                    |      "value": "007",
                    |      "countryCodeOfIssue": "GB"
                    |    }
                    |  ],
                    |  "contacts": [
                    |    {
                    |      "name": "Patrick Dyson",
                    |      "phone": "38390756243",
                    |      "emailAddress": "Patrick.Dyson@example.com"
                    |    },
                    |    {
                    |      "name": "Phillipa Dyson",
                    |      "phone": "38390756246",
                    |      "emailAddress": "Phillipa.Dyson@example.com"
                    |    }
                    |  ],
                    |  "address": {
                    |    "lineOne": "26424 Cecelia Junction",
                    |    "lineTwo": "Suite 858",
                    |    "lineThree": "Building Two",
                    |    "lineFour": "West Siobhanberg",
                    |    "postalCode": "OX2 3HD OX2",
                    |    "countryCode": "GB"
                    |  },
                    |  "reportingNotification": {
                    |    "type": "RPO",
                    |    "isActiveSeller": true,
                    |    "isDueDiligence": false,
                    |    "year": 2024
                    |  }
                    |}
                    |""".stripMargin)
                .futureValue

              assertAsExpected(
                response = response,
                status = BAD_REQUEST,
                jsonBodyOpt = Some("""
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
          "not required, but" - {
            "invalid (too short)" in {
              val response = wsClient
                .url(fullUrl("/platform-operators/345567808"))
                .withHttpHeaders(("Content-Type", "application/json"))
                .post("""
                    |{
                    |  "internalName": "Dyson",
                    |  "businessName": "Dyson Inc.",
                    |  "tradingName": "Dyson",
                    |  "ids": [
                    |    {
                    |      "type": "UTR",
                    |      "value": "1234567890",
                    |      "countryCodeOfIssue": "GB"
                    |    },
                    |    {
                    |      "type": "OTHER",
                    |      "value": "007",
                    |      "countryCodeOfIssue": "GB"
                    |    }
                    |  ],
                    |  "contacts": [
                    |    {
                    |      "name": "Patrick Dyson",
                    |      "phone": "38390756243",
                    |      "emailAddress": "Patrick.Dyson@example.com"
                    |    },
                    |    {
                    |      "name": "Phillipa Dyson",
                    |      "phone": "38390756246",
                    |      "emailAddress": "Phillipa.Dyson@example.com"
                    |    }
                    |  ],
                    |  "address": {
                    |    "lineOne": "26424 Cecelia Junction",
                    |    "lineTwo": "Suite 858",
                    |    "lineThree": "Building Two",
                    |    "lineFour": "West Siobhanberg",
                    |    "postalCode": "",
                    |    "countryCode": "DK"
                    |  },
                    |  "reportingNotification": {
                    |    "type": "RPO",
                    |    "isActiveSeller": true,
                    |    "isDueDiligence": false,
                    |    "year": 2024
                    |  }
                    |}
                    |""".stripMargin)
                .futureValue

              assertAsExpected(
                response = response,
                status = BAD_REQUEST,
                jsonBodyOpt = Some("""
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
      }
      "the reporting notification, where" - {
        "the type is" - {
          "absent" in {
            val response = wsClient
              .url(fullUrl("/platform-operators/345567808"))
              .withHttpHeaders(("Content-Type", "application/json"))
              .post("""
                  |{
                  |  "internalName": "Dyson",
                  |  "businessName": "Dyson Inc.",
                  |  "tradingName": "Dyson",
                  |  "ids": [
                  |    {
                  |      "type": "UTR",
                  |      "value": "1234567890",
                  |      "countryCodeOfIssue": "GB"
                  |    },
                  |    {
                  |      "type": "OTHER",
                  |      "value": "007",
                  |      "countryCodeOfIssue": "GB"
                  |    }
                  |  ],
                  |  "contacts": [
                  |    {
                  |      "name": "Patrick Dyson",
                  |      "phone": "38390756243",
                  |      "emailAddress": "Patrick.Dyson@example.com"
                  |    },
                  |    {
                  |      "name": "Phillipa Dyson",
                  |      "phone": "38390756246",
                  |      "emailAddress": "Phillipa.Dyson@example.com"
                  |    }
                  |  ],
                  |  "address": {
                  |    "lineOne": "26424 Cecelia Junction",
                  |    "lineTwo": "Suite 858",
                  |    "lineThree": "Building Two",
                  |    "lineFour": "West Siobhanberg",
                  |    "postalCode": "OX2 3HD",
                  |    "countryCode": "GB"
                  |  },
                  |  "reportingNotification": {
                  |    "isActiveSeller": true,
                  |    "isDueDiligence": false,
                  |    "year": 2024
                  |  }
                  |}
                  |""".stripMargin)
              .futureValue

            assertAsExpected(
              response = response,
              status = BAD_REQUEST,
              jsonBodyOpt = Some("""
                  |[
                  |  {
                  |    "code": "invalid-reporting-notification-type"
                  |  }
                  |]
                  |""".stripMargin)
            )
            verifyThatDownstreamApiWasNotCalled()
          }
          "invalid (unrecognised)" in {
            val response = wsClient
              .url(fullUrl("/platform-operators/345567808"))
              .withHttpHeaders(("Content-Type", "application/json"))
              .post("""
                  |{
                  |  "internalName": "Dyson",
                  |  "businessName": "Dyson Inc.",
                  |  "tradingName": "Dyson",
                  |  "ids": [
                  |    {
                  |      "type": "UTR",
                  |      "value": "1234567890",
                  |      "countryCodeOfIssue": "GB"
                  |    },
                  |    {
                  |      "type": "OTHER",
                  |      "value": "007",
                  |      "countryCodeOfIssue": "GB"
                  |    }
                  |  ],
                  |  "contacts": [
                  |    {
                  |      "name": "Patrick Dyson",
                  |      "phone": "38390756243",
                  |      "emailAddress": "Patrick.Dyson@example.com"
                  |    },
                  |    {
                  |      "name": "Phillipa Dyson",
                  |      "phone": "38390756246",
                  |      "emailAddress": "Phillipa.Dyson@example.com"
                  |    }
                  |  ],
                  |  "address": {
                  |    "lineOne": "26424 Cecelia Junction",
                  |    "lineTwo": "Suite 858",
                  |    "lineThree": "Building Two",
                  |    "lineFour": "West Siobhanberg",
                  |    "postalCode": "OX2 3HD",
                  |    "countryCode": "GB"
                  |  },
                  |  "reportingNotification": {
                  |    "type": "WTF",
                  |    "isActiveSeller": true,
                  |    "isDueDiligence": false,
                  |    "year": 2024
                  |  }
                  |}
                  |""".stripMargin)
              .futureValue

            assertAsExpected(
              response = response,
              status = BAD_REQUEST,
              jsonBodyOpt = Some("""
                  |[
                  |  {
                  |    "code": "invalid-reporting-notification-type"
                  |  }
                  |]
                  |""".stripMargin)
            )
            verifyThatDownstreamApiWasNotCalled()
          }
        }
        "'is active seller' is" - {
          "absent" in {
            val response = wsClient
              .url(fullUrl("/platform-operators/345567808"))
              .withHttpHeaders(("Content-Type", "application/json"))
              .post("""
                  |{
                  |  "internalName": "Dyson",
                  |  "businessName": "Dyson Inc.",
                  |  "tradingName": "Dyson",
                  |  "ids": [
                  |    {
                  |      "type": "UTR",
                  |      "value": "1234567890",
                  |      "countryCodeOfIssue": "GB"
                  |    },
                  |    {
                  |      "type": "OTHER",
                  |      "value": "007",
                  |      "countryCodeOfIssue": "GB"
                  |    }
                  |  ],
                  |  "contacts": [
                  |    {
                  |      "name": "Patrick Dyson",
                  |      "phone": "38390756243",
                  |      "emailAddress": "Patrick.Dyson@example.com"
                  |    },
                  |    {
                  |      "name": "Phillipa Dyson",
                  |      "phone": "38390756246",
                  |      "emailAddress": "Phillipa.Dyson@example.com"
                  |    }
                  |  ],
                  |  "address": {
                  |    "lineOne": "26424 Cecelia Junction",
                  |    "lineTwo": "Suite 858",
                  |    "lineThree": "Building Two",
                  |    "lineFour": "West Siobhanberg",
                  |    "postalCode": "OX2 3HD",
                  |    "countryCode": "GB"
                  |  },
                  |  "reportingNotification": {
                  |    "type": "EPO",
                  |    "isDueDiligence": false,
                  |    "year": 2024
                  |  }
                  |}
                  |""".stripMargin)
              .futureValue

            assertAsExpected(
              response = response,
              status = BAD_REQUEST,
              jsonBodyOpt = Some("""
                  |[
                  |  {
                  |    "code": "invalid-reporting-notification-is-active-seller"
                  |  }
                  |]
                  |""".stripMargin)
            )
            verifyThatDownstreamApiWasNotCalled()
          }
        }
        "'is due diligence' is" - {
          "absent" in {
            val response = wsClient
              .url(fullUrl("/platform-operators/345567808"))
              .withHttpHeaders(("Content-Type", "application/json"))
              .post("""
                  |{
                  |  "internalName": "Dyson",
                  |  "businessName": "Dyson Inc.",
                  |  "tradingName": "Dyson",
                  |  "ids": [
                  |    {
                  |      "type": "UTR",
                  |      "value": "1234567890",
                  |      "countryCodeOfIssue": "GB"
                  |    },
                  |    {
                  |      "type": "OTHER",
                  |      "value": "007",
                  |      "countryCodeOfIssue": "GB"
                  |    }
                  |  ],
                  |  "contacts": [
                  |    {
                  |      "name": "Patrick Dyson",
                  |      "phone": "38390756243",
                  |      "emailAddress": "Patrick.Dyson@example.com"
                  |    },
                  |    {
                  |      "name": "Phillipa Dyson",
                  |      "phone": "38390756246",
                  |      "emailAddress": "Phillipa.Dyson@example.com"
                  |    }
                  |  ],
                  |  "address": {
                  |    "lineOne": "26424 Cecelia Junction",
                  |    "lineTwo": "Suite 858",
                  |    "lineThree": "Building Two",
                  |    "lineFour": "West Siobhanberg",
                  |    "postalCode": "OX2 3HD",
                  |    "countryCode": "GB"
                  |  },
                  |  "reportingNotification": {
                  |    "type": "RPO",
                  |    "isActiveSeller": true,
                  |    "year": 2024
                  |  }
                  |}
                  |""".stripMargin)
              .futureValue

            assertAsExpected(
              response = response,
              status = BAD_REQUEST,
              jsonBodyOpt = Some("""
                  |[
                  |  {
                  |    "code": "invalid-reporting-notification-is-due-diligence"
                  |  }
                  |]
                  |""".stripMargin)
            )
            verifyThatDownstreamApiWasNotCalled()
          }
        }
        "the year is" - {
          "absent" in {
            val response = wsClient
              .url(fullUrl("/platform-operators/345567808"))
              .withHttpHeaders(("Content-Type", "application/json"))
              .post("""
                  |{
                  |  "internalName": "Dyson",
                  |  "businessName": "Dyson Inc.",
                  |  "tradingName": "Dyson",
                  |  "ids": [
                  |    {
                  |      "type": "UTR",
                  |      "value": "1234567890",
                  |      "countryCodeOfIssue": "GB"
                  |    },
                  |    {
                  |      "type": "OTHER",
                  |      "value": "007",
                  |      "countryCodeOfIssue": "GB"
                  |    }
                  |  ],
                  |  "contacts": [
                  |    {
                  |      "name": "Patrick Dyson",
                  |      "phone": "38390756243",
                  |      "emailAddress": "Patrick.Dyson@example.com"
                  |    },
                  |    {
                  |      "name": "Phillipa Dyson",
                  |      "phone": "38390756246",
                  |      "emailAddress": "Phillipa.Dyson@example.com"
                  |    }
                  |  ],
                  |  "address": {
                  |    "lineOne": "26424 Cecelia Junction",
                  |    "lineTwo": "Suite 858",
                  |    "lineThree": "Building Two",
                  |    "lineFour": "West Siobhanberg",
                  |    "postalCode": "OX2 3HD",
                  |    "countryCode": "GB"
                  |  },
                  |  "reportingNotification": {
                  |    "type": "RPO",
                  |    "isActiveSeller": true,
                  |    "isDueDiligence": false
                  |  }
                  |}
                  |""".stripMargin)
              .futureValue

            assertAsExpected(
              response = response,
              status = BAD_REQUEST,
              jsonBodyOpt = Some("""
                  |[
                  |  {
                  |    "code": "invalid-reporting-notification-year"
                  |  }
                  |]
                  |""".stripMargin)
            )
            verifyThatDownstreamApiWasNotCalled()
          }
          "invalid (before 2024)" in {
            val response = wsClient
              .url(fullUrl("/platform-operators/345567808"))
              .withHttpHeaders(("Content-Type", "application/json"))
              .post("""
                  |{
                  |  "internalName": "Dyson",
                  |  "businessName": "Dyson Inc.",
                  |  "tradingName": "Dyson",
                  |  "ids": [
                  |    {
                  |      "type": "UTR",
                  |      "value": "1234567890",
                  |      "countryCodeOfIssue": "GB"
                  |    },
                  |    {
                  |      "type": "OTHER",
                  |      "value": "007",
                  |      "countryCodeOfIssue": "GB"
                  |    }
                  |  ],
                  |  "contacts": [
                  |    {
                  |      "name": "Patrick Dyson",
                  |      "phone": "38390756243",
                  |      "emailAddress": "Patrick.Dyson@example.com"
                  |    },
                  |    {
                  |      "name": "Phillipa Dyson",
                  |      "phone": "38390756246",
                  |      "emailAddress": "Phillipa.Dyson@example.com"
                  |    }
                  |  ],
                  |  "address": {
                  |    "lineOne": "26424 Cecelia Junction",
                  |    "lineTwo": "Suite 858",
                  |    "lineThree": "Building Two",
                  |    "lineFour": "West Siobhanberg",
                  |    "postalCode": "OX2 3HD",
                  |    "countryCode": "GB"
                  |  },
                  |  "reportingNotification": {
                  |    "type": "RPO",
                  |    "isActiveSeller": true,
                  |    "isDueDiligence": false,
                  |    "year": 2020
                  |  }
                  |}
                  |""".stripMargin)
              .futureValue

            assertAsExpected(
              response = response,
              status = BAD_REQUEST,
              jsonBodyOpt = Some("""
                  |[
                  |  {
                  |    "code": "invalid-reporting-notification-year"
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

  private def asFullUrl(path: String): String =
    "http://localhost" + ":" + port + "/dprs" + path
}
