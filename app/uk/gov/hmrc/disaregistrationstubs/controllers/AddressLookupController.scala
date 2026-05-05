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

import play.api.Logging
import play.api.libs.json.Json
import play.api.mvc.{Action, ControllerComponents}
import uk.gov.hmrc.disaregistrationstubs.models.addresslookup.*
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import javax.inject.{Inject, Singleton}

@Singleton
class AddressLookupController @Inject() (
  cc: ControllerComponents
) extends BackendController(cc)
    with Logging {

  def lookup(): Action[LookupByPostcodeRequest] = Action(parse.json[LookupByPostcodeRequest]) { implicit request =>
    val filter  = request.body.filter
    val records = request.body.postcode.toString match {
      case "ZZ00 1ZZ" =>
        logger.info("Returning stubbed address lookup response with no results")
        Seq.empty

      case "ZZ11 1ZZ" =>
        logger.info("Returning stubbed address lookup response with a single result")
        Seq(
          addressRecord(
            id = "990091234512",
            uprn = 990091234512L,
            addressLines = List("10 Test Street"),
            town = "Test town",
            postcode = "ZZ11 1ZZ"
          )
        )

      case "ZZ22 2ZZ" =>
        logger.info("Returning stubbed address lookup response with multiple results")
        Seq(
          addressRecord(
            id = "990091234512",
            uprn = 990091234512L,
            addressLines = List("10 Test Street"),
            town = "Test town",
            postcode = "ZZ22 2ZZ"
          ),
          addressRecord(
            id = "990091234513",
            uprn = 990091234513L,
            addressLines = List("11 Test Street"),
            town = "Test town",
            postcode = "ZZ22 2ZZ"
          )
        )

      case _ =>
        logger.info(s"Returning default empty address lookup response for postcode: ${request.body.postcode.toString}")
        Seq.empty
    }

    Ok(Json.toJson(applyFilter(records, filter)))
  }

  private def applyFilter(records: Seq[AddressRecord], maybeFilter: Option[String]): Seq[AddressRecord] =
    maybeFilter.fold(records) { filter =>
      val normalisedFilter = filter.toLowerCase

      records.filter { record =>
        record.address.lines.exists(_.toLowerCase.contains(normalisedFilter))
      }
    }

  private def addressRecord(
    id: String,
    uprn: Long,
    addressLines: List[String],
    town: String,
    postcode: String
  ): AddressRecord =
    AddressRecord(
      id = id,
      uprn = Some(uprn),
      parentUprn = None,
      usrn = None,
      organisation = None,
      address = Address(
        lines = addressLines,
        town = town,
        postcode = postcode,
        subdivision = Some("GB-ENG"),
        country = Country(
          code = "GB",
          name = "United Kingdom"
        )
      ),
      language = "en",
      localCustodian = Some(
        LocalCustodian(
          code = 121,
          name = "NORTH SOMERSET"
        )
      ),
      location = None,
      blpuState = None,
      logicalState = None,
      streetClassification = None,
      administrativeArea = None,
      poBox = None
    )
}
