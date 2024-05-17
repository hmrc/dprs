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

import uk.gov.hmrc.dprs.connectors.registration.withId.RegistrationWithIdForOrganisationConnector
import uk.gov.hmrc.dprs.services.AcknowledgementReferenceGenerator
import uk.gov.hmrc.dprs.services.registration.withId.RegistrationWithIdForOrganisationService.{Request => ServiceRequest, Response => ServiceResponse}
import uk.gov.hmrc.dprs.services.registration.withId.{RegistrationWithIdForOrganisationService, RegistrationWithIdService}

import java.time.Clock

class RegistrationWithIdForOrganisationConverter(clock: Clock, acknowledgementReferenceGenerator: AcknowledgementReferenceGenerator)
    extends RegistrationWithIdConverter(clock, acknowledgementReferenceGenerator) {

  private val requestOrganisationTypeToCode: Map[ServiceRequest.Type, String] = {
    import ServiceRequest.Type._
    Map(
      NotSpecified                -> "0000",
      Partnership                 -> "0001",
      LimitedLiabilityPartnership -> "0002",
      CorporateBody               -> "0003",
      UnincorporatedBody          -> "0004"
    )
  }

  private val responseOrganisationTypeCode = {
    import ServiceResponse.Type._
    Map(
      "0000" -> NotSpecified,
      "0001" -> Partnership,
      "0002" -> LimitedLiabilityPartnership,
      "0003" -> CorporateBody,
      "0004" -> UnincorporatedBody
    )
  }

  def convert(request: ServiceRequest): RegistrationWithIdForOrganisationConnector.Request =
    RegistrationWithIdForOrganisationConnector.Request(
      common = generateRequestCommon(),
      detail = RegistrationWithIdForOrganisationConnector.Request.Detail(
        idType = request.id.idType.toString,
        idNumber = request.id.value,
        requiresNameMatch = true,
        isAnAgent = false, // TODO: Should we pass this in?
        name = request.name,
        _type = convert(request._type)
      )
    )

  def convert(connectorResponse: RegistrationWithIdForOrganisationConnector.Response): ServiceResponse =
    ServiceResponse(
      ids = extractIds(connectorResponse),
      name = connectorResponse.detail.name,
      _type = connectorResponse.detail.typeCode.map(convert).getOrElse(ServiceResponse.Type.UnknownOrganisationType),
      address = convert(connectorResponse.detail.address),
      contactDetails = convert(connectorResponse.detail.contactDetails)
    )

  def extractIds(connectorResponse: RegistrationWithIdForOrganisationConnector.Response): Seq[RegistrationWithIdService.Response.Id] =
    Seq(
      Some(RegistrationWithIdService.Response.Id(ServiceResponse.IdType.SAFE.toString, connectorResponse.detail.safeId)),
      connectorResponse.detail.arn.map(RegistrationWithIdService.Response.Id(ServiceResponse.IdType.ARN.toString, _)),
      connectorResponse.common.returnParams
        .find(_.name == "SAP_NUMBER")
        .map(returnParam => RegistrationWithIdService.Response.Id(ServiceResponse.IdType.SAP.toString, returnParam.value))
    ).flatten.sortBy(_.idType)

  private def convert(organisationType: RegistrationWithIdForOrganisationService.Request.Type): String =
    requestOrganisationTypeToCode.getOrElse(organisationType, "0000")

  private def convert(rawCode: String): RegistrationWithIdForOrganisationService.Response.Type =
    responseOrganisationTypeCode.getOrElse(rawCode, ServiceResponse.Type.UnknownOrganisationType)

}
