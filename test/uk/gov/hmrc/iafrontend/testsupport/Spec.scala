package uk.gov.hmrc.iafrontend.testsupport

import org.scalatest._
import org.scalatest.concurrent.{Eventually, IntegrationPatience, ScalaFutures}
import org.scalatest.mockito.MockitoSugar
import uk.gov.hmrc.play.test.UnitSpec

trait Spec extends Matchers
  with DiagrammedAssertions
  with TryValues
  with EitherValues
  with OptionValues
  with AppendedClues
  with ScalaFutures
  with StreamlinedXml
  with Inside
  with Eventually
  with MockitoSugar
  with UnitSpec
  with IntegrationPatience{
  implicit lazy val ec = scala.concurrent.ExecutionContext.Implicits.global
}
