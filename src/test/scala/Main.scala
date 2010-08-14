import scalaj.spring.SignatureWrangler._
import scalaj.spring.{TypeWrangler, IntSeqHolder}
import tools.scalap.scalax.rules.scalasig._
import tools.scalap.scalax.util.StringUtil

object Main {

  def main(args:Array[String]) = {
    val printer = new ScalaSigPrinter(System.out, false)

    val spsig = getScalaSig(java.lang.Class.forName("scala.package"))
    val pkgobj = spsig map {_.topLevelObjects.head}
    println(pkgobj.get)
    val pkgclass = pkgobj map (companionOf)

    val tpe = pkgclass map (_.infoType)
    println(tpe.get)

    val members = pkgclass map {_.children} getOrElse {Nil}
    members collect {case alias @ AliasSymbol(info) => println(alias.name + " -> " + info.owner)}

    val constructorSymbols = constructorsOf(classOf[IntSeqHolder])

    for(ctor <- constructorSymbols) {
      print("constructor: ")
      printer.printSymbol(ctor)

      val ctorType = ctor.infoType
      print("constructor type: ")
      println(ctorType)
      print("(pretty): ")
      printer.printMethodType(ctorType,false)({println})


      val paramSymbols = ctorType match {
        case MethodType(resultType, paramSymbols) => paramSymbols
        case _ => Nil
      }

      println("Params:")
      paramSymbols foreach {println}

      val paramTypes = paramSymbols map { _.asInstanceOf[MethodSymbol].infoType }
      println("Param Types:")
      paramTypes foreach {
        _ match {
          case trt @ TypeRefType(prefix, symbol, typeArgs) =>
            println(trt)
            println(symbolFullName(symbol))
            println(typeToManifest(trt))
            //typeArgs foreach {println}
        }
      }



    }

  }
}