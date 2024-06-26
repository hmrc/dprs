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

import uk.gov.hmrc.dprs.connectors.registration.withId.RegistrationWithIdConnector
import uk.gov.hmrc.dprs.converters.registration.RegistrationConverter
import uk.gov.hmrc.dprs.services.AcknowledgementReferenceGenerator
import uk.gov.hmrc.dprs.services.registration.withId.RegistrationWithIdService

import java.time.Clock

class RegistrationWithIdConverter(clock: Clock, acknowledgementReferenceGenerator: AcknowledgementReferenceGenerator)
    extends RegistrationConverter(clock, acknowledgementReferenceGenerator) {

  def convert(connectorAddress: RegistrationWithIdConnector.Response.Address): RegistrationWithIdService.Response.Address =
    RegistrationWithIdService.Response.Address(
      lineOne = connectorAddress.lineOne,
      lineTwo = connectorAddress.lineTwo,
      lineThree = connectorAddress.lineThree,
      lineFour = connectorAddress.lineFour,
      postalCode = connectorAddress.postalCode,
      countryCode = connectorAddress.countryCode
    )

  def convert(connectorContactDetails: RegistrationWithIdConnector.Response.ContactDetails): RegistrationWithIdService.Response.ContactDetails =
    RegistrationWithIdService.Response.ContactDetails(
      landline = connectorContactDetails.landline,
      mobile = connectorContactDetails.mobile,
      fax = connectorContactDetails.fax,
      emailAddress = connectorContactDetails.emailAddress
    )

}
