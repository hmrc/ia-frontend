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

package uk.gov.hmrc.iafrontend.config

import javax.inject.{Inject, Singleton}
import play.api.{Configuration, Environment}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

@Singleton
class AppConfig @Inject()(servicesConfig: ServicesConfig,  config: Configuration,environment: Environment)  {

  val runTimeConfig =config
  val runModeEnvironment = environment
  //todo perhaps move most of this to the config class
  final lazy val defaultOriginStride: String = config.get[String]("sosOrigin") match {
      case sosOrigin if !sosOrigin.isEmpty => sosOrigin
      case _ => config.get[String]("appName") match {
        case appName if !appName.isEmpty => appName
        case _ => "undefined"
      }
    }

  val strideRoles = {
    val maybeStrideRoles = config.get[Seq[String]]("stride.roles")
    if (maybeStrideRoles.length == 0)throw new RuntimeException("there are no stride roles in your config!")
    else maybeStrideRoles
  }


  private def loadConfig(key: String) = servicesConfig.getString(key)
  private val contactHost = servicesConfig.getString("contact-frontend.host")
  private val contactFormServiceIdentifier = "MyService"

  lazy val assetsPrefix = loadConfig(s"assets.url") + loadConfig(s"assets.version")
  lazy val analyticsToken = loadConfig(s"google-analytics.token")
  lazy val analyticsHost = loadConfig(s"google-analytics.host")

  lazy val reportAProblemPartialUrl = s"$contactHost/contact/problem_reports_ajax?service=$contactFormServiceIdentifier"
  lazy val reportAProblemNonJSUrl = s"$contactHost/contact/problem_reports_nonjs?service=$contactFormServiceIdentifier"
}