/*
 * Copyright 2018 HM Revenue & Customs
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

package uk.gov.hmrc.iafrontend.connector

import javax.inject.Inject
import play.api.Logger
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.iafrontend.config.IaConfig
import uk.gov.hmrc.iafrontend.domain.GreenUtr
import uk.gov.hmrc.play.bootstrap.http.HttpClient

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


class IaConnector  @Inject() (
                               http: HttpClient,
                               ia: IaConfig) {
  val urlUpload = s"${ia.baseUrl}/ia/upload"
  val urlDrop = s"${ia.baseUrl}/ia/drop"

  def sendUtrs(batchUtrs: List[GreenUtr])(implicit hc: HeaderCarrier): Future[Int] = {
    http.POST(urlUpload, batchUtrs).map(result => result.status match{
      case 200 => result.body.toInt
      case _ => throw new Exception(s"batch update error for url ${urlUpload}  utrs list ${batchUtrs} bad end retuned ${result}")
    })
  }

  def drop()(implicit hc: HeaderCarrier): Future[Unit] = {
    http.POSTEmpty(urlDrop).map(_ => ())
  }
}

