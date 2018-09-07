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

import sbt.Keys._
import sbt.Tests.{Group, SubProcess}
import sbt._
import play.routes.compiler.StaticRoutesGenerator
import play.sbt.PlayImport.PlayKeys
import play.sbt.routes.RoutesKeys.routesImport
import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin._


trait MicroService {

  import uk.gov.hmrc._
  import DefaultBuildSettings.{scalaSettings, defaultSettings, targetJvm, addTestReportOption}
  import uk.gov.hmrc.ShellPrompt
  import play.sbt.routes.RoutesKeys.routesGenerator

  val appName: String
  val appVersion: String

  lazy val appDependencies : Seq[ModuleID] = ???
  lazy val plugins : Seq[Plugins] = Seq(play.sbt.PlayScala)
  lazy val playSettings : Seq[Setting[_]] = Seq.empty
  val appScalaVersion = "2.11.11"

  lazy val microservice = Project(appName, file("."))
    .enablePlugins(plugins : _*)
    .settings(playSettings : _*)
    .settings(version := appVersion)
    .settings(scalaSettings: _*)
    .settings(publishingSettings: _*)
    .settings(defaultSettings(): _*)
    .settings(
      targetJvm := "jvm-1.8",
      shellPrompt := ShellPrompt(appVersion),
      libraryDependencies ++= appDependencies,
      parallelExecution in Test := false,
      scalaVersion := appScalaVersion,
      fork in Test := false,
      retrieveManaged := false,
      routesGenerator := StaticRoutesGenerator,
      scalacOptions ++= Seq(
        "-Xfatal-warnings",
        "-Xlint:-missing-interpolator,_",
        "-Yno-adapted-args",
        "-Ywarn-value-discard",
        "-Ywarn-dead-code",
        "-deprecation",
        "-feature",
        "-unchecked",
        "-language:implicitConversions",
        "-Ypartial-unification" //required by cats
      ))
    .settings(PlayKeys.playDefaultPort := 8050)
    .configs(IntegrationTest)
    .settings(inConfig(IntegrationTest)(Defaults.itSettings): _*)
    .settings(
      Keys.fork in IntegrationTest := false,
      unmanagedSourceDirectories in IntegrationTest := (baseDirectory in IntegrationTest)(base => Seq(base / "it")).value,
      addTestReportOption(IntegrationTest, "int-test-reports"),
      testGrouping in IntegrationTest := TestPhases.oneForkedJvmPerTest((definedTests in IntegrationTest).value),
      parallelExecution in IntegrationTest := false)
    .settings(SbtBuildInfo(): _*)
    .disablePlugins(sbt.plugins.JUnitXmlReportPlugin)
    .settings(resolvers ++= Seq(
      Resolver.bintrayRepo("hmrc", "releases"),
      Resolver.jcenterRepo
    ))
}

private object TestPhases {

  def oneForkedJvmPerTest(tests: Seq[TestDefinition]) =
    tests map {
      test => new Group(test.name, Seq(test), SubProcess(ForkOptions(runJVMOptions = Seq("-Dtest.name=" + test.name))))
    }
}

