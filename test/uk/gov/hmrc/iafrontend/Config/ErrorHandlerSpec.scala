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

package uk.gov.hmrc.iafrontend.Config

import play.twirl.api.Html
import uk.gov.hmrc.iafrontend.config.ErrorHandler
import uk.gov.hmrc.iafrontend.testsupport.ITSpec
import uk.gov.hmrc.iafrontend.views

class ErrorHandlerSpec extends ITSpec {

  val errorHandler = injector.instanceOf[ErrorHandler]
  val error_view = injector.instanceOf[views.html.error_template]
  "ErrorHandler shoud Return valid html" in {
    val html: Html = errorHandler.standardErrorTemplate("page title", "heading", "message")(fakeRequest)
    html.contentType shouldBe "text/html"
    val error = error_view("page title", "heading", "message")(fakeRequest, messages, appConfig)
    error shouldBe (html)
  }

}
