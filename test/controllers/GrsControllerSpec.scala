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
import play.api.libs.json.JsValue
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import uk.gov.hmrc.auth.core.*
import utils.BaseUnitSpec

import scala.concurrent.Future

class GrsControllerSpec extends BaseUnitSpec {

  private def request(journeyId: String) =
    FakeRequest(GET, s"/journey/$journeyId")

  private def json(result: Future[play.api.mvc.Result]): JsValue =
    contentAsJson(result)

  "GrsController.retrieveJourneyData" should {

    "return 200 with success payload" in {
      running(fakeApplication()) {
        authorisedUser()

        val result = route(app, request("success")).get

        status(result) mustBe OK
        (json(result) \ "identifiersMatch").as[Boolean] mustBe true
        (json(result) \ "registration" \ "registrationStatus").as[String] mustBe "REGISTERED"
        (json(result) \ "businessVerification" \ "verificationStatus").as[String] mustBe "PASS"
        (json(result) \ "ctutr").as[String] mustBe "1234567890"
      }
    }

    "return identifiers mismatch scenario" in {
      running(fakeApplication()) {
        authorisedUser()

        val result = route(app, request("identifiers-fail")).get

        status(result) mustBe OK
        (json(result) \ "identifiersMatch").as[Boolean] mustBe false
        (json(result) \ "registration" \ "registrationStatus").as[String] mustBe "REGISTRATION_FAILED"
      }
    }

    "return BV fail scenario" in {
      running(fakeApplication()) {
        authorisedUser()

        val result = route(app, request("bv-fail")).get

        status(result) mustBe OK
        (json(result) \ "businessVerification" \ "verificationStatus").as[String] mustBe "FAIL"
      }
    }

    "return BV not called (UNCHALLENGED)" in {
      running(fakeApplication()) {
        authorisedUser()

        val result = route(app, request("bv-not-called")).get

        status(result) mustBe OK
        (json(result) \ "businessVerification" \ "verificationStatus").as[String] mustBe "UNCHALLENGED"
      }
    }

    "return CT enrolled scenario" in {
      running(fakeApplication()) {
        authorisedUser()

        val result = route(app, request("bv-ct-enrolled")).get

        status(result) mustBe OK
        (json(result) \ "businessVerification" \ "verificationStatus").as[String] mustBe "CT_ENROLLED"
      }
    }

    "return registration failed scenario" in {
      running(fakeApplication()) {
        authorisedUser()

        val result = route(app, request("registration-failed")).get

        status(result) mustBe OK
        (json(result) \ "registration" \ "registrationStatus").as[String] mustBe "REGISTRATION_FAILED"
      }
    }

    "return registration not called scenario" in {
      running(fakeApplication()) {
        authorisedUser()

        val result = route(app, request("registration-not-called")).get

        status(result) mustBe OK
        (json(result) \ "registration" \ "registrationStatus").as[String] mustBe "REGISTRATION_NOT_CALLED"
      }
    }

    "return response without ctutr when ct-utr-absent" in {
      running(fakeApplication()) {
        authorisedUser()

        val result = route(app, request("ct-utr-absent")).get

        status(result) mustBe OK
        (json(result) \ "ctutr").toOption mustBe None
      }
    }

    "return 404 when journey not found" in {
      running(fakeApplication()) {
        authorisedUser()

        val result = route(app, request("grs-data-not-found")).get

        status(result) mustBe NOT_FOUND
      }
    }

    "default to success-like response for unknown journeyId" in {
      running(fakeApplication()) {
        authorisedUser()

        val result = route(app, request("something-random")).get

        status(result) mustBe OK
        (json(result) \ "registration" \ "registrationStatus").as[String] mustBe "REGISTERED"
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

        val result = route(app, request("success")).get

        status(result) mustBe UNAUTHORIZED
      }
    }
  }
}
