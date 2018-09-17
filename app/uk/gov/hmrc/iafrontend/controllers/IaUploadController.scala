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

package uk.gov.hmrc.iafrontend.controllers


import javax.inject.{Inject, Singleton}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc._
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.iafrontend.config.AppConfig
import uk.gov.hmrc.iafrontend.streams.CSVStreamer
import uk.gov.hmrc.iafrontend.views
import uk.gov.hmrc.play.bootstrap.controller.FrontendController

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class IaUploadController @Inject()(stream: CSVStreamer, val messagesApi: MessagesApi, implicit val appConfig: AppConfig) extends FrontendController with I18nSupport {

  //we need this for the stream bodyParser
  implicit  val Hc = HeaderCarrier()

  def getUploadPage() = Action.async { implicit request =>
    Future.successful(Ok(views.html.upload()))
  }

  def submitUploadPage() = Action.async(stream.bodyParser) { implicit request =>
    stream.upload(request.body).map(noOfRecords => Ok(noOfRecords.toString))
  }
}
