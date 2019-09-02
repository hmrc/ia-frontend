/*
 * Copyright 2019 HM Revenue & Customs
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
import java.nio.file.{Path, Paths}

import javax.inject.{Inject, Singleton}
import play.api.i18n.I18nSupport
import play.api.libs.Files.TemporaryFile
import play.api.mvc.{MessagesControllerComponents, MultipartFormData, Request}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.iafrontend.auth.StrideAuthenticatedAction
import uk.gov.hmrc.iafrontend.config.AppConfig
import uk.gov.hmrc.iafrontend.connector.IaConnector
import uk.gov.hmrc.iafrontend.lock.LockService
import uk.gov.hmrc.iafrontend.streams.CSVStreamer
import uk.gov.hmrc.iafrontend.views
import uk.gov.hmrc.play.bootstrap.controller.FrontendController

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class IaUploadController @Inject() (stream:      CSVStreamer,
                                    iaConnector: IaConnector,
                                    lockService: LockService,
                                    strideAuth:  StrideAuthenticatedAction,
                                    mcc:         MessagesControllerComponents,
                                    uploadCheck: views.html.upload_check,
                                    upload:      views.html.upload
)(implicit ec: ExecutionContext, appConfig: AppConfig) extends FrontendController(mcc) with I18nSupport {

  //we need this for the stream bodyParser
  implicit val Hc: HeaderCarrier = HeaderCarrier()

  def getUploadPage() = strideAuth.async { implicit request =>
    Future.successful(Ok(upload()))
  }

  def submitUploadPage() = strideAuth.async(parse.multipartFormData) { implicit request =>
    doUpload()(request)
  }

  def doUpload()(implicit request: Request[MultipartFormData[TemporaryFile]]) = {
    request.body.file("file").map { zippedFile =>
      val filename = Paths.get(zippedFile.filename).getFileName
      zippedFile.ref.moveTo(new File(s"$filename"), replace = true)
      checkLockAndStream(filename).map(_ => Redirect(routes.IaUploadController.getUploadCheck()))
    }.getOrElse(
      Future.successful(Ok("Upload failed, please try again"))
    )
  }

  private def checkLockAndStream(filename: Path) = {
    lockService.acquire(Hc).map { _ => stream.processFile(filename) }
  }

  def getUploadCheck() = strideAuth.async { implicit request =>
    iaConnector.count().map(res => Ok(uploadCheck(res)))
  }
}
