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

package uk.gov.hmrc.dprs.converters.registration.withId

import uk.gov.hmrc.dprs.connectors.registration.RegistrationConnector
import uk.gov.hmrc.dprs.connectors.registration.withId.RegistrationWithIdConnector.{Response => CommonConnectorResponse}
import uk.gov.hmrc.dprs.connectors.registration.withId.RegistrationWithIdForIndividualConnector.{Request => ConnectorRequest, Response => ConnectorResponse}
import uk.gov.hmrc.dprs.services.BaseSpec
import uk.gov.hmrc.dprs.services.registration.withId.RegistrationWithIdForIndividualService.{Request => ServiceRequest, Response => ServiceResponse}
import uk.gov.hmrc.dprs.services.registration.withId.RegistrationWithIdService.{Response => CommonServiceResponse}

class RegistrationWithIdForIndividualConverterSpec extends BaseSpec {

  private val converter = new RegistrationWithIdForIndividualConverter(fixedClock, acknowledgementReferenceGenerator)

  "when converting from" - {
    "a service request to a connector request" in {
      val serviceRequest = ServiceRequest(
        id = ServiceRequest.RequestId(idType = ServiceRequest.RequestIdType.NINO, value = "AA000000A"),
        firstName = "Patrick",
        middleName = Some("John"),
        lastName = "Dyson",
        dateOfBirth = "1970-10-04"
      )

      val connectorRequest = converter.convert(serviceRequest)

      connectorRequest shouldBe ConnectorRequest(
        common = RegistrationConnector.Request.Common(
          receiptDate = currentDateTime,
          regime = "DPRS",
          acknowledgementReference = acknowledgementReference,
          requestParameters = Seq(RegistrationConnector.Request.Common.RequestParameter("REGIME", "DPRS"))
        ),
        detail = ConnectorRequest.Detail(
          idType = "NINO",
          idNumber = "AA000000A",
          requiresNameMatch = true,
          isAnAgent = false,
          firstName = "Patrick",
          middleName = Some("John"),
          lastName = "Dyson",
          dateOfBirth = "1970-10-04"
        )
      )

    }
    "a connector response to a service response, for" - {
      val connectorResponse = ConnectorResponse(
        common = RegistrationConnector.Response.Common(
          returnParams = Seq(
            RegistrationConnector.Response.Common.ReturnParam("SAP_NUMBER", "1960629967")
          )
        ),
        detail = ConnectorResponse.Detail(
          safeId = "XE0000200775706",
          arn = Some("WARN3849921"),
          firstName = "Patrick",
          middleName = Some("John"),
          lastName = "Dyson",
          dateOfBirth = Some("1970-10-04"),
          address = CommonConnectorResponse.Address(
            lineOne = "26424 Cecelia Junction",
            lineTwo = Some("Suite 858"),
            lineThree = Some("-"),
            lineFour = Some("West Siobhanberg"),
            postalCode = "OX2 3HD",
            countryCode = "AD"
          ),
          contactDetails = CommonConnectorResponse.ContactDetails(landline = Some("747663966"),
                                                                  mobile = Some("38390756243"),
                                                                  fax = Some("58371813020"),
                                                                  emailAddress = Some("Patrick.Dyson@example.com")
          )
        )
      )

      val serviceResponse = converter.convert(connectorResponse)

      serviceResponse shouldBe ServiceResponse(
        ids = Seq(
          CommonServiceResponse.Id("ARN", "WARN3849921"),
          CommonServiceResponse.Id("SAFE", "XE0000200775706"),
          CommonServiceResponse.Id("SAP", "1960629967")
        ),
        firstName = "Patrick",
        middleName = Some("John"),
        lastName = "Dyson",
        dateOfBirth = Some("1970-10-04"),
        address = CommonServiceResponse.Address(
          lineOne = "26424 Cecelia Junction",
          lineTwo = Some("Suite 858"),
          lineThree = Some("-"),
          lineFour = Some("West Siobhanberg"),
          postalCode = "OX2 3HD",
          countryCode = "AD"
        ),
        contactDetails = CommonServiceResponse.ContactDetails(landline = Some("747663966"),
                                                              mobile = Some("38390756243"),
                                                              fax = Some("58371813020"),
                                                              emailAddress = Some("Patrick.Dyson@example.com")
        )
      )

    }

  }
}
