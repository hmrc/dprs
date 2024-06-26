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

package uk.gov.hmrc.dprs.services.subscription

import com.google.inject.Inject
import play.api.http.Status._
import play.api.libs.functional.syntax.{toApplicativeOps, toFunctionalBuilderOps}
import play.api.libs.json.Reads.{maxLength, minLength, verifying}
import play.api.libs.json._
import uk.gov.hmrc.dprs.connectors.BaseBackendConnector
import uk.gov.hmrc.dprs.connectors.BaseConnector.Responses.Error
import uk.gov.hmrc.dprs.connectors.subscription.CreateSubscriptionConnector
import uk.gov.hmrc.dprs.services.BaseService
import uk.gov.hmrc.dprs.services.BaseService.ErrorResponse
import uk.gov.hmrc.dprs.services.subscription.CreateSubscriptionService.Converter
import uk.gov.hmrc.dprs.services.subscription.CreateSubscriptionService.Requests.Request.{Contact, Id}
import uk.gov.hmrc.dprs.services.subscription.CreateSubscriptionService.Responses.Response.ConnectorErrorCode
import uk.gov.hmrc.dprs.support.ValidationSupport.Reads.{lengthBetween, validEmailAddress, validPhoneNumber}

import scala.concurrent.{ExecutionContext, Future}

class CreateSubscriptionService @Inject() (
  createSubscriptionConnector: CreateSubscriptionConnector
) extends BaseService {

  private val converter = new Converter

  def call(serviceRequest: CreateSubscriptionService.Requests.Request, requestHeaders: BaseBackendConnector.Request.Headers)(implicit
    executionContext: ExecutionContext
  ): Future[Either[BaseService.ErrorResponse, CreateSubscriptionService.Responses.Response]] =
    converter
      .convert(serviceRequest)
      .map { connectorRequest =>
        createSubscriptionConnector.call(connectorRequest, requestHeaders).map {
          case Right(connectorResponse) => Right(converter.convert(connectorResponse))
          case Left(error)              => Left(convert(error))
        }
      }
      .getOrElse(Future.successful(Left(convert(Error(INTERNAL_SERVER_ERROR)))))

  override protected def convert(connectorError: Error): ErrorResponse = {
    import BaseService.{ErrorCodes => ServiceErrorCodes}
    import ConnectorErrorCode._
    connectorError match {
      case Error(INTERNAL_SERVER_ERROR, None)                        => ErrorResponse(SERVICE_UNAVAILABLE, Some(ServiceErrorCodes.internalServerError))
      case Error(INTERNAL_SERVER_ERROR, Some(`couldNotBeProcessed`)) => ErrorResponse(INTERNAL_SERVER_ERROR)
      case Error(INTERNAL_SERVER_ERROR, Some(`malformedPayload`))    => ErrorResponse(INTERNAL_SERVER_ERROR)
      case Error(UNPROCESSABLE_ENTITY, Some(`duplicateSubmission`))  => ErrorResponse(CONFLICT, Some(ServiceErrorCodes.conflict))
      case Error(UNPROCESSABLE_ENTITY, Some(`couldNotBeProcessed`))  => ErrorResponse(SERVICE_UNAVAILABLE, Some(ServiceErrorCodes.serviceUnavailableError))
      case Error(UNPROCESSABLE_ENTITY, Some(`invalidId`))            => ErrorResponse(INTERNAL_SERVER_ERROR)
      case Error(INTERNAL_SERVER_ERROR, Some(`unauthorised`))        => ErrorResponse(UNAUTHORIZED, Some(ServiceErrorCodes.unauthorised))
      case Error(INTERNAL_SERVER_ERROR, Some(`forbidden`))           => ErrorResponse(FORBIDDEN, Some(ServiceErrorCodes.forbidden))
      case _                                                         => ErrorResponse(connectorError.status)
    }
  }
}

object CreateSubscriptionService {

  object Requests {

    final case class Request(id: Id, name: Option[String], contacts: Seq[Contact])

    object Request {

      implicit val reads: Reads[Request] =
        ((JsPath \ "id").read[Id] and
          (JsPath \ "name").readNullable(lengthBetween(1, 80)) and
          (JsPath \ "contacts").read[Seq[Contact]])(Request.apply _).flatMapResult { request =>
          if (request.contacts.isEmpty) JsError((JsPath(List(KeyPathNode("contacts"))), JsonValidationError(Seq("error.minLength"), 1)))
          else if (request.contacts.size > 2) JsError((JsPath(List(KeyPathNode("contacts"))), JsonValidationError(Seq("error.maxLength"), 2)))
          else JsSuccess(request)
        }

      final case class Id(idType: IdType, value: String)

      object Id {
        implicit val reads: Reads[Id] =
          ((JsPath \ "type").read[IdType] and
            (JsPath \ "value").read[String](lengthBetween(1, 15)))(Id.apply _)
      }

      trait IdType

      object IdType {

        val all: Set[IdType] = Set(NINO, SAFE, UTR)

        case object NINO extends IdType

        case object UTR extends IdType

        case object SAFE extends IdType

        implicit val reads: Reads[IdType] =
          JsPath.read[String](minLength[String](1).andKeep(verifying(all.map(_.toString).contains))).map {
            case "UTR"  => IdType.UTR
            case "NINO" => IdType.NINO
            case "SAFE" => IdType.SAFE
          }

      }

      sealed trait Contact {
        def landline: Option[String]

        def mobile: Option[String]

        def emailAddress: String
      }

      object Contact {
        private val invalidTypeError = JsError(JsPath(List(KeyPathNode("type"))), "error.invalid")
        implicit val reads: Reads[Contact] = (json: JsValue) =>
          (json \ "type").toOption
            .map {
              _.as[JsString].value.trim.toUpperCase match {
                case "I" => json.validate[Individual]
                case "O" => json.validate[Organisation]
                case _   => invalidTypeError
              }
            }
            .getOrElse(invalidTypeError)
      }

      final case class Individual(firstName: String,
                                  middleName: Option[String],
                                  lastName: String,
                                  landline: Option[String],
                                  mobile: Option[String],
                                  emailAddress: String
      ) extends Contact

      object Individual {

        implicit val reads: Reads[Individual] =
          ((JsPath \ "firstName").read(lengthBetween(1, 35)) and
            (JsPath \ "middleName").readNullable(lengthBetween(1, 35)) and
            (JsPath \ "lastName").read(lengthBetween(1, 35)) and
            (JsPath \ "landline").readNullable(validPhoneNumber) and
            (JsPath \ "mobile").readNullable(validPhoneNumber) and
            (JsPath \ "emailAddress").read(maxLength[String](132).keepAnd(validEmailAddress)))(Individual.apply _)
      }

      final case class Organisation(name: String, landline: Option[String], mobile: Option[String], emailAddress: String) extends Contact

      object Organisation {
        implicit val reads: Reads[Organisation] =
          ((JsPath \ "name").read[String](lengthBetween(1, 105)) and
            (JsPath \ "landline").readNullable(validPhoneNumber) and
            (JsPath \ "mobile").readNullable(validPhoneNumber) and
            (JsPath \ "emailAddress").read(maxLength[String](132).keepAnd(validEmailAddress)))(Organisation.apply _)
      }

    }
  }

  object Responses {

    final case class Response(id: String)

    object Response {
      implicit val writes: OWrites[Response] =
        (JsPath \ "id").write[String].contramap(_.id)

      object ConnectorErrorCode {
        val couldNotBeProcessed = "003"
        val duplicateSubmission = "004"
        val forbidden           = "403"
        val invalidId           = "016"
        val malformedPayload    = "400"
        val unauthorised        = "401"
      }

    }

  }

  class Converter {

    def convert(request: Requests.Request): Option[CreateSubscriptionConnector.Requests.Request] =
      request.contacts.headOption
        .map { primaryContact =>
          CreateSubscriptionConnector.Requests.Request(
            idType = request.id.idType.toString,
            idNumber = request.id.value,
            tradingName = request.name,
            gbUser = true, // TODO: Determine this.
            primaryContact = convert(primaryContact),
            secondaryContact = request.contacts.tail.headOption.map(convert)
          )
        }

    def convert(response: CreateSubscriptionConnector.Responses.Response): Responses.Response =
      Responses.Response(id = response.dprsReference)

    private def convert(contact: Requests.Request.Contact): CreateSubscriptionConnector.Requests.Contact =
      contact match {
        case Requests.Request.Individual(firstName, middleName, lastName, landline, mobile, emailAddress) =>
          CreateSubscriptionConnector.Requests.Contact(
            landline = landline,
            mobile = mobile,
            emailAddress = emailAddress,
            individualDetails =
              Some(CreateSubscriptionConnector.Requests.Contact.IndividualDetails(firstName = firstName, middleName = middleName, lastName = lastName)),
            organisationDetails = None
          )
        case Requests.Request.Organisation(name, landline, mobile, emailAddress) =>
          CreateSubscriptionConnector.Requests.Contact(
            landline = landline,
            mobile = mobile,
            emailAddress = emailAddress,
            individualDetails = None,
            organisationDetails = Some(CreateSubscriptionConnector.Requests.Contact.OrganisationDetails(name = name))
          )
      }
  }
}
