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

package uk.gov.hmrc.iafrontend

import play.api.Mode.{Mode, Dev}
import play.api.i18n.{DefaultLangs, DefaultMessagesApi, I18nSupport}
import play.api.test.FakeRequest
import play.api.{Configuration, Environment}
import uk.gov.hmrc.iafrontend.config.AppConfig
import uk.gov.hmrc.play.config.ServicesConfig
trait TestHelper {
  val fakeRequestGet = FakeRequest("GET", "/")
  val fakeRequestPostForm = FakeRequest("POST", "/")

  val env: Environment = Environment.simple()
  val configuration: Configuration = Configuration.load(env)
  val i18nSupport = new I18nSupport {
    override def messagesApi = messageApi
  }
  val messageApi = new DefaultMessagesApi(env, configuration, new DefaultLangs(configuration))

  class testServiceConfig extends ServicesConfig{
    override protected def mode: Mode = Dev

    override protected def runModeConfiguration: Configuration = configuration
  }


  val appConfig = new AppConfig(new testServiceConfig(),configuration)
}
