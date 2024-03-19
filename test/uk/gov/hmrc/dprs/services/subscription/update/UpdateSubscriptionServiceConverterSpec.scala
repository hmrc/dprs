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

package uk.gov.hmrc.dprs.services.subscription.update

import uk.gov.hmrc.dprs.connectors.UpdateSubscriptionConnector
import uk.gov.hmrc.dprs.services.BaseSpec
import uk.gov.hmrc.dprs.services.UpdateSubscriptionService.Requests.Request
import uk.gov.hmrc.dprs.services.UpdateSubscriptionService.{Converter, Requests}

class UpdateSubscriptionServiceConverterSpec extends BaseSpec {

  private val converter = new Converter(fixedClock, acknowledgementReferenceGenerator)

  "when converting from" - {
    "a service request to a connector request, expecting" - {
      "success" in {
        val serviceRequest = Requests.Request(
          name = Some("Harold Winter"),
          contacts = Seq(
            Request.Individual(
              firstName = "Patrick",
              middleName = Some("John"),
              lastName = "Dyson",
              landline = Some("747663966"),
              mobile = Some("38390756243"),
              emailAddress = "Patrick.Dyson@example.com"
            ),
            Request.Organisation(name = "Dyson", landline = Some("847663966"), mobile = Some("48390756243"), emailAddress = "info@dyson.com")
          )
        )

        val connectorRequest = converter.convert("cfea3248-0df1-4588-b34d-08500f6b46f5", serviceRequest)

        connectorRequest shouldBe Some(
          UpdateSubscriptionConnector.Requests.Request(
            common = UpdateSubscriptionConnector.Requests.Common(
              receiptDate = currentDateTime,
              regime = "MDR",
              acknowledgementReference = acknowledgementReference,
              originatingSystem = "MDTP"
            ),
            detail = UpdateSubscriptionConnector.Requests.Detail(
              idType = "MDR",
              idNumber = "cfea3248-0df1-4588-b34d-08500f6b46f5",
              tradingName = Some("Harold Winter"),
              isGBUser = true,
              primaryContact = UpdateSubscriptionConnector.Requests.Contact(
                landline = Some("747663966"),
                mobile = Some("38390756243"),
                emailAddress = "Patrick.Dyson@example.com",
                individualDetails = Some(
                  UpdateSubscriptionConnector.Requests.Contact.IndividualDetails(firstName = "Patrick", middleName = Some("John"), lastName = "Dyson")
                ),
                organisationDetails = None
              ),
              secondaryContact = Some(
                UpdateSubscriptionConnector.Requests.Contact(
                  landline = Some("847663966"),
                  mobile = Some("48390756243"),
                  emailAddress = "info@dyson.com",
                  individualDetails = None,
                  organisationDetails = Some(UpdateSubscriptionConnector.Requests.Contact.OrganisationDetails(name = "Dyson"))
                )
              )
            )
          )
        )
      }
    }
  }
  "failure, when" - {
    "there are no contacts" in {
      val serviceRequest = Requests.Request(
        name = Some("Harold Winter"),
        contacts = Seq.empty
      )

      val connectorRequest = converter.convert("65bb78ba-baa8-46a2-809a-4747d46b381c", serviceRequest)

      connectorRequest shouldBe empty
    }
  }

}
