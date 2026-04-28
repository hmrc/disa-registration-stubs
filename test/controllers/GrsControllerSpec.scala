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

package controllers

import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatest.matchers.must.Matchers.mustBe
import play.api.libs.json.{JsObject, JsValue, Json}
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import uk.gov.hmrc.auth.core.*
import utils.BaseUnitSpec

import scala.concurrent.Future

class GrsControllerSpec extends BaseUnitSpec {

  private def journeyRetrievalRequest(journeyId: String) =
    FakeRequest(GET, s"/journey/$journeyId")

  private def journeyRetrievalJson(result: Future[play.api.mvc.Result]): JsValue =
    contentAsJson(result)

  private val createLimitedCompanyJourneyUrl =
    "/incorporated-entity-identification/api/limited-company-journey"

  private val validCreateJourneyJson: JsObject = Json.obj(
    "continueUrl"               -> "/testUrl",
    "businessVerificationCheck" -> true,
    "deskProServiceId"          -> "DeskProServiceId",
    "signOutUrl"                -> "/testSignOutUrl",
    "regime"                    -> "DISA",
    "accessibilityUrl"          -> "/accessibility-statement/my-service"
  )

  private val invalidUrlsCreateJourneyJson: JsObject =
    validCreateJourneyJson ++ Json.obj(
      "continueUrl" -> "https://example.com/testUrl"
    )

  private def createLimitedCompanyJourneyRequest(json: JsValue = validCreateJourneyJson) =
    FakeRequest(POST, createLimitedCompanyJourneyUrl)
      .withHeaders(CONTENT_TYPE -> "application/json")
      .withJsonBody(json)

  "GrsController.retrieveJourneyData" should {

    "return 200 with success payload" in {
      running(fakeApplication()) {
        authorisedUser()

        val result = route(app, journeyRetrievalRequest("grs-retrieval-success")).get

        status(result) mustBe OK
        (journeyRetrievalJson(result) \ "identifiersMatch").as[Boolean] mustBe true
        (journeyRetrievalJson(result) \ "registration" \ "registrationStatus").as[String] mustBe "REGISTERED"
        (journeyRetrievalJson(result) \ "businessVerification" \ "verificationStatus").as[String] mustBe "PASS"
        (journeyRetrievalJson(result) \ "ctutr").as[String] mustBe "1234567890"
      }
    }

    "return identifiers mismatch scenario" in {
      running(fakeApplication()) {
        authorisedUser()

        val result = route(app, journeyRetrievalRequest("grs-retrieval-identifiers-fail")).get

        status(result) mustBe OK
        (journeyRetrievalJson(result) \ "identifiersMatch").as[Boolean] mustBe false
        (journeyRetrievalJson(result) \ "registration" \ "registrationStatus").as[String] mustBe "REGISTRATION_FAILED"
      }
    }

    "return BV fail scenario" in {
      running(fakeApplication()) {
        authorisedUser()

        val result = route(app, journeyRetrievalRequest("grs-retrieval-bv-fail")).get

        status(result) mustBe OK
        (journeyRetrievalJson(result) \ "businessVerification" \ "verificationStatus").as[String] mustBe "FAIL"
      }
    }

    "return BV not called (UNCHALLENGED)" in {
      running(fakeApplication()) {
        authorisedUser()

        val result = route(app, journeyRetrievalRequest("grs-retrieval-bv-not-called")).get

        status(result) mustBe OK
        (journeyRetrievalJson(result) \ "businessVerification" \ "verificationStatus").as[String] mustBe "UNCHALLENGED"
      }
    }

    "return CT enrolled scenario" in {
      running(fakeApplication()) {
        authorisedUser()

        val result = route(app, journeyRetrievalRequest("grs-retrieval-bv-ct-enrolled")).get

        status(result) mustBe OK
        (journeyRetrievalJson(result) \ "businessVerification" \ "verificationStatus").as[String] mustBe "CT_ENROLLED"
      }
    }

    "return registration failed scenario" in {
      running(fakeApplication()) {
        authorisedUser()

        val result = route(app, journeyRetrievalRequest("grs-retrieval-registration-failed")).get

        status(result) mustBe OK
        (journeyRetrievalJson(result) \ "registration" \ "registrationStatus").as[String] mustBe "REGISTRATION_FAILED"
      }
    }

    "return registration not called scenario" in {
      running(fakeApplication()) {
        authorisedUser()

        val result = route(app, journeyRetrievalRequest("grs-retrieval-registration-not-called")).get

        status(result) mustBe OK
        (journeyRetrievalJson(result) \ "registration" \ "registrationStatus")
          .as[String] mustBe "REGISTRATION_NOT_CALLED"
      }
    }

    "return response without ctutr when ct-utr-absent" in {
      running(fakeApplication()) {
        authorisedUser()

        val result = route(app, journeyRetrievalRequest("grs-retrieval-ct-utr-absent")).get

        status(result) mustBe OK
        (journeyRetrievalJson(result) \ "ctutr").toOption mustBe None
      }
    }

    "return 404 when journey not found" in {
      running(fakeApplication()) {
        authorisedUser()

        val result = route(app, journeyRetrievalRequest("grs-retrieval-data-not-found")).get

        status(result) mustBe NOT_FOUND
      }
    }

    "return 401 when journeyId indicates stubbed unauthorised scenario" in {
      running(fakeApplication()) {
        authorisedUser()

        val result = route(app, journeyRetrievalRequest("grs-retrieval-unauthorised")).get

        status(result) mustBe UNAUTHORIZED
      }
    }

    "default to success-like response for unknown journeyId" in {
      running(fakeApplication()) {
        authorisedUser()

        val result = route(app, journeyRetrievalRequest("something-random")).get

        status(result) mustBe OK
        (journeyRetrievalJson(result) \ "registration" \ "registrationStatus").as[String] mustBe "REGISTERED"
      }
    }

    "return 401 when authorisation throws" in {
      running(fakeApplication()) {
        when(
          mockAuthConnector.authorise(
            any(),
            any()
          )(any(), any())
        ).thenReturn(Future.failed(InsufficientEnrolments()))

        val result = route(app, journeyRetrievalRequest("grs-retrieval-success")).get

        status(result) mustBe UNAUTHORIZED
      }
    }
  }

  "GrsController.createLimitedCompanyJourney" should {

    "return 201 with journeyStartUrl when credId indicates create journey success" in {
      running(fakeApplication()) {
        authorisedUser(Some("grs-create-journey-success"))

        val result = route(app, createLimitedCompanyJourneyRequest()).get

        status(result) mustBe CREATED
        (journeyRetrievalJson(result) \ "journeyStartUrl").as[String] mustBe
          "http://localhost:1202/incorporated-identity-callback?journeyId=grs-create-journey-success"
      }
    }

    "return 201 with journeyStartUrl using GRS retrieval scenario credId" in {
      running(fakeApplication()) {
        authorisedUser(Some("bv-fail"))

        val result = route(app, createLimitedCompanyJourneyRequest()).get

        status(result) mustBe CREATED
        (journeyRetrievalJson(result) \ "journeyStartUrl").as[String] mustBe
          "http://localhost:1202/incorporated-identity-callback?journeyId=bv-fail"
      }
    }

    "return 401 when credId indicates create journey unauthorised" in {
      running(fakeApplication()) {
        authorisedUser(Some("grs-create-journey-unauthorised"))

        val result = route(app, createLimitedCompanyJourneyRequest()).get

        status(result) mustBe UNAUTHORIZED
      }
    }

    "return 500 when credId indicates create journey upstream error" in {
      running(fakeApplication()) {
        authorisedUser(Some("grs-create-journey-upstream-error"))

        val result = route(app, createLimitedCompanyJourneyRequest()).get

        status(result) mustBe INTERNAL_SERVER_ERROR
      }
    }

    "return 400 when credId indicates invalid json stub scenario" in {
      running(fakeApplication()) {
        authorisedUser(Some("grs-create-journey-invalid-json"))

        val result = route(app, createLimitedCompanyJourneyRequest()).get

        status(result) mustBe BAD_REQUEST
        (journeyRetrievalJson(result) \ "code").as[String] mustBe "INVALID_JSON"
        (journeyRetrievalJson(result) \ "message").as[String] mustBe "Request body is invalid"
      }
    }

    "return 400 when credId indicates invalid urls stub scenario" in {
      running(fakeApplication()) {
        authorisedUser(Some("grs-create-journey-invalid-urls"))

        val result = route(app, createLimitedCompanyJourneyRequest()).get

        status(result) mustBe BAD_REQUEST
        contentAsString(result) should include("JourneyConfig contained non-relative urls")
      }
    }

    "return 400 when request contains non-relative urls" in {
      running(fakeApplication()) {
        authorisedUser(Some("grs-create-journey-success"))

        val result = route(app, createLimitedCompanyJourneyRequest(invalidUrlsCreateJourneyJson)).get

        status(result) mustBe BAD_REQUEST
        contentAsString(result) should include("JourneyConfig contained non-relative urls")
      }
    }

    "return 400 when request json does not match the expected model" in {
      running(fakeApplication()) {
        authorisedUser(Some("grs-create-journey-success"))

        val result = route(
          app,
          createLimitedCompanyJourneyRequest(Json.obj("continueUrl" -> "/testUrl"))
        ).get

        status(result) mustBe BAD_REQUEST
      }
    }

    "return 401 when authorisation throws" in {
      running(fakeApplication()) {
        when(
          mockAuthConnector.authorise(
            any(),
            any()
          )(any(), any())
        ).thenReturn(Future.failed(InsufficientEnrolments()))

        val result = route(app, createLimitedCompanyJourneyRequest()).get

        status(result) mustBe UNAUTHORIZED
      }
    }

    "return 500 when credentials cannot be retrieved from auth" in {
      running(fakeApplication()) {
        when(
          mockAuthConnector.authorise(
            any(),
            any()
          )(any(), any())
        ).thenReturn(Future.successful(None))

        val result = route(app, createLimitedCompanyJourneyRequest()).get

        status(result) mustBe INTERNAL_SERVER_ERROR
        contentAsString(result) mustBe "Internal ID could not be retrieved from Auth"
      }
    }
  }
}
