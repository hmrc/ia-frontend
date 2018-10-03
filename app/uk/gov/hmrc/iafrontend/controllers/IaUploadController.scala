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




import java.io.File
import java.nio.file.Paths

import javax.inject.{Inject, Singleton}
import play.api.i18n.{I18nSupport, MessagesApi}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.iafrontend.auth.StrideAuthenticatedAction
import uk.gov.hmrc.iafrontend.config.AppConfig
import uk.gov.hmrc.iafrontend.connector.IaConnector
import uk.gov.hmrc.iafrontend.streams.CSVStreamer
import uk.gov.hmrc.iafrontend.views
import uk.gov.hmrc.play.bootstrap.controller.FrontendController

import scala.concurrent.Future

@Singleton
class IaUploadController @Inject()(stream: CSVStreamer,
                                   iaConnector: IaConnector,
                                   strideAuth: StrideAuthenticatedAction,
                                   val messagesApi: MessagesApi,
                                   implicit val appConfig: AppConfig) extends FrontendController with I18nSupport {

  //we need this for the stream bodyParser
  implicit val Hc = HeaderCarrier()

  def getUploadPage() = strideAuth.async { implicit request =>
    Future.successful(Ok(views.html.upload()))
  }

  def submitUploadPage() = strideAuth.async(parse.multipartFormData) { implicit request =>
    request.body.file("file").map { ZippedFile =>
      val filename = Paths.get(ZippedFile.filename).getFileName
      ZippedFile.ref.moveTo(new File(s"/$filename"), replace = true)

      stream.processFile(filename)
      Future.successful(Redirect(routes.IaUploadController.getUploadCheck()))
    }.getOrElse(
      Future.successful(Ok("Upload failed please try again"))
    )
  }

  def getUploadCheck() = strideAuth.async { implicit request =>
    iaConnector.count().map(res => Ok(views.html.upload_check(res)))
  }
}
