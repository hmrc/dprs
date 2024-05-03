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
import uk.gov.hmrc.dprs.services.BaseSpec
import uk.gov.hmrc.dprs.services.BaseSpec.beSameAs

class CreateSubscriptionConnectorRequestSpec extends BaseSpec {

  "writing to JSON should give the expected object where there" - {
    "is only a primary contact, which is an" - {
      "individual" in {
        val request = CreateSubscriptionConnector.Requests.Request(
          idType = "NINO",
          idNumber = "AA000000A",
          tradingName = Some("Harold Winter"),
          gbUser = true,
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

        val json = toJson(request)

        json should beSameAs("""
            |{
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
            |                "mobile" : "747663966",
            |                "phone" : "38390756243"
            |            }
            |        }
            |""".stripMargin)

      }
      "organisation" in {
        val request = CreateSubscriptionConnector.Requests.Request(
          idType = "NINO",
          idNumber = "AA000000A",
          tradingName = Some("Harold Winter"),
          gbUser = true,
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

        val json = toJson(request)

        json should beSameAs("""
                               |{
                               |            "idType": "NINO",
                               |            "idNumber": "AA000000A",
                               |            "tradingName": "Harold Winter",
                               |            "gbUser": true,
                               |            "primaryContact": {
                               |                "organisation": {
                               |                    "name": "Dyson"
                               |                },
                               |                "email": "info@example.com",
                               |                "mobile": "847663966",
                               |                "phone": "48390756243"
                               |            }
                               |        }
                               |""".stripMargin)

      }
    }
    "is a secondary contact, which is an" - {
      "individual" in {
        val request = CreateSubscriptionConnector.Requests.Request(
          idType = "NINO",
          idNumber = "AA000000A",
          tradingName = Some("Harold Winter"),
          gbUser = true,
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

        val json = toJson(request)

        json should beSameAs("""
                               |{
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
                               |                "individual": {
                               |                    "firstName": "Patricia",
                               |                    "middleName": "Jane",
                               |                    "lastName": "Dyson"
                               |                },
                               |                "email": "Patricia.Dyson@example.com",
                               |                "mobile": "647663968",
                               |                "phone": "28390756245"
                               |            }
                               |        }
                               |""".stripMargin)
      }
      "organisation" in {
        val request = CreateSubscriptionConnector.Requests.Request(
          idType = "NINO",
          idNumber = "AA000000A",
          tradingName = Some("Harold Winter"),
          gbUser = true,
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

        val json = toJson(request)

        json should beSameAs("""
                               |{
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
                               |            "secondaryContact": {
                               |                "organisation": {
                               |                    "name": "Dyson"
                               |                },
                               |                "email": "info@example.com",
                               |                "mobile": "147663966",
                               |                "phone": "28390756243"
                               |            }
                               |        }
                               |""".stripMargin)
      }
    }
  }
}
