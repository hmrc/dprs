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

package uk.gov.hmrc.dprs.controllers.registration.withoutId

import play.api.libs.json.Json.toJson
import play.api.libs.json._
import play.api.mvc.{Action, ControllerComponents}
import uk.gov.hmrc.dprs.controllers.BaseController
import uk.gov.hmrc.dprs.services.BaseService
import uk.gov.hmrc.dprs.services.registration.withoutId.RegistrationWithoutIdForOrganisationService

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class RegistrationWithoutIdForOrganisationController @Inject() (cc: ControllerComponents,
                                                                registrationWithoutIdForOrganisationService: RegistrationWithoutIdForOrganisationService
)(implicit
  executionContext: ExecutionContext
) extends BaseController(cc) {

  def call(): Action[JsValue] = Action(parse.json).async { implicit request =>
    request.body.validate[RegistrationWithoutIdForOrganisationService.Request] match {
      case JsSuccess(requestForOrganisation, _) =>
        registrationWithoutIdForOrganisationService.call(requestForOrganisation, generateRequestHeaders(request)).map {
          case Right(requestForOrganisation) => Ok(toJson(requestForOrganisation))
          case Left(error)                   => handleServiceError(error)
        }
      case JsError(errors: collection.Seq[(JsPath, collection.Seq[JsonValidationError])]) =>
        Future.successful(BadRequest(toJson(convert(errors))))
    }
  }

  private def convert(errors: scala.collection.Seq[(JsPath, collection.Seq[JsonValidationError])]): Seq[BaseService.Error] =
    convert(
      errors,
      Map(
        "/name"                        -> "invalid-name",
        "/address/lineOne"             -> "invalid-address-line-one",
        "/address/lineTwo"             -> "invalid-address-line-two",
        "/address/lineThree"           -> "invalid-address-line-three",
        "/address/lineFour"            -> "invalid-address-line-four",
        "/address/countryCode"         -> "invalid-address-country-code",
        "/address/postalCode"          -> "invalid-address-postal-code",
        "/contactDetails/landline"     -> "invalid-contact-details-landline",
        "/contactDetails/mobile"       -> "invalid-contact-details-mobile",
        "/contactDetails/fax"          -> "invalid-contact-details-fax",
        "/contactDetails/emailAddress" -> "invalid-contact-details-email-address"
      )
    )
}
