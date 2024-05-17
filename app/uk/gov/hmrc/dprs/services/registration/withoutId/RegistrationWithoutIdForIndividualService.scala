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

import play.api.libs.functional.syntax.{toApplicativeOps, toFunctionalBuilderOps}
import play.api.libs.json.Reads.{minLength, verifying}
import play.api.libs.json._
import uk.gov.hmrc.dprs.connectors.registration.withoutId.RegistrationWithoutIdForIndividualConnector
import uk.gov.hmrc.dprs.converters.registration.withoutId.RegistrationWithoutIdForIndividualConverter
import uk.gov.hmrc.dprs.services.{AcknowledgementReferenceGenerator, BaseService}
import uk.gov.hmrc.dprs.support.ValidationSupport
import uk.gov.hmrc.dprs.support.ValidationSupport.Reads.lengthBetween

import java.time.Clock
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class RegistrationWithoutIdForIndividualService @Inject() (clock: Clock,
                                                           acknowledgementReferenceGenerator: AcknowledgementReferenceGenerator,
                                                           registrationWithoutIdForIndividualConnector: RegistrationWithoutIdForIndividualConnector
) extends RegistrationWithoutIdService {

  private val converter = new RegistrationWithoutIdForIndividualConverter(clock, acknowledgementReferenceGenerator)

  def call(
    request: RegistrationWithoutIdForIndividualService.Request
  )(implicit
    executionContext: ExecutionContext
  ): Future[Either[BaseService.ErrorResponse, RegistrationWithoutIdService.Response]] =
    registrationWithoutIdForIndividualConnector.call(converter.convert(request)).map {
      case Right(response) => Right(converter.convert(response))
      case Left(error)     => Left(convert(error))
    }

}

object RegistrationWithoutIdForIndividualService {

  final case class Request(firstName: String,
                           middleName: Option[String],
                           lastName: String,
                           dateOfBirth: String,
                           address: RegistrationWithoutIdService.Request.Address,
                           contactDetails: RegistrationWithoutIdService.Request.ContactDetails
  )

  object Request {

    implicit val reads: Reads[Request] =
      ((JsPath \ "firstName").read(lengthBetween(1, 35)) and
        (JsPath \ "middleName").readNullable(lengthBetween(1, 35)) and
        (JsPath \ "lastName").read(lengthBetween(1, 35)) and
        (JsPath \ "dateOfBirth").read(minLength[String](1).keepAnd(verifying[String](ValidationSupport.isValidDate))) and
        (JsPath \ "address").read[RegistrationWithoutIdService.Request.Address] and
        (JsPath \ "contactDetails").read[RegistrationWithoutIdService.Request.ContactDetails])(Request.apply _)
  }

}
