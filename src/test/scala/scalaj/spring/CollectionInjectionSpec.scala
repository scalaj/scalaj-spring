package scalaj.spring

import org.scalatest.matchers.ShouldMatchers
import scala.collection.mutable.Stack
import org.springframework.context.support.ClassPathXmlApplicationContext
import org.scalatest.{FeatureSpec, FunSuite, Spec}

class CollectionInjectionSpec extends FeatureSpec {

  feature("injecting collections") {
    scenario("constructor-injection with a List<String>") {
      val context = new ClassPathXmlApplicationContext("testContext.xml")
      val seqHolder = context.getBean(classOf[StringSeqHolder])
      assert(seqHolder.seq === Seq("1","2","3"))
    }
  }

}