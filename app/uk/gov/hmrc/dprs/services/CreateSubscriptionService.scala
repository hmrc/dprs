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
import play.api.http.Status.BAD_REQUEST
import play.api.libs.functional.syntax.{toApplicativeOps, toFunctionalBuilderOps}
import play.api.libs.json.Reads.{maxLength, minLength, verifying}
import play.api.libs.json._
import uk.gov.hmrc.dprs.connectors.{BaseConnector, CreateSubscriptionConnector}
import uk.gov.hmrc.dprs.services.CreateSubscriptionService.Requests.Request.{Contact, Id}
import uk.gov.hmrc.dprs.support.ValidationSupport.Reads.{lengthBetween, validEmailAddress, validPhoneNumber}
import uk.gov.hmrc.http.HeaderCarrier

import java.time.{Clock, Instant}
import scala.concurrent.{ExecutionContext, Future}

class CreateSubscriptionService @Inject() (clock: Clock,
                                           acknowledgementReferenceGenerator: AcknowledgementReferenceGenerator,
                                           createSubscriptionConnector: CreateSubscriptionConnector
) extends BaseService {

  private val converter = new CreateSubscriptionService.Converter(clock, acknowledgementReferenceGenerator)

  def call(serviceRequest: CreateSubscriptionService.Requests.Request)(implicit
    headerCarrier: HeaderCarrier,
    executionContext: ExecutionContext
  ): Future[Either[BaseService.ErrorCodeWithStatus, CreateSubscriptionService.Responses.Response]] =
    converter
      .convert(serviceRequest)
      .map { connectorRequest =>
        createSubscriptionConnector.call(connectorRequest).map {
          case Right(connectorResponse)              => Right(converter.convert(connectorResponse))
          case Left(BaseConnector.Error(statusCode)) => Left(convert(statusCode))
        }
      }
      .getOrElse(Future.successful(Left(convert(BAD_REQUEST))))
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
    }

  }

  class Converter(clock: Clock, acknowledgementReferenceGenerator: AcknowledgementReferenceGenerator) {

    /** We're awaiting the specs for the underlying API; in the meantime, we'll use the one for MDR; this matches the expectations of the stub service.
      */
    // This is sort of what I was referring to with the literals, where we have then declared as constants.
    // For reusable ones, they could be in a shared class that can be used anywhere

    private val regime            = "MDR"
    private val originatingSystem = "MDTP"

    def convert(request: Requests.Request): Option[CreateSubscriptionConnector.Requests.Request] =
      request.contacts.headOption
        .map { primaryContact =>
          CreateSubscriptionConnector.Requests.Request(
            common = generateRequestCommon(),
            detail = CreateSubscriptionConnector.Requests.Detail(
              idType = request.id.idType.toString,
              idNumber = request.id.value,
              tradingName = request.name,
              isGBUser = true, // TODO: Determine this.
              primaryContact = convert(primaryContact),
              secondaryContact = request.contacts.tail.headOption.map(convert)
            )
          )
        }

    def convert(response: CreateSubscriptionConnector.Responses.Response): Responses.Response =
      Responses.Response(id = response.id)

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

    private def generateRequestCommon() = CreateSubscriptionConnector.Requests.Common(
      receiptDate = Instant.now(clock).toString,
      regime = regime,
      acknowledgementReference = acknowledgementReferenceGenerator.generate(),
      originatingSystem = originatingSystem
    )
  }
}
