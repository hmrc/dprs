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

package uk.gov.hmrc.dprs.services.registration.withoutId

import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.json.{JsPath, Reads}
import uk.gov.hmrc.dprs.connectors.registration.withoutId.RegistrationWithoutIdForOrganisationConnector
import uk.gov.hmrc.dprs.converters.registration.withoutId.RegistrationWithoutIdForOrganisationConverter
import uk.gov.hmrc.dprs.services.registration.withoutId.RegistrationWithoutIdService.Request.{Address, ContactDetails}
import uk.gov.hmrc.dprs.services.{AcknowledgementReferenceGenerator, BaseService}
import uk.gov.hmrc.dprs.support.ValidationSupport.Reads.lengthBetween

import java.time.Clock
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class RegistrationWithoutIdForOrganisationService @Inject() (clock: Clock,
                                                             acknowledgementReferenceGenerator: AcknowledgementReferenceGenerator,
                                                             registrationWithoutIdForOrganisationConnector: RegistrationWithoutIdForOrganisationConnector
) extends RegistrationWithoutIdService {

  private val converterForOrganisation = new RegistrationWithoutIdForOrganisationConverter(clock, acknowledgementReferenceGenerator)

  def call(
    request: RegistrationWithoutIdForOrganisationService.Request
  )(implicit
    executionContext: ExecutionContext
  ): Future[Either[BaseService.ErrorResponse, RegistrationWithoutIdService.Response]] =
    registrationWithoutIdForOrganisationConnector.call(converterForOrganisation.convert(request)).map {
      case Right(response) => Right(converterForOrganisation.convert(response))
      case Left(error)     => Left(convert(error))
    }

}

object RegistrationWithoutIdForOrganisationService {

  final case class Request(name: String, address: Address, contactDetails: ContactDetails)

  object Request {
    implicit val reads: Reads[Request] =
      ((JsPath \ "name").read(lengthBetween(1, 105)) and
        (JsPath \ "address").read[Address] and
        (JsPath \ "contactDetails").read[ContactDetails])(Request.apply _)
  }

}
