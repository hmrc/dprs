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

package uk.gov.hmrc.dprs.converters.platformOperator

import uk.gov.hmrc.dprs.connectors.platformOperator.CreatePlatformOperatorConnector.{Request => ConnectorRequest, Response => ConnectorResponse}
import uk.gov.hmrc.dprs.services.platformOperator.CreatePlatformOperatorService.{Request => ServiceRequest, Response => ServiceResponse}

class CreatePlatformOperatorConverter {

  private val originatingSystem  = "MDTP"
  private val transmittingSystem = "EIS"
  private val requestType        = "CREATE"
  private val regime             = "DPI"

  def convert(subscriptionId: String, serviceRequest: ServiceRequest): Option[ConnectorRequest] = {

    def generateMinimalConnectorRequest(firstContact: ServiceRequest.Contact): ConnectorRequest =
      ConnectorRequest(
        originatingSystem = originatingSystem,
        transmittingSystem = transmittingSystem,
        requestType = requestType,
        regime = regime,
        requestParameters = Seq.empty,
        subscriptionId = subscriptionId,
        internalName = serviceRequest.internalName,
        businessName = serviceRequest.businessName,
        tradingName = serviceRequest.tradingName,
        ids = serviceRequest.ids.map(convert),
        reportingNotification = convert(serviceRequest.reportingNotification),
        address = convert(serviceRequest.address),
        primaryContact = convert(firstContact),
        secondaryContact = None
      )

    serviceRequest.contacts match {
      case Seq(primaryContact, secondaryContact) =>
        Some(generateMinimalConnectorRequest(primaryContact).copy(secondaryContact = Some(convert(secondaryContact))))
      case Seq(primaryContact) => Some(generateMinimalConnectorRequest(primaryContact))
      case _                   => None

    }
  }

  def convert(connectorResponse: ConnectorResponse): Option[ServiceResponse] =
    Some(connectorResponse.returnParameter.value).filter(_ => connectorResponse.returnParameter.key == "POID").map(ServiceResponse(_))

  private def convert(serviceRequestId: ServiceRequest.ID): ConnectorRequest.ID =
    ConnectorRequest.ID(_type = serviceRequestId._type.toString, value = serviceRequestId.value, countryCodeOfIssue = serviceRequestId.countryCodeOfIssue)

  private def convert(serviceRequestReportingNotification: ServiceRequest.ReportingNotification): ConnectorRequest.ReportingNotification =
    ConnectorRequest.ReportingNotification(
      _type = serviceRequestReportingNotification._type.toString,
      isActiveSeller = Some(serviceRequestReportingNotification.isActiveSeller),
      isDueDiligence = Some(serviceRequestReportingNotification.isDueDiligence),
      year = serviceRequestReportingNotification.year.toString
    )

  private def convert(serviceRequestAddress: ServiceRequest.Address): ConnectorRequest.Address =
    ConnectorRequest.Address(
      lineOne = serviceRequestAddress.lineOne,
      lineTwo = serviceRequestAddress.lineTwo,
      lineThree = serviceRequestAddress.lineThree,
      lineFour = serviceRequestAddress.lineFour,
      postalCode = serviceRequestAddress.postalCode,
      countryCode = serviceRequestAddress.countryCode
    )

  private def convert(serviceRequestContact: ServiceRequest.Contact): ConnectorRequest.Contact =
    ConnectorRequest.Contact(name = serviceRequestContact.name, phone = serviceRequestContact.phone, emailAddress = serviceRequestContact.emailAddress)
}
