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

package uk.gov.hmrc.dprs.services

import com.google.inject.Inject
import play.api.http.Status._
import play.api.libs.functional.syntax.{toApplicativeOps, toFunctionalBuilderOps}
import play.api.libs.json.Reads.maxLength
import play.api.libs.json._
import uk.gov.hmrc.dprs.connectors.{BaseConnector, UpdateSubscriptionConnector}
import uk.gov.hmrc.dprs.services.BaseService.{ErrorCodeWithStatus, ErrorCodes}
import uk.gov.hmrc.dprs.services.UpdateSubscriptionService.Converter
import uk.gov.hmrc.dprs.services.UpdateSubscriptionService.Requests.Request.Contact
import uk.gov.hmrc.dprs.support.ValidationSupport.Reads.{lengthBetween, validEmailAddress, validPhoneNumber}
import uk.gov.hmrc.http.HeaderCarrier

import java.time.{Clock, Instant}
import scala.concurrent.{ExecutionContext, Future}

class UpdateSubscriptionService @Inject() (clock: Clock,
                                           acknowledgementReferenceGenerator: AcknowledgementReferenceGenerator,
                                           updateSubscriptionConnector: UpdateSubscriptionConnector
) extends BaseService {

  private val converter = new Converter(clock, acknowledgementReferenceGenerator)
  override val errorStatusCodeConversions: Map[Int, ErrorCodeWithStatus] =
    Map(
      INTERNAL_SERVER_ERROR -> ErrorCodeWithStatus(SERVICE_UNAVAILABLE, Some(ErrorCodes.internalServerError)),
      SERVICE_UNAVAILABLE   -> ErrorCodeWithStatus(SERVICE_UNAVAILABLE, Some(ErrorCodes.serviceUnavailableError)),
      CONFLICT              -> ErrorCodeWithStatus(INTERNAL_SERVER_ERROR),
      NOT_FOUND             -> ErrorCodeWithStatus(NOT_FOUND, Some(ErrorCodes.notFound)),
      BAD_REQUEST           -> ErrorCodeWithStatus(INTERNAL_SERVER_ERROR)
    )

  def call(id: String, serviceRequest: UpdateSubscriptionService.Requests.Request)(implicit
    headerCarrier: HeaderCarrier,
    executionContext: ExecutionContext
  ): Future[Either[BaseService.ErrorCodeWithStatus, Unit]] =
    converter
      .convert(id, serviceRequest)
      .map {
        updateSubscriptionConnector.call(_).map {
          case Right(_)                              => Right(())
          case Left(BaseConnector.Error(statusCode)) => Left(convert(statusCode))
        }
      }
      .getOrElse(Future.successful(Left(convert(BAD_REQUEST))))

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
  }

  class Converter(clock: Clock, acknowledgementReferenceGenerator: AcknowledgementReferenceGenerator) {

    /** We're awaiting the specs for the underlying API; in the meantime, we'll use the one for MDR; this matches the expectations of the stub service.
      */
    private val regime            = "MDR"
    private val originatingSystem = "MDTP"

    def convert(id: String, request: Requests.Request): Option[UpdateSubscriptionConnector.Requests.Request] =
      request.contacts.headOption
        .map { primaryContact =>
          UpdateSubscriptionConnector.Requests.Request(
            common = generateRequestCommon(),
            detail = UpdateSubscriptionConnector.Requests.Detail(
              idType = "MDR",
              idNumber = id,
              tradingName = request.name,
              isGBUser = true, // TODO: Determine this.
              primaryContact = convert(primaryContact),
              secondaryContact = request.contacts.tail.headOption.map(convert)
            )
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

    private def generateRequestCommon() = UpdateSubscriptionConnector.Requests.Common(
      receiptDate = Instant.now(clock).toString,
      regime = regime,
      acknowledgementReference = acknowledgementReferenceGenerator.generate(),
      originatingSystem = originatingSystem
    )
  }
}
