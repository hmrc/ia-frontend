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
import play.api.{Configuration, Environment}
import play.api.i18n.{I18nSupport, MessagesApi}
import uk.gov.hmrc.auth.core.AuthorisedFunctions
import uk.gov.hmrc.iafrontend.config.AppConfig
import uk.gov.hmrc.iafrontend.connectors.{AuthConnector, UserDetailsConnector}
import uk.gov.hmrc.play.bootstrap.config.AuthRedirects
import uk.gov.hmrc.play.bootstrap.controller.FrontendController

@Singleton
class FileUploadController @Inject()(
                                      val messagesApi: MessagesApi,
                                      val authConnector: AuthConnector,
                                      userDetailsConnector: UserDetailsConnector,
                                     override val config: Configuration,
                                      override val env: Environment,
                                      implicit val appConfig: AppConfig)
  extends FrontendController
    with I18nSupport
    with AuthorisedFunctions
    with AuthRedirects{


}
