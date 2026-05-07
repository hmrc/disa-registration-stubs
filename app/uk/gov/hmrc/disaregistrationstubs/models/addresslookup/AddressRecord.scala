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

import play.api.libs.json.{Json, OFormat}

case class AddressRecord(
  id: String,
  uprn: Option[Long],
  parentUprn: Option[Long],
  usrn: Option[Long],
  organisation: Option[String],
  address: Address,
  language: String,
  localCustodian: Option[LocalCustodian],
  location: Option[Seq[BigDecimal]],
  blpuState: Option[String],
  logicalState: Option[String],
  streetClassification: Option[String],
  administrativeArea: Option[String] = None,
  poBox: Option[String] = None
) {
  require(location.isEmpty || location.exists(_.size == 2), location)
}

object AddressRecord {
  implicit val format: OFormat[AddressRecord] = Json.format[AddressRecord]
}

case class Address(
  lines: List[String],
  town: String,
  postcode: String,
  subdivision: Option[String],
  country: Country
)

object Address {
  implicit val format: OFormat[Address] = Json.format[Address]
}

case class Country(
  code: String,
  name: String
)

object Country {
  implicit val format: OFormat[Country] = Json.format[Country]
}

case class LocalCustodian(
  code: Int,
  name: String
)

object LocalCustodian {
  implicit val format: OFormat[LocalCustodian] = Json.format[LocalCustodian]
}
