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

package uk.gov.hmrc.dprs.services.registration.withId

import uk.gov.hmrc.dprs.connectors.registration.RegistrationWithIdConnector
import uk.gov.hmrc.dprs.services.BaseSpec
import uk.gov.hmrc.dprs.services.registration.RegistrationWithIdService
import uk.gov.hmrc.dprs.services.registration.RegistrationWithIdService.Requests.Individual
import uk.gov.hmrc.dprs.services.registration.RegistrationWithIdService.{Requests, Responses}

class RegistrationWithIdServiceConverterSpec extends BaseSpec {

  private val converter = new RegistrationWithIdService.Converter(fixedClock, acknowledgementReferenceGenerator)

  "when converting from" - {
    "a service request to a connector request, for" - {
      "an individual" in {
        val serviceRequest = Requests.Individual(
          id = Requests.Individual.RequestId(idType = Individual.RequestIdType.NINO, value = "AA000000A"),
          firstName = "Patrick",
          middleName = Some("John"),
          lastName = "Dyson",
          dateOfBirth = "1970-10-04"
        )

        val connectorRequest = converter.convert(serviceRequest)

        connectorRequest shouldBe RegistrationWithIdConnector.Request(
          common = RegistrationWithIdConnector.Request.Common(
            receiptDate = currentDateTime,
            regime = "MDR",
            acknowledgementReference = acknowledgementReference
          ),
          detail = RegistrationWithIdConnector.Request.Detail(
            idType = "NINO",
            idNumber = "AA000000A",
            requiresNameMatch = true,
            isAnAgent = false,
            individual = Some(
              RegistrationWithIdConnector.Request.Individual(firstName = "Patrick", middleName = Some("John"), lastName = "Dyson", dateOfBirth = "1970-10-04")
            ),
            organisation = None
          )
        )
      }
      "an organisation" in {
        val types =
          Table(
            ("Type (Raw)", "Expected Code"),
            ("NotSpecified", "0000"),
            ("Partnership", "0001"),
            ("LimitedLiabilityPartnership", "0002"),
            ("CorporateBody", "0003"),
            ("UnincorporatedBody", "0004")
          )

        forAll(types) { (rawType, expectedCode) =>
          val _type = Requests.Organisation.Type.all.find(_.toString == rawType).get
          val serviceRequest = Requests.Organisation(
            id = Requests.Organisation.RequestId(idType = Requests.Organisation.RequestIdType.UTR, value = "1234567890"),
            name = "Dyson",
            _type = _type
          )

          val connectorRequest = converter.convert(serviceRequest)

          connectorRequest shouldBe RegistrationWithIdConnector.Request(
            common =
              RegistrationWithIdConnector.Request.Common(receiptDate = currentDateTime, regime = "MDR", acknowledgementReference = acknowledgementReference),
            detail = RegistrationWithIdConnector.Request.Detail(
              idType = "UTR",
              idNumber = "1234567890",
              requiresNameMatch = true,
              isAnAgent = false,
              individual = None,
              organisation = Some(RegistrationWithIdConnector.Request.Organisation(name = "Dyson", _type = expectedCode))
            )
          )
        }
      }
    }
    "a connector response to a service response, for" - {
      "an individual" in {
        val connectorResponse = RegistrationWithIdConnector.Responses.Individual(
          common = RegistrationWithIdConnector.Responses.Common(
            returnParams = Seq(
              RegistrationWithIdConnector.Responses.ReturnParam("SAP_NUMBER", "1960629967")
            )
          ),
          detail = RegistrationWithIdConnector.Responses.Individual.Detail(
            safeId = "XE0000200775706",
            arn = Some("WARN3849921"),
            firstName = "Patrick",
            middleName = Some("John"),
            lastName = "Dyson",
            dateOfBirth = Some("1970-10-04"),
            address = RegistrationWithIdConnector.Responses.Address(
              lineOne = "26424 Cecelia Junction",
              lineTwo = Some("Suite 858"),
              lineThree = Some("-"),
              lineFour = Some("West Siobhanberg"),
              postalCode = "OX2 3HD",
              countryCode = "AD"
            ),
            contactDetails = RegistrationWithIdConnector.Responses.ContactDetails(landline = Some("747663966"),
                                                                                  mobile = Some("38390756243"),
                                                                                  fax = Some("58371813020"),
                                                                                  emailAddress = Some("Patrick.Dyson@example.com")
            )
          )
        )

        val serviceResponse = converter.convert(connectorResponse)

        serviceResponse shouldBe Responses.Individual(
          ids = Seq(
            Responses.Id("ARN", "WARN3849921"),
            Responses.Id("SAFE", "XE0000200775706"),
            Responses.Id("SAP", "1960629967")
          ),
          firstName = "Patrick",
          middleName = Some("John"),
          lastName = "Dyson",
          dateOfBirth = Some("1970-10-04"),
          address = Responses.Address(
            lineOne = "26424 Cecelia Junction",
            lineTwo = Some("Suite 858"),
            lineThree = Some("-"),
            lineFour = Some("West Siobhanberg"),
            postalCode = "OX2 3HD",
            countryCode = "AD"
          ),
          contactDetails = Responses.ContactDetails(landline = Some("747663966"),
                                                    mobile = Some("38390756243"),
                                                    fax = Some("58371813020"),
                                                    emailAddress = Some("Patrick.Dyson@example.com")
          )
        )

      }
      "an organisation" in {
        val types =
          Table(
            ("Code", "Expected Type (Raw)"),
            ("0000", "NotSpecified"),
            ("0001", "Partnership"),
            ("0002", "LimitedLiabilityPartnership"),
            ("0003", "CorporateBody"),
            ("0004", "UnincorporatedBody"),
            ("0006", "UnknownOrganisationType"),
            ("1111", "UnknownOrganisationType")
          )

        forAll(types) { (code, expectedRawType) =>
          val expectedType = Responses.Organisation.Type.all.find(_.toString == expectedRawType).get
          val connectorResponse = RegistrationWithIdConnector.Responses.Organisation(
            common =
              RegistrationWithIdConnector.Responses.Common(returnParams = Seq(RegistrationWithIdConnector.Responses.ReturnParam("SAP_NUMBER", "8231791429"))),
            detail = RegistrationWithIdConnector.Responses.Organisation.Detail(
              safeId = "XE0000586571722",
              arn = Some("WARN1442450"),
              name = "Dyson",
              typeCode = Some(code),
              address = RegistrationWithIdConnector.Responses.Address(lineOne = "2627 Gus Hill",
                                                                      lineTwo = Some("Apt. 898"),
                                                                      lineThree = Some("-"),
                                                                      lineFour = Some("West Corrinamouth"),
                                                                      postalCode = "OX2 3HD",
                                                                      countryCode = "AD"
              ),
              contactDetails = RegistrationWithIdConnector.Responses.ContactDetails(landline = Some("176905117"),
                                                                                    mobile = Some("62281724761"),
                                                                                    fax = Some("08959633679"),
                                                                                    emailAddress = Some("edward.goodenough@example.com")
              )
            )
          )

          val serviceResponse = converter.convert(connectorResponse)

          serviceResponse shouldBe Responses.Organisation(
            ids = Seq(
              Responses.Id(idType = "ARN", value = "WARN1442450"),
              Responses.Id(idType = "SAFE", value = "XE0000586571722"),
              Responses.Id(idType = "SAP", value = "8231791429")
            ),
            name = "Dyson",
            _type = expectedType,
            address = Responses.Address(lineOne = "2627 Gus Hill",
                                        lineTwo = Some("Apt. 898"),
                                        lineThree = Some("-"),
                                        lineFour = Some("West Corrinamouth"),
                                        postalCode = "OX2 3HD",
                                        countryCode = "AD"
            ),
            contactDetails = Responses.ContactDetails(landline = Some("176905117"),
                                                      mobile = Some("62281724761"),
                                                      fax = Some("08959633679"),
                                                      emailAddress = Some("edward.goodenough@example.com")
            )
          )

        }
      }
    }
  }

}
