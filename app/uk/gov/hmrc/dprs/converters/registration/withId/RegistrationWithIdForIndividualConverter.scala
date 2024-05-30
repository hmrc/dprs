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

import uk.gov.hmrc.dprs.connectors.registration.withId.RegistrationWithIdForIndividualConnector
import uk.gov.hmrc.dprs.services.AcknowledgementReferenceGenerator
import uk.gov.hmrc.dprs.services.registration.withId.RegistrationWithIdForIndividualService.Response.IdType
import uk.gov.hmrc.dprs.services.registration.withId.RegistrationWithIdForIndividualService.{Request => ServiceRequest, Response => ServiceResponse}
import uk.gov.hmrc.dprs.services.registration.withId.RegistrationWithIdService

import java.time.Clock

class RegistrationWithIdForIndividualConverter(clock: Clock, acknowledgementReferenceGenerator: AcknowledgementReferenceGenerator)
    extends RegistrationWithIdConverter(clock, acknowledgementReferenceGenerator) {

  def convert(request: ServiceRequest): RegistrationWithIdForIndividualConnector.Request =
    RegistrationWithIdForIndividualConnector.Request(
      common = generateRequestCommon(),
      detail = RegistrationWithIdForIndividualConnector.Request.Detail(
        idType = request.id.idType.toString,
        idNumber = request.id.value,
        requiresNameMatch = true,
        isAnAgent = false, // TODO: Will this always be false?
        firstName = request.firstName,
        middleName = request.middleName,
        lastName = request.lastName,
        dateOfBirth = request.dateOfBirth
      )
    )

  def convert(connectorResponse: RegistrationWithIdForIndividualConnector.Response): ServiceResponse =
    ServiceResponse(
      ids = extractIds(connectorResponse),
      firstName = connectorResponse.detail.firstName,
      middleName = connectorResponse.detail.middleName,
      lastName = connectorResponse.detail.lastName,
      dateOfBirth = connectorResponse.detail.dateOfBirth,
      address = convert(connectorResponse.detail.address),
      contactDetails = convert(connectorResponse.detail.contactDetails)
    )

  def extractIds(connectorResponse: RegistrationWithIdForIndividualConnector.Response): Seq[RegistrationWithIdService.Response.Id] =
    Seq(
      Some(RegistrationWithIdService.Response.Id(IdType.SAFE.toString, connectorResponse.detail.safeId)),
      connectorResponse.detail.arn.map(RegistrationWithIdService.Response.Id(IdType.ARN.toString, _)),
      connectorResponse.common.returnParams
        .find(_.name == "SAP_NUMBER")
        .map(returnParam => RegistrationWithIdService.Response.Id(IdType.SAP.toString, returnParam.value))
    ).flatten.sortBy(_.idType)

}
