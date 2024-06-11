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
import uk.gov.hmrc.dprs.connectors.registration.withoutId.RegistrationWithoutIdConnector.{Request => CommonConnectorRequest, Response => CommonConnectorResponse}
import uk.gov.hmrc.dprs.connectors.registration.withoutId.RegistrationWithoutIdForIndividualConnector.{Request => ConnectorRequest}
import uk.gov.hmrc.dprs.services.BaseSpec
import uk.gov.hmrc.dprs.services.registration.withoutId.RegistrationWithoutIdForIndividualService.{Request => ServiceRequest}
import uk.gov.hmrc.dprs.services.registration.withoutId.RegistrationWithoutIdService.{Request => CommonServiceRequest, Response => CommonServiceResponse}

class RegistrationConverterWithoutIdForIndividualConverterSpec extends BaseSpec {

  private val converter = new RegistrationWithoutIdForIndividualConverter(fixedClock, acknowledgementReferenceGenerator)

  "when converting from" - {
    "a service request to a connector request" in {
      val serviceRequest = ServiceRequest(
        firstName = "Patrick",
        middleName = Some("John"),
        lastName = "Dyson",
        dateOfBirth = "1970-10-04",
        address = CommonServiceRequest.Address(lineOne = "34 Park Lane",
                                               lineTwo = "Building A",
                                               lineThree = "Suite 100",
                                               lineFour = Some("Manchester"),
                                               postalCode = Some("M54 1MQ"),
                                               countryCode = "GB"
        ),
        contactDetails = CommonServiceRequest.ContactDetails(landline = Some("747663966"),
                                                             mobile = Some("38390756243"),
                                                             fax = Some("58371813020"),
                                                             emailAddress = Some("Patrick.Dyson@example.com")
        )
      )

      val connectorRequest = converter.convert(serviceRequest)

      connectorRequest shouldBe ConnectorRequest(
        common = Common(
          receiptDate = currentDateTime,
          regime = "DPRS",
          acknowledgementReference = acknowledgementReference,
          requestParameters = Seq(RegistrationConnector.Request.Common.RequestParameter("REGIME", "DPRS"))
        ),
        detail = ConnectorRequest.Detail(
          firstName = "Patrick",
          middleName = Some("John"),
          lastName = "Dyson",
          dateOfBirth = Some("1970-10-04"),
          address = CommonConnectorRequest.Address(lineOne = "34 Park Lane",
                                                   lineTwo = "Building A",
                                                   lineThree = "Suite 100",
                                                   lineFour = Some("Manchester"),
                                                   postalCode = Some("M54 1MQ"),
                                                   countryCode = "GB"
          ),
          contactDetails = CommonConnectorRequest.ContactDetails(landline = Some("747663966"),
                                                                 mobile = Some("38390756243"),
                                                                 fax = Some("58371813020"),
                                                                 emailAddress = Some("Patrick.Dyson@example.com")
          )
        )
      )
    }
    "a connector response to a service response" in {
      val connectorResponse = CommonConnectorResponse(
        common = RegistrationConnector.Response.Common(returnParams =
          Seq(
            RegistrationConnector.Response.Common.ReturnParam("SAP_NUMBER", "1960629967")
          )
        ),
        detail = CommonConnectorResponse.Detail(safeId = "XE0000200775706", arn = Some("WARN3849921"))
      )

      val serviceResponse = converter.convert(connectorResponse)

      serviceResponse shouldBe CommonServiceResponse(
        ids = Seq(
          CommonServiceResponse.Id("ARN", "WARN3849921"),
          CommonServiceResponse.Id("SAFE", "XE0000200775706"),
          CommonServiceResponse.Id("SAP", "1960629967")
        )
      )
    }
  }
}
