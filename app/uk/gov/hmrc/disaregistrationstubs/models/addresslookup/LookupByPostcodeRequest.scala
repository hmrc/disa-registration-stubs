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

package uk.gov.hmrc.disaregistrationstubs.models.addresslookup

import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.json.*

case class LookupByPostcodeRequest(postcode: Postcode, filter: Option[String] = None)

object LookupByPostcodeRequest {
  implicit val postcodeReads: Reads[Postcode] = Reads[Postcode] { json =>
    json.validate[String] match {
      case e: JsError           => e
      case s: JsSuccess[String] =>
        Postcode.cleanupPostcode(s.get) match {
          case pc if pc.isDefined => JsSuccess(pc.get)
          case _                  => JsError("error.invalid")
        }
    }
  }

  implicit val postcodeWrites: Writes[Postcode] = new Writes[Postcode] {
    override def writes(o: Postcode): JsValue =
      JsString(o.toString)
  }

  implicit val reads: Reads[LookupByPostcodeRequest] = (
    (JsPath \ "postcode").read[Postcode] and
      (JsPath \ "filter")
        .readNullable[String]
        .map(fo => fo.flatMap(f => if (f.trim.isEmpty) None else Some(f)))
  )((pc, fo) => LookupByPostcodeRequest.apply(pc, fo))

  implicit val writes: Writes[LookupByPostcodeRequest] =
    Json.writes[LookupByPostcodeRequest]
}
