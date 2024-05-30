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

import play.api.http.Status.{BAD_REQUEST, CONFLICT, INTERNAL_SERVER_ERROR, SERVICE_UNAVAILABLE}
import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.json.{JsPath, OWrites}
import uk.gov.hmrc.dprs.connectors.BaseConnector.Responses.Error
import uk.gov.hmrc.dprs.services.BaseService
import uk.gov.hmrc.dprs.services.BaseService.ErrorResponse

import scala.Function.unlift

abstract class RegistrationWithIdService extends BaseService {

  override protected def convert(connectorError: Error): ErrorResponse = {
    import BaseService.{ErrorCodes => ServiceErrorCodes}
    import uk.gov.hmrc.dprs.connectors.BaseConnector.Responses.{ErrorCodes => ConnectorErrorCodes}
    connectorError match {
      case Error(INTERNAL_SERVER_ERROR, Some(ConnectorErrorCodes.InternalServerError)) =>
        ErrorResponse(SERVICE_UNAVAILABLE, Some(ServiceErrorCodes.internalServerError))
      case Error(SERVICE_UNAVAILABLE, _) => ErrorResponse(SERVICE_UNAVAILABLE, Some(ServiceErrorCodes.serviceUnavailableError))
      case Error(CONFLICT, _)            => ErrorResponse(CONFLICT, Some(ServiceErrorCodes.conflict))
      case Error(BAD_REQUEST, _)         => ErrorResponse(INTERNAL_SERVER_ERROR)
      case _                             => ErrorResponse(connectorError.status)
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
  }

}
