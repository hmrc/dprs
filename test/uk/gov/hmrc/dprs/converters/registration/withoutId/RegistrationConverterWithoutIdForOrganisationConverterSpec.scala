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

package uk.gov.hmrc.dprs.converters.registration.withoutId

import uk.gov.hmrc.dprs.connectors.registration.RegistrationConnector
import uk.gov.hmrc.dprs.connectors.registration.RegistrationConnector.Request.Common
import uk.gov.hmrc.dprs.connectors.registration.withoutId.{RegistrationWithoutIdConnector, RegistrationWithoutIdForOrganisationConnector}
import uk.gov.hmrc.dprs.services.BaseSpec
import uk.gov.hmrc.dprs.services.registration.withoutId.{RegistrationWithoutIdForOrganisationService, RegistrationWithoutIdService}

class RegistrationConverterWithoutIdForOrganisationConverterSpec extends BaseSpec {

  private val converter = new RegistrationWithoutIdForOrganisationConverter(fixedClock, acknowledgementReferenceGenerator)

  "when converting from" - {
    "a service request to a connector request" in {
      val serviceRequest = RegistrationWithoutIdForOrganisationService.Request(
        name = "Dyson",
        address = RegistrationWithoutIdService.Request.Address(lineOne = "34 Park Lane",
                                                               lineTwo = "Building A",
                                                               lineThree = "Suite 100",
                                                               lineFour = Some("Manchester"),
                                                               postalCode = Some("M54 1MQ"),
                                                               countryCode = "GB"
        ),
        contactDetails = RegistrationWithoutIdService.Request.ContactDetails(landline = Some("747663966"),
                                                                             mobile = Some("38390756243"),
                                                                             fax = Some("58371813020"),
                                                                             emailAddress = Some("dyson@example.com")
        )
      )

      val connectorRequest = converter.convert(serviceRequest)

      connectorRequest shouldBe RegistrationWithoutIdForOrganisationConnector.Request(
        common = Common(
          receiptDate = currentDateTime,
          regime = "DPRS",
          acknowledgementReference = acknowledgementReference,
          requestParameters = Seq(RegistrationConnector.Request.Common.RequestParameter("REGIME", "DPRS"))
        ),
        detail = RegistrationWithoutIdForOrganisationConnector.Request.Detail(
          name = "Dyson",
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
    "a connector response to a service response" in {
      val connectorResponse = RegistrationWithoutIdConnector.Response(
        common = RegistrationConnector.Response.Common(returnParams =
          Seq(
            RegistrationConnector.Response.Common.ReturnParam("SAP_NUMBER", "1960629967")
          )
        ),
        detail = RegistrationWithoutIdConnector.Response.Detail(safeId = "XE0000200775706", arn = Some("WARN3849921"))
      )

      val serviceResponse = converter.convert(connectorResponse)

      serviceResponse shouldBe RegistrationWithoutIdService.Response(
        ids = Seq(
          RegistrationWithoutIdService.Response.Id("ARN", "WARN3849921"),
          RegistrationWithoutIdService.Response.Id("SAFE", "XE0000200775706"),
          RegistrationWithoutIdService.Response.Id("SAP", "1960629967")
        )
      )
    }

  }
}
