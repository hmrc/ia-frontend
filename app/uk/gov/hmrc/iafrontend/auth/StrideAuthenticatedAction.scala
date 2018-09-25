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

package uk.gov.hmrc.iafrontend.auth

import cats.instances.list._
import cats.instances.option._
import cats.syntax.traverse._
import com.google.inject.Inject
import play.api.{Configuration, Environment, Logger}
import play.api.mvc.Results._
import play.api.mvc._
import uk.gov.hmrc.auth.core.AuthProvider.PrivilegedApplication
import uk.gov.hmrc.auth.core.retrieve.Retrievals
import uk.gov.hmrc.auth.core.{AuthProviders, _}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.iafrontend.config.AppConfig
import uk.gov.hmrc.play.HeaderCarrierConverter
import uk.gov.hmrc.play.bootstrap.config.AuthRedirects

import scala.concurrent.{ExecutionContext, Future}

class StrideAuthenticatedAction @Inject()(
                                           af: AuthorisedFunctions,
                                           appConfig: AppConfig)(implicit ec: ExecutionContext) extends ActionBuilder[Request] with AuthRedirects{

  override def config: Configuration = appConfig.runTimeConfig

  override def env: Environment = appConfig.runModeEnvironment

  override def invokeBlock[A](request: Request[A], block:  Request[A]  => Future[Result]): Future[Result] = {
    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromHeadersAndSession(request.headers, Some(request.session))
    implicit val r: Request[A] = request

    af.authorised(AuthProviders(PrivilegedApplication)).retrieve(Retrievals.allEnrolments ) { enrolments =>
      necessaryRoles(enrolments).fold[Future[Result]](Future.successful(Unauthorized("Insufficient roles"))){ _ => block(request) }
    }.recover {
      case _: NoActiveSession =>
        toStrideLogin(request.uri)
      case e: AuthorisationException =>
        Logger.debug(s"Unauthorised because of ${e.reason}, $e")
        Unauthorized("")
    }
  }


  private def necessaryRoles(enrolments: Enrolments):Option[List[Enrolment]] =
    appConfig.strideRoles.toList.map(enrolments.getEnrolment).traverse[Option, Enrolment](identity)

}
