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

import akka.util.ByteString
import org.scalatest.{BeforeAndAfterAll, FreeSpecLike}
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.inject.Injector
import play.api.inject.guice.GuiceApplicationBuilder
import uk.gov.hmrc.iafrontend.lock.{RichMatchers, WireMockSupport}

trait ServiceSpec
  extends FreeSpecLike
  with RichMatchers
   with WireMockSupport
    with BeforeAndAfterAll
    with GuiceOneAppPerSuite
     {

  override implicit lazy val app: Application = new GuiceApplicationBuilder()
    .configure("microservice.services.lock.port" -> WireMockSupport.port).build()

  def injector: Injector = app.injector


  implicit def toByteStringOps(s: String) = new {
    def bs: ByteString = ByteString(s)
  }

}
