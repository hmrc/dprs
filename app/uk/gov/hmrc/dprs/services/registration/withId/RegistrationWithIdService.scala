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

package uk.gov.hmrc.dprs.services.registration.withId

import play.api.http.Status._
import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.json.{JsPath, OWrites}
import uk.gov.hmrc.dprs.connectors.BaseConnector.Responses.Error
import uk.gov.hmrc.dprs.services.BaseService
import uk.gov.hmrc.dprs.services.BaseService.ErrorResponse
import uk.gov.hmrc.dprs.services.registration.withId.RegistrationWithIdService.Response.ConnectorErrorCode

import scala.Function.unlift

abstract class RegistrationWithIdService extends BaseService {

  override protected def convert(connectorError: Error): ErrorResponse = {
    import BaseService.{ErrorCodes => ServiceErrorCodes}
    import ConnectorErrorCode._
    connectorError match {
      case Error(BAD_REQUEST, Some(`badRequest`))                  => ErrorResponse(INTERNAL_SERVER_ERROR)
      case Error(CONFLICT, Some(`duplicateSubmission`))            => ErrorResponse(CONFLICT, Some(ServiceErrorCodes.duplicateSubmission))
      case Error(FORBIDDEN, None)                                  => ErrorResponse(FORBIDDEN, Some(ServiceErrorCodes.forbidden))
      case Error(NOT_FOUND, Some(`noMatch`))                       => ErrorResponse(NOT_FOUND, Some(ServiceErrorCodes.notFound))
      case Error(UNAUTHORIZED, None)                               => ErrorResponse(UNAUTHORIZED, Some(ServiceErrorCodes.unauthorised))
      case Error(SERVICE_UNAVAILABLE, Some(`couldNotBeProcessed`)) => ErrorResponse(SERVICE_UNAVAILABLE, Some(ServiceErrorCodes.couldNotBeProcessed))
      case Error(SERVICE_UNAVAILABLE, Some(`internalError`))       => ErrorResponse(SERVICE_UNAVAILABLE, Some(ServiceErrorCodes.internalError))
      case _                                                       => ErrorResponse(connectorError.status)
    }
  }

}

object RegistrationWithIdService {

  object Response {

    final case class Id(idType: String, value: String)

    object Id {
      implicit val writes: OWrites[Id] =
        ((JsPath \ "type").write[String] and
          (JsPath \ "value").write[String])(unlift(Id.unapply))
    }

    final case class Address(lineOne: String,
                             lineTwo: Option[String],
                             lineThree: Option[String],
                             lineFour: Option[String],
                             postalCode: String,
                             countryCode: String
    )

    object Address {
      implicit val writes: OWrites[Address] =
        ((JsPath \ "lineOne").write[String] and
          (JsPath \ "lineTwo").writeNullable[String] and
          (JsPath \ "lineThree").writeNullable[String] and
          (JsPath \ "lineFour").writeNullable[String] and
          (JsPath \ "postalCode").write[String] and
          (JsPath \ "countryCode").write[String])(unlift(Address.unapply))
    }

    final case class ContactDetails(landline: Option[String], mobile: Option[String], fax: Option[String], emailAddress: Option[String])

    object ContactDetails {
      implicit val writes: OWrites[ContactDetails] =
        ((JsPath \ "landline").writeNullable[String] and
          (JsPath \ "mobile").writeNullable[String] and
          (JsPath \ "fax").writeNullable[String] and
          (JsPath \ "emailAddress").writeNullable[String])(unlift(ContactDetails.unapply))
    }

    object ConnectorErrorCode {
      val badRequest          = "400"
      val duplicateSubmission = "409"
      val noMatch             = "404"
      val couldNotBeProcessed = "503"
      val internalError       = "500"
    }
  }

}
