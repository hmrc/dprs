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
import uk.gov.hmrc.dprs.connectors.registration.RegistrationConnector.Request.Common
import uk.gov.hmrc.dprs.connectors.registration.withId.{RegistrationWithIdConnector, RegistrationWithIdForOrganisationConnector}
import uk.gov.hmrc.dprs.services.BaseSpec
import uk.gov.hmrc.dprs.services.registration.withId.RegistrationWithIdForOrganisationService.{Request, Response}
import uk.gov.hmrc.dprs.services.registration.withId.RegistrationWithIdService.{Response => CommonServiceResponse}

class RegistrationConverterWithIdForOrganisationConverterSpec extends BaseSpec {

  private val converter = new RegistrationWithIdForOrganisationConverter(fixedClock, acknowledgementReferenceGenerator)

  "when converting from" - {
    "a service request to a connector request" in {
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
        val _type = Request.Type.all.find(_.toString == rawType).get
        val serviceRequest = Request(
          id = Request.RequestId(idType = Request.RequestIdType.UTR, value = "1234567890"),
          name = "Dyson",
          _type = _type
        )

        val connectorRequest = converter.convert(serviceRequest)

        connectorRequest shouldBe RegistrationWithIdForOrganisationConnector.Request(
          common = Common(
            receiptDate = currentDateTime,
            regime = "DPRS",
            acknowledgementReference = acknowledgementReference,
            requestParameters = Seq(RegistrationConnector.Request.Common.RequestParameter("REGIME", "DPRS"))
          ),
          detail = RegistrationWithIdForOrganisationConnector.Request.Detail(
            idType = "UTR",
            idNumber = "1234567890",
            requiresNameMatch = true,
            isAnAgent = false,
            name = "Dyson",
            _type = expectedCode
          )
        )
      }
    }
    "a connector response to a service response, for" - {
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
        val expectedType = Response.Type.all.find(_.toString == expectedRawType).get
        val connectorResponse = RegistrationWithIdForOrganisationConnector.Response(
          common = RegistrationConnector.Response.Common(returnParams = Seq(RegistrationConnector.Response.Common.ReturnParam("SAP_NUMBER", "8231791429"))),
          detail = RegistrationWithIdForOrganisationConnector.Response.Detail(
            safeId = "XE0000586571722",
            arn = Some("WARN1442450"),
            name = "Dyson",
            typeCode = Some(code),
            address = RegistrationWithIdConnector.Response.Address(lineOne = "2627 Gus Hill",
                                                                   lineTwo = Some("Apt. 898"),
                                                                   lineThree = Some("-"),
                                                                   lineFour = Some("West Corrinamouth"),
                                                                   postalCode = "OX2 3HD",
                                                                   countryCode = "AD"
            ),
            contactDetails = RegistrationWithIdConnector.Response.ContactDetails(landline = Some("176905117"),
                                                                                 mobile = Some("62281724761"),
                                                                                 fax = Some("08959633679"),
                                                                                 emailAddress = Some("edward.goodenough@example.com")
            )
          )
        )

        val serviceResponse = converter.convert(connectorResponse)

        serviceResponse shouldBe Response(
          ids = Seq(
            CommonServiceResponse.Id(idType = "ARN", value = "WARN1442450"),
            CommonServiceResponse.Id(idType = "SAFE", value = "XE0000586571722"),
            CommonServiceResponse.Id(idType = "SAP", value = "8231791429")
          ),
          name = "Dyson",
          _type = expectedType,
          address = CommonServiceResponse.Address(lineOne = "2627 Gus Hill",
                                                  lineTwo = Some("Apt. 898"),
                                                  lineThree = Some("-"),
                                                  lineFour = Some("West Corrinamouth"),
                                                  postalCode = "OX2 3HD",
                                                  countryCode = "AD"
          ),
          contactDetails = CommonServiceResponse.ContactDetails(landline = Some("176905117"),
                                                                mobile = Some("62281724761"),
                                                                fax = Some("08959633679"),
                                                                emailAddress = Some("edward.goodenough@example.com")
          )
        )

      }
    }
  }
}
