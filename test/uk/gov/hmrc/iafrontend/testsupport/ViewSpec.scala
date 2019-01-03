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

package uk.gov.hmrc.iafrontend.testsupport

import org.scalatest.FreeSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.i18n.{Messages, MessagesApi}
import play.api.inject.Injector
import play.api.test.FakeRequest
import play.filters.csrf.CSRF.Token
import play.filters.csrf.{CSRFConfigProvider, CSRFFilter}
import uk.gov.hmrc.iafrontend.config.AppConfig
trait ViewSpec
  extends Spec
    with GuiceOneAppPerSuite
{

  def injector: Injector = app.injector
  val appConfig = injector.instanceOf[AppConfig]
  def messagesApi: MessagesApi = injector.instanceOf[MessagesApi]
  def fakeRequest: FakeRequest[_] = addToken(FakeRequest("", ""))
  def messages: Messages = messagesApi.preferred(fakeRequest)

  private def addToken[T](fakeRequest: FakeRequest[T])(implicit app: Application) = {
    val csrfConfig = app.injector.instanceOf[CSRFConfigProvider].get
    val csrfFilter = app.injector.instanceOf[CSRFFilter]
    val token = csrfFilter.tokenProvider.generateToken

    fakeRequest.copyFakeRequest(tags = fakeRequest.tags ++ Map(
      Token.NameRequestTag -> csrfConfig.tokenName,
      Token.RequestTag -> token)).withHeaders((csrfConfig.headerName, token))
  }

}
