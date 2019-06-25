import play.core.PlayVersion
import play.sbt.PlayImport._
import sbt.Keys.libraryDependencies
import sbt._

object AppDependencies {

  val compile = Seq(
    "uk.gov.hmrc" %% "govuk-template" % "5.26.0-play-26",
    "uk.gov.hmrc" %% "play-ui" % "7.27.0-play-26",
    ws,
    "uk.gov.hmrc" %% "bootstrap-play-26" % "0.41.0",
    "org.typelevel" %% "cats-core" % "1.1.0",
    "com.github.pathikrit" %% "better-files" % "2.16.0"
  )

  def test(scope: String = "test") = Seq(
    "org.scalatest" %% "scalatest" % "3.0.4" % scope,
    "org.pegdown" % "pegdown" % "1.6.0" % scope,
    "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.2" % scope,
    "org.jsoup" % "jsoup" % "1.10.2" % scope,
    "org.mockito" % "mockito-core" % "2.23.0" % scope,
    "com.github.tomakehurst" % "wiremock-jre8" % "2.21.0" % scope,
    "com.typesafe.play" %% "play-test" % PlayVersion.current % scope,
    "com.typesafe.akka" %% "akka-stream-testkit" % "2.5.16" % Test,
     "org.scalamock" %% "scalamock-scalatest-support" % "3.6.0" % scope
  )

}
