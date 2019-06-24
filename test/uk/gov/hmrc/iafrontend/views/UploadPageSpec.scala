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

package uk.gov.hmrc.iafrontend.views

import uk.gov.hmrc.iafrontend.testsupport.{PageElements, ViewSpec}


class UploadPageSpec extends ViewSpec {


   "UploadPage" should {
     "display the correct elements " in  new PageElements{
       val upload = new uk.gov.hmrc.iafrontend.views.html.upload(
         mainTemplate = ???,
         form = ???
       )
       val html = upload()(fakeRequest, messages, appConfig)

       headerTitle shouldBe "ia-frontend" withClue "the correct banner title"
     }
   }
}
