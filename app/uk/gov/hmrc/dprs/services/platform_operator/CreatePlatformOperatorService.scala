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

package uk.gov.hmrc.dprs.services.platform_operator

import com.google.inject.Inject
import play.api.http.Status._
import play.api.libs.functional.syntax.{toApplicativeOps, toFunctionalBuilderOps}
import play.api.libs.json.Reads.{minLength, verifying}
import play.api.libs.json._
import uk.gov.hmrc.dprs.connectors.BaseBackendConnector
import uk.gov.hmrc.dprs.connectors.BaseConnector.Responses
import uk.gov.hmrc.dprs.connectors.BaseConnector.Responses.Error
import uk.gov.hmrc.dprs.connectors.platform_operator.CreatePlatformOperatorConnector
import uk.gov.hmrc.dprs.converters.platform_operator.CreatePlatformOperatorConverter
import uk.gov.hmrc.dprs.services.BaseService
import uk.gov.hmrc.dprs.services.BaseService.ErrorResponse
import uk.gov.hmrc.dprs.services.platform_operator.CreatePlatformOperatorService.{Request, Response}
import uk.gov.hmrc.dprs.support.ValidationSupport.Reads.{lengthBetween, validEmailAddress, validPhoneNumber}
import uk.gov.hmrc.dprs.support.ValidationSupport.{isPostalCodeRequired, isValidCountryCode}

import scala.concurrent.{ExecutionContext, Future}

class CreatePlatformOperatorService @Inject() (createPlatformOperatorConnector: CreatePlatformOperatorConnector) extends BaseService {

  private val converter = new CreatePlatformOperatorConverter

  def call(
    subscriptionId: String,
    request: Request,
    requestHeaders: BaseBackendConnector.Request.Headers
  )(implicit
    executionContext: ExecutionContext
  ): Future[Either[ErrorResponse, Option[Response]]] =
    converter.convert(subscriptionId, request) match {
      case Some(connectorRequest) =>
        createPlatformOperatorConnector.call(connectorRequest, requestHeaders).map {
          case Right(response) => Right(converter.convert(response))
          case Left(error)     => Left(convert(error))
        }
      case None => Future.successful(Right(None))
    }

  override protected def convert(connectorError: Responses.Error): BaseService.ErrorResponse = {
    import BaseService.{ErrorCodes => ServiceErrorCodes}
    import uk.gov.hmrc.dprs.services.platform_operator.CreatePlatformOperatorService.Response.ConnectorErrorCode._
    connectorError match {
      case Error(BAD_REQUEST, None)                                        => ErrorResponse(INTERNAL_SERVER_ERROR)
      case Error(FORBIDDEN, None)                                          => ErrorResponse(FORBIDDEN, Some(ServiceErrorCodes.forbidden))
      case Error(INTERNAL_SERVER_ERROR, None)                              => ErrorResponse(SERVICE_UNAVAILABLE, Some(ServiceErrorCodes.internalServerError))
      case Error(UNPROCESSABLE_ENTITY, Some(`insufficientData`))           => ErrorResponse(INTERNAL_SERVER_ERROR)
      case Error(UNPROCESSABLE_ENTITY, Some(`missingRequestTypeOfRegime`)) => ErrorResponse(INTERNAL_SERVER_ERROR)
      case _                                                               => ErrorResponse(connectorError.status)
    }
  }

}

object CreatePlatformOperatorService {

  final case class Request(internalName: String,
                           businessName: Option[String],
                           tradingName: Option[String],
                           ids: Seq[Request.ID],
                           contacts: Seq[Request.Contact],
                           address: Request.Address,
                           reportingNotification: Request.ReportingNotification
  )

  object Request {

    implicit lazy val reads: Reads[Request] =
      ((JsPath \ "internalName").read[String](lengthBetween(1, 105)) and
        (JsPath \ "businessName").readNullable[String](lengthBetween(1, 105)) and
        (JsPath \ "tradingName").readNullable[String](lengthBetween(1, 80)) and
        (JsPath \ "ids").read[Seq[Request.ID]] and
        (JsPath \ "contacts").read[Seq[Request.Contact]] and
        (JsPath \ "address").read[Request.Address] and
        (JsPath \ "reportingNotification").read[Request.ReportingNotification])(Request.apply _).flatMapResult { request =>
        if (request.contacts.isEmpty) JsError((JsPath(List(KeyPathNode("contacts"))), JsonValidationError(Seq("error.minLength"), 1)))
        else if (request.contacts.size > 2) JsError((JsPath(List(KeyPathNode("contacts"))), JsonValidationError(Seq("error.maxLength"), 2)))
        else JsSuccess(request)
      }

    final case class ID(_type: Request.IDType, value: String, countryCodeOfIssue: String)

    object ID {
      implicit lazy val reads: Reads[ID] =
        ((JsPath \ "type").read[Request.IDType] and
          (JsPath \ "value").read[String](lengthBetween(1, 25)) and
          (JsPath \ "countryCodeOfIssue").read[String](lengthBetween(1, 2).andKeep(verifying(isValidCountryCode))))(ID.apply _)
    }

    trait IDType

    object IDType {

      implicit lazy val reads: Reads[IDType] =
        JsPath.read[String](minLength[String](1).andKeep(verifying(IDType.all.map(_.toString).contains))).map {
          case "BROCS"  => IDType.BROCS
          case "CHRN"   => IDType.CHRN
          case "CRN"    => IDType.CRN
          case "EMPREF" => IDType.EMPREF
          case "OTHER"  => IDType.OTHER
          case "UTR"    => IDType.UTR
          case "VRN"    => IDType.VRN
        }

      val all: Set[IDType] = Set(BROCS, CHRN, CRN, EMPREF, OTHER, UTR, VRN)

      case object BROCS extends IDType
      case object CHRN extends IDType
      case object CRN extends IDType
      case object EMPREF extends IDType
      case object OTHER extends IDType
      case object UTR extends IDType
      case object VRN extends IDType

    }

    final case class ReportingNotification(_type: ReportingNotification.ReportingNotificationType, isActiveSeller: Boolean, isDueDiligence: Boolean, year: Int)

    object ReportingNotification {

      implicit lazy val reads: Reads[ReportingNotification] =
        ((JsPath \ "type").read[ReportingNotification.ReportingNotificationType] and
          (JsPath \ "isActiveSeller").read[Boolean] and
          (JsPath \ "isDueDiligence").read[Boolean] and
          (JsPath \ "year").read[Int](verifying[Int](_ >= 2024)))(ReportingNotification.apply _)

      trait ReportingNotificationType

      object ReportingNotificationType {

        implicit lazy val reads: Reads[ReportingNotificationType] =
          JsPath.read[String](minLength[String](1).andKeep(verifying(ReportingNotificationType.all.map(_.toString).contains))).map {
            case "RPO" => ReportingNotificationType.RPO
            case "EPO" => ReportingNotificationType.EPO
          }

        val all: Set[ReportingNotificationType] = Set(RPO, EPO)

        case object RPO extends ReportingNotificationType
        case object EPO extends ReportingNotificationType
      }
    }

    final case class Address(lineOne: String, lineTwo: String, lineThree: String, lineFour: Option[String], postalCode: Option[String], countryCode: String)

    object Address {

      private def postalCodePresentIfExpected(address: Request.Address): Boolean =
        if (isPostalCodeRequired(address.countryCode)) address.postalCode.isDefined else true

      implicit lazy val reads: Reads[Address] =
        ((JsPath \ "lineOne").read[String](lengthBetween(1, 35)) and
          (JsPath \ "lineTwo").read[String](lengthBetween(1, 35)) and
          (JsPath \ "lineThree").read[String](lengthBetween(1, 35)) and
          (JsPath \ "lineFour").readNullable[String](lengthBetween(1, 35)) and
          (JsPath \ "postalCode").readNullable[String](lengthBetween(1, 10)) and
          (JsPath \ "countryCode").read[String](lengthBetween(2, 2).andKeep(verifying(isValidCountryCode))))(Address.apply _)
          .flatMapResult { address =>
            if (!postalCodePresentIfExpected(address)) JsError(JsPath(List(KeyPathNode("postalCode"))), "error.path.missing")
            else JsSuccess(address)
          }
    }

    final case class Contact(name: String, phone: Option[String], emailAddress: String)

    object Contact {
      implicit lazy val reads: Reads[Contact] =
        ((JsPath \ "name").read[String](lengthBetween(1, 105)) and
          (JsPath \ "phone").readNullable[String](validPhoneNumber) and
          (JsPath \ "emailAddress").read[String](lengthBetween(1, 132).keepAnd(validEmailAddress)))(Contact.apply _)
    }
  }

  final case class Response(platformOperatorId: String)

  object Response {

    object ConnectorErrorCode {
      val insufficientData           = "005"
      val missingRequestTypeOfRegime = "001"
    }

  }

}
