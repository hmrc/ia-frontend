import play.core.PlayVersion
import play.sbt.PlayImport._
import sbt.Keys.libraryDependencies
import sbt._

object AppDependencies {

  val compile = Seq(
    "uk.gov.hmrc" %% "govuk-template" % "5.22.0",
    "uk.gov.hmrc" %% "play-ui" % "7.22.0",
    ws,
    "uk.gov.hmrc" %% "bootstrap-play-25" % "3.2.0",
    "org.typelevel" %% "cats-core" % "1.1.0",
    "com.lightbend.akka" %% "akka-stream-alpakka-csv" % "0.8",
    "uk.gov.hmrc" %% "domain" % "5.2.0"
  )

  def test(scope: String = "test") = Seq(
    "uk.gov.hmrc" %% "hmrctest" % "3.0.0" % scope,
    "org.scalatest" %% "scalatest" % "3.0.4" % scope,
    "org.pegdown" % "pegdown" % "1.6.0" % scope,
    "org.scalatestplus.play" %% "scalatestplus-play" % "2.0.1" % scope,
    "org.jsoup" % "jsoup" % "1.10.2" % scope,
    "org.mockito" % "mockito-core" % "2.11.0" % scope,
    "com.typesafe.play" %% "play-test" % PlayVersion.current % scope,
     "org.scalamock" %% "scalamock-scalatest-support" % "3.6.0" % scope
  )

}
