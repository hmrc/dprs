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

import play.api.http.Status._
import play.api.libs.functional.syntax.{toApplicativeOps, toFunctionalBuilderOps}
import play.api.libs.json.Reads.verifying
import play.api.libs.json._
import uk.gov.hmrc.dprs.connectors.BaseConnector.Responses.Error
import uk.gov.hmrc.dprs.services.BaseService
import uk.gov.hmrc.dprs.services.BaseService.ErrorResponse
import uk.gov.hmrc.dprs.services.registration.withoutId.RegistrationWithoutIdService.Response.ConnectorErrorCode
import uk.gov.hmrc.dprs.support.ValidationSupport.Reads.{lengthBetween, validEmailAddress, validPhoneNumber}
import uk.gov.hmrc.dprs.support.ValidationSupport.{isPostalCodeRequired, isValidCountryCode}

import scala.Function.unlift

abstract class RegistrationWithoutIdService extends BaseService {

  override protected def convert(connectorError: Error): ErrorResponse = {
    import BaseService.{ErrorCodes => ServiceErrorCodes}
    import ConnectorErrorCode._
    connectorError match {
      case Error(INTERNAL_SERVER_ERROR, Some(`internalServerError`)) => ErrorResponse(SERVICE_UNAVAILABLE, Some(ServiceErrorCodes.internalServerError))
      case Error(SERVICE_UNAVAILABLE, Some(`serviceUnavailable`))    => ErrorResponse(SERVICE_UNAVAILABLE, Some(ServiceErrorCodes.serviceUnavailableError))
      case Error(CONFLICT, _)                                        => ErrorResponse(CONFLICT, Some(ServiceErrorCodes.conflict))
      case Error(FORBIDDEN, _)                                       => ErrorResponse(FORBIDDEN, Some(ServiceErrorCodes.forbidden))
      case Error(BAD_REQUEST, _)                                     => ErrorResponse(INTERNAL_SERVER_ERROR)
      case _                                                         => ErrorResponse(connectorError.status)
    }
  }
}

object RegistrationWithoutIdService {

  object Request {

    final case class Address(lineOne: String, lineTwo: String, lineThree: String, lineFour: Option[String], postalCode: Option[String], countryCode: String)

    object Address {

      private def postalCodePresentIfExpected(address: Request.Address): Boolean =
        if (isPostalCodeRequired(address.countryCode)) address.postalCode.isDefined else true

      implicit val reads: Reads[Address] =
        ((JsPath \ "lineOne").read(lengthBetween(1, 35)) and
          (JsPath \ "lineTwo").read(lengthBetween(1, 35)) and
          (JsPath \ "lineThree").read(lengthBetween(1, 35)) and
          (JsPath \ "lineFour").readNullable(lengthBetween(1, 35)) and
          (JsPath \ "postalCode").readNullable(lengthBetween(1, 10)) and
          (JsPath \ "countryCode").read(lengthBetween(1, 2).andKeep(verifying(isValidCountryCode))))(Address.apply _)
          .flatMapResult { address =>
            if (!postalCodePresentIfExpected(address)) JsError(JsPath(List(KeyPathNode("postalCode"))), "error.path.missing")
            else JsSuccess(address)
          }
    }

    final case class ContactDetails(
      landline: Option[String],
      mobile: Option[String],
      fax: Option[String],
      emailAddress: Option[String]
    )

    object ContactDetails {
      implicit val reads: Reads[ContactDetails] =
        ((JsPath \ "landline").readNullable(validPhoneNumber) and
          (JsPath \ "mobile").readNullable(validPhoneNumber) and
          (JsPath \ "fax").readNullable(validPhoneNumber) and
          (JsPath \ "emailAddress").readNullable(validEmailAddress))(ContactDetails.apply _)
    }
  }

  final case class Response(ids: Seq[Response.Id])

  object Response {

    implicit val writes: OWrites[Response] =
      (JsPath \ "ids").write[Seq[Id]].contramap(_.ids)

    sealed trait IdType

    object IdType {
      case object ARN extends IdType

      case object SAP extends IdType

      case object SAFE extends IdType
    }

    final case class Id(idType: String, value: String)

    object Id {
      implicit val writes: OWrites[Id] =
        ((JsPath \ "type").write[String] and
          (JsPath \ "value").write[String])(unlift(Id.unapply))
    }

    final case class Error(code: String)

    object Error {
      implicit val writes: OWrites[Error] =
        (JsPath \ "code").write[String].contramap(_.code)
    }

    object ConnectorErrorCode {
      val internalServerError = "500"
      val serviceUnavailable  = "503"
    }

  }
}
