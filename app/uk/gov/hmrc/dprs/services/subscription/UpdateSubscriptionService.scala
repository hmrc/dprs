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
import play.api.libs.json.Reads.maxLength
import play.api.libs.json._
import uk.gov.hmrc.dprs.connectors.BaseBackendConnector
import uk.gov.hmrc.dprs.connectors.BaseConnector.Responses.Error
import uk.gov.hmrc.dprs.connectors.subscription.UpdateSubscriptionConnector
import uk.gov.hmrc.dprs.services.BaseService
import uk.gov.hmrc.dprs.services.BaseService.ErrorResponse
import uk.gov.hmrc.dprs.services.subscription.UpdateSubscriptionService.Converter
import uk.gov.hmrc.dprs.services.subscription.UpdateSubscriptionService.Requests.Request.Contact
import uk.gov.hmrc.dprs.services.subscription.UpdateSubscriptionService.Requests.Response.ConnectorErrorCode
import uk.gov.hmrc.dprs.support.ValidationSupport.Reads.{lengthBetween, validEmailAddress, validPhoneNumber}

import scala.concurrent.{ExecutionContext, Future}

class UpdateSubscriptionService @Inject() (updateSubscriptionConnector: UpdateSubscriptionConnector) extends BaseService {

  private val converter = new Converter

  def call(id: String, serviceRequest: UpdateSubscriptionService.Requests.Request, requestHeaders: BaseBackendConnector.Request.Headers)(implicit
    executionContext: ExecutionContext
  ): Future[Either[BaseService.ErrorResponse, Unit]] =
    converter
      .convert(id, serviceRequest)
      .map {
        updateSubscriptionConnector.call(_, requestHeaders).map {
          case Right(_)    => Right(())
          case Left(error) => Left(convert(error))
        }
      }
      .getOrElse(Future.successful(Left(ErrorResponse(SERVICE_UNAVAILABLE))))

  override protected def convert(connectorError: Error): ErrorResponse = {
    import BaseService.{ErrorCodes => Service}
    import ConnectorErrorCode._
    connectorError match {
      case Error(INTERNAL_SERVER_ERROR, Some(`internalServerError`))    => ErrorResponse(SERVICE_UNAVAILABLE, Some(Service.internalServerError))
      case Error(INTERNAL_SERVER_ERROR, Some(`unauthorised`))           => ErrorResponse(UNAUTHORIZED, Some(Service.unauthorised))
      case Error(INTERNAL_SERVER_ERROR, Some(`forbidden`))              => ErrorResponse(FORBIDDEN, Some(Service.forbidden))
      case Error(UNPROCESSABLE_ENTITY, Some(`couldNotBeProcessed`))     => ErrorResponse(SERVICE_UNAVAILABLE, Some(Service.serviceUnavailableError))
      case Error(UNPROCESSABLE_ENTITY, Some(`createOrAmendInProgress`)) => ErrorResponse(SERVICE_UNAVAILABLE, Some(Service.serviceUnavailableError))
      case Error(UNPROCESSABLE_ENTITY, Some(`couldNotBeProcessed`))     => ErrorResponse(CONFLICT, Some(Service.conflict))
      case Error(UNPROCESSABLE_ENTITY, Some(`noSubscription`))          => ErrorResponse(NOT_FOUND, Some(Service.notFound))
      case Error(UNPROCESSABLE_ENTITY, Some(`duplicateSubmission`))     => ErrorResponse(CONFLICT, Some(Service.conflict))
      case _                                                            => ErrorResponse(INTERNAL_SERVER_ERROR)
    }
  }

}

object UpdateSubscriptionService {

  object Requests {

    final case class Request(name: Option[String], contacts: Seq[Contact])

    object Request {

      implicit val reads: Reads[Request] =
        ((JsPath \ "name").readNullable(lengthBetween(1, 80)) and
          (JsPath \ "contacts").read[Seq[Contact]])(Request.apply _).flatMapResult { request =>
          if (request.contacts.isEmpty) JsError((JsPath(List(KeyPathNode("contacts"))), JsonValidationError(Seq("error.minLength"), 1)))
          else if (request.contacts.size > 2) JsError((JsPath(List(KeyPathNode("contacts"))), JsonValidationError(Seq("error.maxLength"), 2)))
          else JsSuccess(request)
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

    object Response {
      object ConnectorErrorCode {
        val couldNotBeProcessed     = "003"
        val createOrAmendInProgress = "201"
        val duplicateSubmission     = "004"
        val forbidden               = "403"
        val internalServerError     = "500"
        val noSubscription          = "202"
        val unauthorised            = "401"
      }
    }
  }

  class Converter {

    def convert(id: String, request: Requests.Request): Option[UpdateSubscriptionConnector.Requests.Request] =
      request.contacts.headOption
        .map { primaryContact =>
          UpdateSubscriptionConnector.Requests.Request(
            idType = "DPRS",
            idNumber = id,
            tradingName = request.name,
            isGBUser = true, // TODO: Determine this.
            primaryContact = convert(primaryContact),
            secondaryContact = request.contacts.tail.headOption.map(convert)
          )
        }

    private def convert(contact: Requests.Request.Contact): UpdateSubscriptionConnector.Requests.Contact =
      contact match {
        case Requests.Request.Individual(firstName, middleName, lastName, landline, mobile, emailAddress) =>
          UpdateSubscriptionConnector.Requests.Contact(
            landline = landline,
            mobile = mobile,
            emailAddress = emailAddress,
            individualDetails =
              Some(UpdateSubscriptionConnector.Requests.Contact.IndividualDetails(firstName = firstName, middleName = middleName, lastName = lastName)),
            organisationDetails = None
          )
        case Requests.Request.Organisation(name, landline, mobile, emailAddress) =>
          UpdateSubscriptionConnector.Requests.Contact(
            landline = landline,
            mobile = mobile,
            emailAddress = emailAddress,
            individualDetails = None,
            organisationDetails = Some(UpdateSubscriptionConnector.Requests.Contact.OrganisationDetails(name = name))
          )
      }

  }
}
