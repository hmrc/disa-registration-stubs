/*
 * Copyright 2026 HM Revenue & Customs
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

package uk.gov.hmrc.disaregistrationstubs.controllers

import play.api.libs.json.{JsObject, Json}
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import uk.gov.hmrc.auth.core.retrieve.Credentials
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals.credentials
import uk.gov.hmrc.auth.core.{AuthConnector, AuthorisationException, AuthorisedFunctions}
import uk.gov.hmrc.disaregistrationstubs.config.AppConfig
import uk.gov.hmrc.disaregistrationstubs.models.GrsCreateJourneyRequest
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController
import uk.gov.hmrc.play.bootstrap.binders.{AbsoluteWithHostnameFromAllowlist, OnlyRelative}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class GrsController @Inject() (
  cc: ControllerComponents,
  val authConnector: AuthConnector,
  appConfig: AppConfig
)(implicit ec: ExecutionContext)
    extends BackendController(cc)
    with AuthorisedFunctions {

  def createLimitedCompanyJourney(): Action[GrsCreateJourneyRequest] =
    Action(parse.json[GrsCreateJourneyRequest]).async { implicit request =>
      authorised()
        .retrieve(credentials) { creds =>
          val response = creds match {
            case None                         => InternalServerError("Internal ID could not be retrieved from Auth")
            case Some(Credentials(credId, _)) =>
              credId match {
                case "grs-create-journey-unauthorised"   => Unauthorized
                case "grs-create-journey-upstream-error" => InternalServerError
                case "grs-create-journey-invalid-json"   =>
                  BadRequest(
                    Json.obj(
                      "code"    -> "INVALID_JSON",
                      "message" -> "Request body is invalid"
                    )
                  )
                case "grs-create-journey-invalid-urls"   =>
                  BadRequest(Json.toJson("JourneyConfig contained non-relative urls"))
                case _ if hasInvalidUrls(request.body)   =>
                  BadRequest(Json.toJson("JourneyConfig contained non-relative urls"))
                case "grs-create-journey-success" | _    =>
                  Created(
                    Json.obj(
                      "journeyStartUrl" ->
                        s"/obligations/enrolment/isa/incorporated-identity-callback?journeyId=$credId"
                    )
                  )
              }
          }
          Future.successful(response)
        }
        .recover { case _: AuthorisationException => Unauthorized }
    }

  def retrieveJourneyData(journeyId: String): Action[AnyContent] = Action.async { implicit request =>
    authorised() {

      val response = journeyId match {

        case "grs-retrieval-unauthorised" =>
          Unauthorized

        case "grs-retrieval-success" =>
          Ok(
            grsJson(
              identifiersMatch = true,
              bvStatus = Some("PASS"),
              registrationStatus = "REGISTERED",
              bpId = Some("111111")
            )
          )

        case "grs-retrieval-identifiers-fail" =>
          Ok(
            grsJson(
              identifiersMatch = false,
              bvStatus = Some("UNCHALLENGED"),
              registrationStatus = "REGISTRATION_FAILED"
            )
          )

        case "grs-retrieval-bv-fail" =>
          Ok(
            grsJson(
              identifiersMatch = true,
              bvStatus = Some("FAIL"),
              registrationStatus = "REGISTERED",
              bpId = Some("111111")
            )
          )

        case "grs-retrieval-bv-not-called" =>
          Ok(
            grsJson(
              identifiersMatch = true,
              bvStatus = Some("UNCHALLENGED"),
              registrationStatus = "REGISTERED",
              bpId = Some("111111")
            )
          )

        case "grs-retrieval-bv-ct-enrolled" =>
          Ok(
            grsJson(
              identifiersMatch = true,
              bvStatus = Some("CT_ENROLLED"),
              registrationStatus = "REGISTERED",
              bpId = Some("111111")
            )
          )

        case "grs-retrieval-registration-failed" =>
          Ok(
            grsJson(
              identifiersMatch = true,
              bvStatus = Some("UNCHALLENGED"),
              registrationStatus = "REGISTRATION_FAILED"
            )
          )

        case "grs-retrieval-registration-not-called" =>
          Ok(
            grsJson(
              identifiersMatch = true,
              bvStatus = None,
              registrationStatus = "REGISTRATION_NOT_CALLED"
            )
          )

        case "grs-retrieval-ct-utr-absent" =>
          Ok(
            grsJson(
              identifiersMatch = true,
              bvStatus = Some("FAIL"),
              registrationStatus = "REGISTERED",
              bpId = Some("111111"),
              includeCtutr = false
            )
          )

        case "grs-retrieval-data-not-found" =>
          NotFound

        case _ =>
          Ok(
            grsJson(
              identifiersMatch = true,
              bvStatus = Some("PASS"),
              registrationStatus = "REGISTERED",
              bpId = Some("111111")
            )
          )
      }

      Future.successful(response)
    }.recover { case _: AuthorisationException =>
      Unauthorized
    }
  }

  private def grsJson(
    identifiersMatch: Boolean,
    bvStatus: Option[String],
    registrationStatus: String,
    bpId: Option[String] = None,
    includeCtutr: Boolean = true
  ): JsObject = {

    val base =
      Json.obj(
        "companyProfile"   -> companyProfileJson,
        "identifiersMatch" -> identifiersMatch,
        "registration"     -> (
          Json.obj("registrationStatus" -> registrationStatus) ++
            bpId.map(id => Json.obj("registeredBusinessPartnerId" -> id)).getOrElse(Json.obj())
        )
      )

    val withBv =
      bvStatus
        .map(status =>
          base ++ Json.obj(
            "businessVerification" -> Json.obj(
              "verificationStatus" -> status
            )
          )
        )
        .getOrElse(base)

    val withCtutr =
      if (includeCtutr) withBv ++ Json.obj("ctutr" -> "1234567890")
      else withBv

    withCtutr
  }

  private val companyProfileJson: JsObject = Json.obj(
    "companyName"            -> "Widgets Ltd",
    "companyNumber"          -> "12345678",
    "dateOfIncorporation"    -> "2020-01-01",
    "unsanitisedCHROAddress" -> Json.obj(
      "address_line_1" -> "1 Street",
      "address_line_2" -> "Town",
      "locality"       -> "County",
      "postal_code"    -> "ZX719AD"
    )
  )

  private def hasInvalidUrls(request: GrsCreateJourneyRequest): Boolean =
    Seq(
      request.continueUrl,
      request.signOutUrl,
      request.accessibilityUrl
    ).exists(url => !isValidUrl(url))

  private def isValidUrl(url: String): Boolean =
    OnlyRelative.applies(url) || AbsoluteWithHostnameFromAllowlist.apply(appConfig.allowedHosts).applies(url)
}
