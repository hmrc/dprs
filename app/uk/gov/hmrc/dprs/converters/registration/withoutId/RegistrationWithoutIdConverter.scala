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

import uk.gov.hmrc.dprs.connectors.registration.RegistrationConnector.Request.Common.RequestParameter
import uk.gov.hmrc.dprs.connectors.registration.withoutId.RegistrationWithoutIdConnector.{Request => ConnectorRequest, Response => ConnectorResponse}
import uk.gov.hmrc.dprs.converters.registration.RegistrationConverter
import uk.gov.hmrc.dprs.services.AcknowledgementReferenceGenerator
import uk.gov.hmrc.dprs.services.registration.withoutId.RegistrationWithoutIdService
import uk.gov.hmrc.dprs.services.registration.withoutId.RegistrationWithoutIdService.{Request => CommonServiceRequest}

import java.time.Clock

abstract class RegistrationWithoutIdConverter(clock: Clock, acknowledgementReferenceGenerator: AcknowledgementReferenceGenerator)
    extends RegistrationConverter(clock, acknowledgementReferenceGenerator) {

  def extractIds(connectorResponse: ConnectorResponse): Seq[RegistrationWithoutIdService.Response.Id] =
    Seq(
      Some(RegistrationWithoutIdService.Response.Id(RegistrationWithoutIdService.Response.IdType.SAFE.toString, connectorResponse.detail.safeId)),
      connectorResponse.detail.arn.map(RegistrationWithoutIdService.Response.Id(RegistrationWithoutIdService.Response.IdType.ARN.toString, _)),
      connectorResponse.common.returnParams
        .find(_.name == "SAP_NUMBER")
        .map(returnParam => RegistrationWithoutIdService.Response.Id(RegistrationWithoutIdService.Response.IdType.SAP.toString, returnParam.value))
    ).flatten.sortBy(_.idType)

  def convert(
    address: CommonServiceRequest.Address
  ): ConnectorRequest.Address =
    ConnectorRequest.Address(
      lineOne = address.lineOne,
      lineTwo = address.lineTwo,
      lineThree = address.lineThree,
      lineFour = address.lineFour,
      postalCode = address.postalCode,
      countryCode = address.countryCode
    )

  def convert(
    contactDetails: CommonServiceRequest.ContactDetails
  ): ConnectorRequest.ContactDetails =
    ConnectorRequest.ContactDetails(
      landline = contactDetails.landline,
      mobile = contactDetails.mobile,
      fax = contactDetails.fax,
      emailAddress = contactDetails.emailAddress
    )

}
