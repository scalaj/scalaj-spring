package scalaj.spring

import org.springframework.core.convert.TypeDescriptor
import org.springframework.core.MethodParameter
import org.scalatest.{FunSuite, FeatureSpec}
//give the wrangler a short name, don't import methods as we want full control without using them as implicits
import scalaj.spring.{TypeWrangler => wrangler}


class TypeWranglerSpec extends FunSuite  {

  test("dynamic lookup of a Seq[String] manifest") {
    val typeDesc = new TypeDescriptor(wrangler.primaryCtorMethodParam(classOf[StringSeqHolder]))
    val actual = wrangler.manifestFromTypeDescriptor(typeDesc)
    val expected = manifest[Seq[String]]

    assert(actual === expected)
  }

  test("dynamic lookup of a Seq[Int] manifest") {
    val typeDesc = new TypeDescriptor(wrangler.primaryCtorMethodParam(classOf[IntSeqHolder]))
    val actual = wrangler.manifestFromTypeDescriptor(typeDesc)
    val expected = manifest[Seq[Int]]

    assert(actual === expected)
  }


}