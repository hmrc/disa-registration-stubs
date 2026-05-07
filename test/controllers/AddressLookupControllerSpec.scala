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

import org.scalatest.matchers.must.Matchers.mustBe
import play.api.libs.json.{JsObject, JsValue, Json}
import play.api.mvc.Result
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import utils.BaseUnitSpec

import scala.concurrent.Future

class AddressLookupControllerSpec extends BaseUnitSpec {

  private val addressLookupUrl = "/address-lookup/lookup"

  private def addressLookupRequest(json: JsValue) =
    FakeRequest(POST, addressLookupUrl)
      .withHeaders(
        CONTENT_TYPE -> "application/json"
      )
      .withJsonBody(json)

  private def addressLookupJson(result: Future[Result]): JsValue =
    contentAsJson(result)

  private def addressLookupArray(result: Future[Result]): Seq[JsValue] =
    addressLookupJson(result).as[Seq[JsValue]]

  private def lookupJson(postcode: String, filter: Option[String] = None): JsObject =
    Json.obj("postcode" -> postcode) ++
      filter.map(value => Json.obj("filter" -> value)).getOrElse(Json.obj())

  "AddressLookupController.lookup" should {

    "return 200 with an empty array when postcode maps to no results" in {
      running(fakeApplication()) {
        val result = route(app, addressLookupRequest(lookupJson("ZZ00 1ZZ"))).get

        status(result) mustBe OK
        addressLookupArray(result) mustBe Seq.empty
      }
    }

    "return 200 with a single address record when postcode maps to single result" in {
      running(fakeApplication()) {
        val result = route(app, addressLookupRequest(lookupJson("ZZ11 1ZZ"))).get

        status(result) mustBe OK

        val json = addressLookupArray(result)

        json.size mustBe 1
        (json.head \ "id").as[String] mustBe "990091234512"
        (json.head \ "uprn").as[Long] mustBe 990091234512L
        (json.head \ "address" \ "lines").as[Seq[String]] mustBe Seq("10 Test Street")
        (json.head \ "address" \ "town").as[String] mustBe "Test town"
        (json.head \ "address" \ "postcode").as[String] mustBe "ZZ11 1ZZ"
        (json.head \ "address" \ "country" \ "code").as[String] mustBe "GB"
        (json.head \ "address" \ "country" \ "name").as[String] mustBe "United Kingdom"
        (json.head \ "language").as[String] mustBe "en"
        (json.head \ "localCustodian" \ "code").as[Int] mustBe 121
        (json.head \ "localCustodian" \ "name").as[String] mustBe "NORTH SOMERSET"
      }
    }

    "return 200 with multiple address records when postcode maps to multiple results" in {
      running(fakeApplication()) {
        val result = route(app, addressLookupRequest(lookupJson("ZZ22 2ZZ"))).get

        status(result) mustBe OK

        val json = addressLookupArray(result)

        json.size mustBe 2
        (json.head \ "id").as[String] mustBe "990091234512"
        (json.head \ "address" \ "lines").as[Seq[String]] mustBe Seq("10 Test Street")
        (json.head \ "address" \ "postcode").as[String] mustBe "ZZ22 2ZZ"

        (json(1) \ "id").as[String] mustBe "990091234513"
        (json(1) \ "address" \ "lines").as[Seq[String]] mustBe Seq("11 Test Street")
        (json(1) \ "address" \ "postcode").as[String] mustBe "ZZ22 2ZZ"
      }
    }

    "return only matching address records when filter matches one address line" in {
      running(fakeApplication()) {
        val result = route(app, addressLookupRequest(lookupJson("ZZ22 2ZZ", Some("10")))).get

        status(result) mustBe OK

        val json = addressLookupArray(result)

        json.size mustBe 1
        (json.head \ "id").as[String] mustBe "990091234512"
        (json.head \ "address" \ "lines").as[Seq[String]] mustBe Seq("10 Test Street")
      }
    }

    "return all matching address records when filter matches multiple address lines" in {
      running(fakeApplication()) {
        val result = route(app, addressLookupRequest(lookupJson("ZZ22 2ZZ", Some("Test Street")))).get

        status(result) mustBe OK

        val json = addressLookupArray(result)

        json.size mustBe 2
        (json.head \ "address" \ "lines").as[Seq[String]] mustBe Seq("10 Test Street")
        (json(1) \ "address" \ "lines").as[Seq[String]] mustBe Seq("11 Test Street")
      }
    }

    "return an empty array when filter does not match any address lines" in {
      running(fakeApplication()) {
        val result = route(app, addressLookupRequest(lookupJson("ZZ22 2ZZ", Some("No Match")))).get

        status(result) mustBe OK
        addressLookupArray(result) mustBe Seq.empty
      }
    }

    "default to an empty array for an unknown postcode" in {
      running(fakeApplication()) {
        val result = route(app, addressLookupRequest(lookupJson("AA1 1ZZ"))).get

        status(result) mustBe OK
        addressLookupArray(result) mustBe Seq.empty
      }
    }

    "return 400 when request json does not match the expected model" in {
      running(fakeApplication()) {
        val result = route(
          app,
          addressLookupRequest(Json.obj("filter" -> "10"))
        ).get

        status(result) mustBe BAD_REQUEST
      }
    }
  }
}
