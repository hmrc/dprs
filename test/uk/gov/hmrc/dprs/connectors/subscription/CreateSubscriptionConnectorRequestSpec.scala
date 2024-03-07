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

package uk.gov.hmrc.dprs.connectors.subscription

import play.api.libs.json.Json.toJson
import uk.gov.hmrc.dprs.connectors.CreateSubscriptionConnector
import uk.gov.hmrc.dprs.services.BaseSpec
import uk.gov.hmrc.dprs.services.BaseSpec.beSameAs

class CreateSubscriptionConnectorRequestSpec extends BaseSpec {

  "writing to JSON should give the expected object where there" - {
    "is only a primary contact, which is an" - {
      "individual" in {
        val request = CreateSubscriptionConnector.Requests.Request(
          common = CreateSubscriptionConnector.Requests.Common(receiptDate = "2024-02-15T11:32:43.364Z",
                                                               regime = "MDR",
                                                               acknowledgementReference = "0badb375-30dd-41c8-821b-e7d92ebd2ce4",
                                                               originatingSystem = "MDTP"
          ),
          detail = CreateSubscriptionConnector.Requests.Detail(
            idType = "NINO",
            idNumber = "AA000000A",
            tradingName = Some("Harold Winter"),
            isGBUser = true,
            primaryContact = CreateSubscriptionConnector.Requests.Contact(
              landline = Some("747663966"),
              mobile = Some("38390756243"),
              emailAddress = "Patrick.Dyson@example.com",
              individualDetails = Some(
                CreateSubscriptionConnector.Requests.Contact.IndividualDetails(firstName = "Patrick", middleName = Some("John"), lastName = "Dyson")
              ),
              organisationDetails = None
            ),
            secondaryContact = None
          )
        )

        val json = toJson(request)

        json should beSameAs("""
            |          {
            |    "createSubscriptionForMDRRequest": {
            |        "requestCommon": {
            |            "regime": "MDR",
            |            "receiptDate": "2024-02-15T11:32:43.364Z",
            |            "acknowledgementReference": "0badb375-30dd-41c8-821b-e7d92ebd2ce4",
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
            |""".stripMargin)

      }
      "organisation" in {
        val request = CreateSubscriptionConnector.Requests.Request(
          common = CreateSubscriptionConnector.Requests.Common(receiptDate = "2024-02-15T11:32:43.364Z",
                                                               regime = "MDR",
                                                               acknowledgementReference = "0badb375-30dd-41c8-821b-e7d92ebd2ce4",
                                                               originatingSystem = "MDTP"
          ),
          detail = CreateSubscriptionConnector.Requests.Detail(
            idType = "NINO",
            idNumber = "AA000000A",
            tradingName = Some("Harold Winter"),
            isGBUser = true,
            primaryContact = CreateSubscriptionConnector.Requests.Contact(
              landline = Some("847663966"),
              mobile = Some("48390756243"),
              emailAddress = "info@example.com",
              individualDetails = None,
              organisationDetails = Some(
                CreateSubscriptionConnector.Requests.Contact.OrganisationDetails(name = "Dyson")
              )
            ),
            secondaryContact = None
          )
        )

        val json = toJson(request)

        json should beSameAs("""
                               |          {
                               |    "createSubscriptionForMDRRequest": {
                               |        "requestCommon": {
                               |            "regime": "MDR",
                               |            "receiptDate": "2024-02-15T11:32:43.364Z",
                               |            "acknowledgementReference": "0badb375-30dd-41c8-821b-e7d92ebd2ce4",
                               |            "originatingSystem": "MDTP"
                               |        },
                               |        "requestDetail": {
                               |            "IDType": "NINO",
                               |            "IDNumber": "AA000000A",
                               |            "tradingName": "Harold Winter",
                               |            "isGBUser": true,
                               |            "primaryContact": {
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
                               |""".stripMargin)

      }
    }
    "is a secondary contact, which is an" - {
      "individual" in {
        val request = CreateSubscriptionConnector.Requests.Request(
          common = CreateSubscriptionConnector.Requests.Common(receiptDate = "2024-02-15T11:32:43.364Z",
                                                               regime = "MDR",
                                                               acknowledgementReference = "0badb375-30dd-41c8-821b-e7d92ebd2ce4",
                                                               originatingSystem = "MDTP"
          ),
          detail = CreateSubscriptionConnector.Requests.Detail(
            idType = "NINO",
            idNumber = "AA000000A",
            tradingName = Some("Harold Winter"),
            isGBUser = true,
            primaryContact = CreateSubscriptionConnector.Requests.Contact(
              landline = Some("747663966"),
              mobile = Some("38390756243"),
              emailAddress = "Patrick.Dyson@example.com",
              individualDetails = Some(
                CreateSubscriptionConnector.Requests.Contact.IndividualDetails(firstName = "Patrick", middleName = Some("John"), lastName = "Dyson")
              ),
              organisationDetails = None
            ),
            secondaryContact = Some(
              CreateSubscriptionConnector.Requests.Contact(
                landline = Some("647663968"),
                mobile = Some("28390756245"),
                emailAddress = "Patricia.Dyson@example.com",
                individualDetails = Some(
                  CreateSubscriptionConnector.Requests.Contact.IndividualDetails(firstName = "Patricia", middleName = Some("Jane"), lastName = "Dyson")
                ),
                organisationDetails = None
              )
            )
          )
        )

        val json = toJson(request)

        json should beSameAs("""
                               |          {
                               |    "createSubscriptionForMDRRequest": {
                               |        "requestCommon": {
                               |            "regime": "MDR",
                               |            "receiptDate": "2024-02-15T11:32:43.364Z",
                               |            "acknowledgementReference": "0badb375-30dd-41c8-821b-e7d92ebd2ce4",
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
                               |             "secondaryContact": {
                               |                "individual": {
                               |                    "firstName": "Patricia",
                               |                    "middleName": "Jane",
                               |                    "lastName": "Dyson"
                               |                },
                               |                "email": "Patricia.Dyson@example.com",
                               |                "phone": "647663968",
                               |                "mobile": "28390756245"
                               |            }
                               |        }
                               |    }
                               |}
                               |""".stripMargin)
      }
      "organisation" in {
        val request = CreateSubscriptionConnector.Requests.Request(
          common = CreateSubscriptionConnector.Requests.Common(receiptDate = "2024-02-15T11:32:43.364Z",
                                                               regime = "MDR",
                                                               acknowledgementReference = "0badb375-30dd-41c8-821b-e7d92ebd2ce4",
                                                               originatingSystem = "MDTP"
          ),
          detail = CreateSubscriptionConnector.Requests.Detail(
            idType = "NINO",
            idNumber = "AA000000A",
            tradingName = Some("Harold Winter"),
            isGBUser = true,
            primaryContact = CreateSubscriptionConnector.Requests.Contact(
              landline = Some("747663966"),
              mobile = Some("38390756243"),
              emailAddress = "Patrick.Dyson@example.com",
              individualDetails = Some(
                CreateSubscriptionConnector.Requests.Contact.IndividualDetails(firstName = "Patrick", middleName = Some("John"), lastName = "Dyson")
              ),
              organisationDetails = None
            ),
            secondaryContact = Some(
              CreateSubscriptionConnector.Requests.Contact(
                landline = Some("147663966"),
                mobile = Some("28390756243"),
                emailAddress = "info@example.com",
                individualDetails = None,
                organisationDetails = Some(
                  CreateSubscriptionConnector.Requests.Contact.OrganisationDetails(name = "Dyson")
                )
              )
            )
          )
        )

        val json = toJson(request)

        json should beSameAs("""
                               | {
                               |    "createSubscriptionForMDRRequest": {
                               |        "requestCommon": {
                               |            "regime": "MDR",
                               |            "receiptDate": "2024-02-15T11:32:43.364Z",
                               |            "acknowledgementReference": "0badb375-30dd-41c8-821b-e7d92ebd2ce4",
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
                               |                "phone": "147663966",
                               |                "mobile": "28390756243"
                               |            }
                               |        }
                               |    }
                               |}
                               |""".stripMargin)
      }
    }
  }
}
