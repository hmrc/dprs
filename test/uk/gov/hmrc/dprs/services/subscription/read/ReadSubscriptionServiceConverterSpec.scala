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

package uk.gov.hmrc.dprs.services.subscription.read

import uk.gov.hmrc.dprs.connectors.ReadSubscriptionConnector
import uk.gov.hmrc.dprs.connectors.ReadSubscriptionConnector.Responses.Contact
import uk.gov.hmrc.dprs.connectors.ReadSubscriptionConnector.Responses.Contact.{IndividualDetails, OrganisationDetails}
import uk.gov.hmrc.dprs.services.BaseSpec
import uk.gov.hmrc.dprs.services.ReadSubscriptionService.{Converter, Responses}
import uk.gov.hmrc.dprs.services.ReadSubscriptionService.Responses.{Individual, Organisation}

class ReadSubscriptionServiceConverterSpec extends BaseSpec {

  private val converter = new Converter(fixedClock, acknowledgementReferenceGenerator)

  "when converting from" - {
    "a service request to a connector request, expecting" - {
      "success" in {
        val connectorRequest = converter.convert("cfea3248-0df1-4588-b34d-08500f6b46f5")

        connectorRequest shouldBe ReadSubscriptionConnector.Requests.Request(
          common = ReadSubscriptionConnector.Requests.Common(
            receiptDate = currentDateTime,
            regime = "MDR",
            acknowledgementReference = acknowledgementReference,
            originatingSystem = "MDTP"
          ),
          detail = ReadSubscriptionConnector.Requests.Detail(
            idType = "MDR",
            idNumber = "cfea3248-0df1-4588-b34d-08500f6b46f5"
          )
        )
      }
    }
    "a connector response to a service response, expecting" - {
      "success when" - {
        "primary contact with individualDetails and secondary contact exists" in {
          val connectorResponse = ReadSubscriptionConnector.Responses.Response(
            subscriptionID = "5d10d157-26d6-4355-857b-bc691ee3145b",
            tradingName = Some("Baumbach-Waelchi"),
            isGBUser = true,
            primaryContact = Some(
              Contact(
                email = "christopher.wisoky@example.com",
                phone = Some("687394104"),
                mobile = Some("73744443225"),
                individualDetails = Some(IndividualDetails(firstName = "Josefina", lastName = "Zieme", middleName = None)),
                organisationDetails = None
              )
            ),
            secondaryContact = Some(
              Contact(
                email = "cody.halvorson@example.com",
                phone = None,
                mobile = None,
                individualDetails = None,
                organisationDetails = Some(OrganisationDetails(organisationName = "Daugherty, Mante and Rodriguez"))
              )
            )
          )

          val serviceResponse = converter.convert(connectorResponse)

          serviceResponse shouldBe Responses.Response(
            id = "5d10d157-26d6-4355-857b-bc691ee3145b",
            name = Some("Baumbach-Waelchi"),
            contacts = Seq(
              Individual(
                firstName = Some("Josefina"),
                middleName = None,
                lastName = Some("Zieme"),
                landline = Some("687394104"),
                mobile = Some("73744443225"),
                emailAddress = "christopher.wisoky@example.com"
              ),
              Organisation(
                name = "Daugherty, Mante and Rodriguez",
                landline = None,
                mobile = None,
                emailAddress = "cody.halvorson@example.com"
              )
            )
          )
        }
        "primary contact with organisationDetails and secondary contact exists" in {
          val connectorResponse = ReadSubscriptionConnector.Responses.Response(
            subscriptionID = "5d10d157-26d6-4355-857b-bc691ee3145b",
            tradingName = Some("Baumbach-Waelchi"),
            isGBUser = true,
            primaryContact = Some(
              Contact(
                email = "christopher.wisoky@example.com",
                phone = Some("687394104"),
                mobile = Some("73744443225"),
                individualDetails = None,
                organisationDetails = Some(OrganisationDetails(organisationName = "Daugherty, Mante and Rodriguez"))
              )
            ),
            secondaryContact = Some(
              Contact(
                email = "cody.halvorson@example.com",
                phone = None,
                mobile = None,
                individualDetails = None,
                organisationDetails = Some(OrganisationDetails(organisationName = "Daugherty, Mante and Rodriguez"))
              )
            )
          )

          val serviceResponse = converter.convert(connectorResponse)

          serviceResponse shouldBe Responses.Response(
            id = "5d10d157-26d6-4355-857b-bc691ee3145b",
            name = Some("Baumbach-Waelchi"),
            contacts = Seq(
              Organisation(
                name = "Daugherty, Mante and Rodriguez",
                landline = Some("687394104"),
                mobile = Some("73744443225"),
                emailAddress = "christopher.wisoky@example.com"
              ),
              Organisation(
                name = "Daugherty, Mante and Rodriguez",
                landline = None,
                mobile = None,
                emailAddress = "cody.halvorson@example.com"
              )
            )
          )
        }
        "only primary contact with organisationDetails exists" in {
          val connectorResponse = ReadSubscriptionConnector.Responses.Response(
            subscriptionID = "5d10d157-26d6-4355-857b-bc691ee3145b",
            tradingName = Some("Baumbach-Waelchi"),
            isGBUser = true,
            primaryContact = Some(
              Contact(
                email = "christopher.wisoky@example.com",
                phone = Some("687394104"),
                mobile = Some("73744443225"),
                individualDetails = None,
                organisationDetails = Some(OrganisationDetails(organisationName = "Daugherty, Mante and Rodriguez"))
              )
            ),
            secondaryContact = None
          )

          val serviceResponse = converter.convert(connectorResponse)

          serviceResponse shouldBe Responses.Response(
            id = "5d10d157-26d6-4355-857b-bc691ee3145b",
            name = Some("Baumbach-Waelchi"),
            contacts = Seq(
              Organisation(
                name = "Daugherty, Mante and Rodriguez",
                landline = Some("687394104"),
                mobile = Some("73744443225"),
                emailAddress = "christopher.wisoky@example.com"
              )
            )
          )
        }
        "only secondary contact exists" in {
          val connectorResponse = ReadSubscriptionConnector.Responses.Response(
            subscriptionID = "5d10d157-26d6-4355-857b-bc691ee3145b",
            tradingName = Some("Baumbach-Waelchi"),
            isGBUser = true,
            primaryContact = None,
            secondaryContact = Some(
              Contact(
                email = "cody.halvorson@example.com",
                phone = None,
                mobile = None,
                individualDetails = None,
                organisationDetails = Some(OrganisationDetails(organisationName = "Daugherty, Mante and Rodriguez"))
              )
            )
          )

          val serviceResponse = converter.convert(connectorResponse)

          serviceResponse shouldBe Responses.Response(
            id = "5d10d157-26d6-4355-857b-bc691ee3145b",
            name = Some("Baumbach-Waelchi"),
            contacts = Seq(
              Organisation(
                name = "Daugherty, Mante and Rodriguez",
                landline = None,
                mobile = None,
                emailAddress = "cody.halvorson@example.com"
              )
            )
          )
        }
        "no contact exists" in {
          val connectorResponse = ReadSubscriptionConnector.Responses.Response(
            subscriptionID = "5d10d157-26d6-4355-857b-bc691ee3145b",
            tradingName = Some("Baumbach-Waelchi"),
            isGBUser = true,
            primaryContact = None,
            secondaryContact = None
          )

          val serviceResponse = converter.convert(connectorResponse)

          serviceResponse shouldBe Responses.Response(
            id = "5d10d157-26d6-4355-857b-bc691ee3145b",
            name = Some("Baumbach-Waelchi"),
            contacts = Seq.empty
          )
        }
        "only primary contact without organisationDetails or individualDetails exists" in {
          val connectorResponse = ReadSubscriptionConnector.Responses.Response(
            subscriptionID = "5d10d157-26d6-4355-857b-bc691ee3145b",
            tradingName = Some("Baumbach-Waelchi"),
            isGBUser = true,
            primaryContact = Some(
              Contact(
                email = "christopher.wisoky@example.com",
                phone = Some("687394104"),
                mobile = Some("73744443225"),
                individualDetails = None,
                organisationDetails = None
              )
            ),
            secondaryContact = None
          )

          val serviceResponse = converter.convert(connectorResponse)

          serviceResponse shouldBe Responses.Response(
            id = "5d10d157-26d6-4355-857b-bc691ee3145b",
            name = Some("Baumbach-Waelchi"),
            contacts = Seq.empty
          )
        }
        "only secondary contact without organisationDetails or individualDetails exists" in {
          val connectorResponse = ReadSubscriptionConnector.Responses.Response(
            subscriptionID = "5d10d157-26d6-4355-857b-bc691ee3145b",
            tradingName = Some("Baumbach-Waelchi"),
            isGBUser = true,
            primaryContact = None,
            secondaryContact = Some(
              Contact(
                email = "christopher.wisoky@example.com",
                phone = Some("687394104"),
                mobile = Some("73744443225"),
                individualDetails = None,
                organisationDetails = None
              )
            )
          )

          val serviceResponse = converter.convert(connectorResponse)

          serviceResponse shouldBe Responses.Response(
            id = "5d10d157-26d6-4355-857b-bc691ee3145b",
            name = Some("Baumbach-Waelchi"),
            contacts = Seq.empty
          )
        }
      }
    }
  }

}
