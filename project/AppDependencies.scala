import play.core.PlayVersion
import play.sbt.PlayImport._
import sbt.Keys.libraryDependencies
import sbt._

object AppDependencies {

  val compile = Seq(
    "uk.gov.hmrc" %% "govuk-template" % "5.26.0-play-25",
    "uk.gov.hmrc" %% "play-ui" % "7.27.0-play-25",
    ws,
    "uk.gov.hmrc" %% "bootstrap-play-25" % "4.3.0",
    "org.typelevel" %% "cats-core" % "1.1.0",
    "com.github.pathikrit" %% "better-files" % "2.16.0"
  )

  def test(scope: String = "test") = Seq(
    "uk.gov.hmrc" %% "hmrctest" % "3.3.0" % scope,
    "org.scalatest" %% "scalatest" % "3.0.4" % scope,
    "org.pegdown" % "pegdown" % "1.6.0" % scope,
    "org.scalatestplus.play" %% "scalatestplus-play" % "2.0.1" % scope,
    "org.jsoup" % "jsoup" % "1.10.2" % scope,
    "org.mockito" % "mockito-core" % "2.11.0" % scope,
    "com.github.tomakehurst" % "wiremock" % "2.18.0" % scope,
    "com.typesafe.play" %% "play-test" % PlayVersion.current % scope,
    "com.typesafe.akka" %% "akka-stream-testkit" % "2.5.16" % Test,
     "org.scalamock" %% "scalamock-scalatest-support" % "3.6.0" % scope
  )

}
