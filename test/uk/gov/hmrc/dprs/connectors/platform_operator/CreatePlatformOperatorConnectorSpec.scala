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

package uk.gov.hmrc.dprs.connectors.platform_operator

import play.api.libs.json.Json.toJson
import uk.gov.hmrc.dprs.connectors.platform_operator.CreatePlatformOperatorConnector.{Request, Response}
import uk.gov.hmrc.dprs.services.BaseSpec
import uk.gov.hmrc.dprs.services.BaseSpec.{beSameAs, beValid}

class CreatePlatformOperatorConnectorSpec extends BaseSpec {

  "when" - {
    "writing the request object, we should generate the expected JSON request, when there" - {
      "is only a primary contract" in {
        val request = Request(
          originatingSystem = "MDTP",
          transmittingSystem = "EIS",
          requestType = "CREATE",
          regime = "DPI",
          requestParameters = Seq(
            Request.RequestParameter(name = "(name)", value = "(value)")
          ),
          subscriptionId = "345567808",
          internalName = "Amazon UK",
          businessName = Some("Amazon UK Ltd"),
          tradingName = Some("Amazon"),
          ids = Seq(
            Request.ID(_type = "UTR", value = "68936493", countryCodeOfIssue = "GB")
          ),
          reportingNotification = Request.ReportingNotification(_type = "RPO", isActiveSeller = Some(true), isDueDiligence = Some(false), year = "2024"),
          address = Request.Address(lineOne = "22",
                                    lineTwo = "High Street",
                                    lineThree = "Dawley",
                                    lineFour = Some("Telford"),
                                    postalCode = Some("TF22 2RE"),
                                    countryCode = "GB"
          ),
          primaryContact = Request.Contact(name = "John Smith", phone = Some("0789876568"), emailAddress = "jsmith@example.com"),
          secondaryContact = None
        )

        val json = toJson(request)

        json should beSameAs(
          """
            |{
            |  "POManagement": {
            |    "RequestCommon": {
            |      "OriginatingSystem": "MDTP",
            |      "TransmittingSystem": "EIS",
            |      "RequestType": "CREATE",
            |      "Regime": "DPI",
            |      "RequestParameters": [
            |        {
            |          "ParamName": "(name)",
            |          "ParamValue": "(value)"
            |        }
            |      ]
            |    },
            |    "RequestDetails": {
            |      "POName": "Amazon UK",
            |      "BusinessName": "Amazon UK Ltd",
            |      "SubscriptionID": "345567808",
            |      "TradingName": "Amazon",
            |      "TINDetails": [
            |        {
            |          "TINType": "UTR",
            |          "TIN": "68936493",
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
            |        "AddressLine1": "22",
            |        "AddressLine2": "High Street",
            |        "AddressLine3": "Dawley",
            |        "AddressLine4": "Telford",
            |        "CountryCode": "GB",
            |        "PostalCode": "TF22 2RE"
            |      },
            |      "PrimaryContactDetails": {
            |        "ContactName": "John Smith",
            |        "EmailAddress": "jsmith@example.com",
            |        "PhoneNumber": "0789876568"
            |      }
            |    }
            |  }
            |}
            |""".stripMargin
        )

      }
      "there is both a primary and secondary contact" in {
        val request = Request(
          originatingSystem = "MDTP",
          transmittingSystem = "EIS",
          requestType = "CREATE",
          regime = "DPI",
          requestParameters = Seq(
            Request.RequestParameter(name = "(name)", value = "(value)")
          ),
          subscriptionId = "345567808",
          internalName = "Amazon UK",
          businessName = Some("Amazon UK Ltd"),
          tradingName = Some("Amazon"),
          ids = Seq(
            Request.ID(_type = "UTR", value = "68936493", countryCodeOfIssue = "GB")
          ),
          reportingNotification = Request.ReportingNotification(_type = "RPO", isActiveSeller = Some(true), isDueDiligence = Some(false), year = "2024"),
          address = Request.Address(lineOne = "22",
                                    lineTwo = "High Street",
                                    lineThree = "Dawley",
                                    lineFour = Some("Telford"),
                                    postalCode = Some("TF22 2RE"),
                                    countryCode = "GB"
          ),
          primaryContact = Request.Contact(name = "John Smith", phone = Some("0789876568"), emailAddress = "jsmith@example.com"),
          secondaryContact = Some(Request.Contact(name = "Paul Smith", phone = Some("08898765680"), emailAddress = "psmith@example.com"))
        )

        val json = toJson(request)

        json should beSameAs(
          """
            |{
            |  "POManagement": {
            |    "RequestCommon": {
            |      "OriginatingSystem": "MDTP",
            |      "TransmittingSystem": "EIS",
            |      "RequestType": "CREATE",
            |      "Regime": "DPI",
            |      "RequestParameters": [
            |        {
            |          "ParamName": "(name)",
            |          "ParamValue": "(value)"
            |        }
            |      ]
            |    },
            |    "RequestDetails": {
            |      "POName": "Amazon UK",
            |      "BusinessName": "Amazon UK Ltd",
            |      "SubscriptionID": "345567808",
            |      "TradingName": "Amazon",
            |      "TINDetails": [
            |        {
            |          "TINType": "UTR",
            |          "TIN": "68936493",
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
            |        "AddressLine1": "22",
            |        "AddressLine2": "High Street",
            |        "AddressLine3": "Dawley",
            |        "AddressLine4": "Telford",
            |        "CountryCode": "GB",
            |        "PostalCode": "TF22 2RE"
            |      },
            |      "PrimaryContactDetails": {
            |        "ContactName": "John Smith",
            |        "EmailAddress": "jsmith@example.com",
            |        "PhoneNumber": "0789876568"
            |      },
            |      "SecondaryContactDetails": {
            |        "ContactName": "Paul Smith",
            |        "EmailAddress": "psmith@example.com",
            |        "PhoneNumber": "08898765680"
            |      }
            |    }
            |  }
            |}
            |""".stripMargin
        )

      }
    }
    "reading the JSON response, we should generate the expected response object" in {
      val rawJson =
        """
          |{
          |  "success": {
          |    "processingDate": "2023-12-13T11:50:35Z",
          |    "ReturnParameters": {
          |      "Key": "POID",
          |      "Value": "PO12345"
          |    }
          |  }
          |}
          |""".stripMargin

      rawJson should beValid(
        Response(returnParameter = Response.ReturnParam(key = "POID", value = "PO12345"))
      )
    }
  }
}
