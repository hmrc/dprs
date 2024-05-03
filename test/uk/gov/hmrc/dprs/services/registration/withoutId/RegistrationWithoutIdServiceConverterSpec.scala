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

package uk.gov.hmrc.dprs.services.registration.withoutId

import uk.gov.hmrc.dprs.connectors.registration.RegistrationWithoutIdConnector
import uk.gov.hmrc.dprs.services.BaseSpec
import uk.gov.hmrc.dprs.services.registration.RegistrationWithoutIdService
import uk.gov.hmrc.dprs.services.registration.RegistrationWithoutIdService.Requests

class RegistrationWithoutIdServiceConverterSpec extends BaseSpec {

  private val converter = new RegistrationWithoutIdService.Converter(fixedClock, acknowledgementReferenceGenerator)

  "when converting from" - {
    "a service request to a connector request, for" - {
      "an individual" in {
        val serviceRequest = Requests.Individual(
          firstName = "Patrick",
          middleName = Some("John"),
          lastName = "Dyson",
          dateOfBirth = "1970-10-04",
          address = Requests.Address(lineOne = "34 Park Lane",
                                     lineTwo = "Building A",
                                     lineThree = "Suite 100",
                                     lineFour = Some("Manchester"),
                                     postalCode = Some("M54 1MQ"),
                                     countryCode = "GB"
          ),
          contactDetails = Requests.ContactDetails(landline = Some("747663966"),
                                                   mobile = Some("38390756243"),
                                                   fax = Some("58371813020"),
                                                   emailAddress = Some("Patrick.Dyson@example.com")
          )
        )

        val connectorRequest = converter.convert(serviceRequest)

        connectorRequest shouldBe RegistrationWithoutIdConnector.Request(
          common =
            RegistrationWithoutIdConnector.Request.Common(receiptDate = currentDateTime, regime = "MDR", acknowledgementReference = acknowledgementReference),
          detail = RegistrationWithoutIdConnector.Request.Detail(
            individual = Some(
              RegistrationWithoutIdConnector.Request.Individual(firstName = "Patrick",
                                                                middleName = Some("John"),
                                                                lastName = "Dyson",
                                                                dateOfBirth = "1970-10-04"
              )
            ),
            organisation = None,
            address = RegistrationWithoutIdConnector.Request.Address(lineOne = "34 Park Lane",
                                                                     lineTwo = "Building A",
                                                                     lineThree = "Suite 100",
                                                                     lineFour = Some("Manchester"),
                                                                     postalCode = Some("M54 1MQ"),
                                                                     countryCode = "GB"
            ),
            contactDetails = RegistrationWithoutIdConnector.Request.ContactDetails(landline = Some("747663966"),
                                                                                   mobile = Some("38390756243"),
                                                                                   fax = Some("58371813020"),
                                                                                   emailAddress = Some("Patrick.Dyson@example.com")
            )
          )
        )
      }
      "an organisation" in {
        val serviceRequest = Requests.Organisation(
          name = "Dyson",
          address = Requests.Address(lineOne = "34 Park Lane",
                                     lineTwo = "Building A",
                                     lineThree = "Suite 100",
                                     lineFour = Some("Manchester"),
                                     postalCode = Some("M54 1MQ"),
                                     countryCode = "GB"
          ),
          contactDetails = Requests.ContactDetails(landline = Some("747663966"),
                                                   mobile = Some("38390756243"),
                                                   fax = Some("58371813020"),
                                                   emailAddress = Some("dyson@example.com")
          )
        )

        val connectorRequest = converter.convert(serviceRequest)

        connectorRequest shouldBe RegistrationWithoutIdConnector.Request(
          common =
            RegistrationWithoutIdConnector.Request.Common(receiptDate = currentDateTime, regime = "MDR", acknowledgementReference = acknowledgementReference),
          detail = RegistrationWithoutIdConnector.Request.Detail(
            individual = None,
            organisation = Some(
              RegistrationWithoutIdConnector.Request.Organisation(name = "Dyson")
            ),
            address = RegistrationWithoutIdConnector.Request.Address(lineOne = "34 Park Lane",
                                                                     lineTwo = "Building A",
                                                                     lineThree = "Suite 100",
                                                                     lineFour = Some("Manchester"),
                                                                     postalCode = Some("M54 1MQ"),
                                                                     countryCode = "GB"
            ),
            contactDetails = RegistrationWithoutIdConnector.Request.ContactDetails(landline = Some("747663966"),
                                                                                   mobile = Some("38390756243"),
                                                                                   fax = Some("58371813020"),
                                                                                   emailAddress = Some("dyson@example.com")
            )
          )
        )
      }
    }
    "a connector response to a service response, for" - {
      "an individual" in {
        val connectorResponse = RegistrationWithoutIdConnector.Responses.Individual(
          common = RegistrationWithoutIdConnector.Responses.Common(returnParams =
            Seq(
              RegistrationWithoutIdConnector.Responses.ReturnParam("SAP_NUMBER", "1960629967")
            )
          ),
          detail = RegistrationWithoutIdConnector.Responses.Individual.Detail(safeId = "XE0000200775706", arn = Some("WARN3849921"))
        )

        val serviceResponse = converter.convert(connectorResponse)

        serviceResponse shouldBe RegistrationWithoutIdService.Responses.Individual(
          ids = Seq(
            RegistrationWithoutIdService.Responses.Id("ARN", "WARN3849921"),
            RegistrationWithoutIdService.Responses.Id("SAFE", "XE0000200775706"),
            RegistrationWithoutIdService.Responses.Id("SAP", "1960629967")
          )
        )
      }
      "an organisation" in {
        val connectorResponse = RegistrationWithoutIdConnector.Responses.Organisation(
          common = RegistrationWithoutIdConnector.Responses.Common(returnParams =
            Seq(
              RegistrationWithoutIdConnector.Responses.ReturnParam("SAP_NUMBER", "1960629967")
            )
          ),
          detail = RegistrationWithoutIdConnector.Responses.Organisation.Detail(safeId = "XE0000200775706", arn = Some("WARN3849921"))
        )

        val serviceResponse = converter.convert(connectorResponse)

        serviceResponse shouldBe RegistrationWithoutIdService.Responses.Organisation(
          ids = Seq(
            RegistrationWithoutIdService.Responses.Id("ARN", "WARN3849921"),
            RegistrationWithoutIdService.Responses.Id("SAFE", "XE0000200775706"),
            RegistrationWithoutIdService.Responses.Id("SAP", "1960629967")
          )
        )
      }
    }
  }
}
