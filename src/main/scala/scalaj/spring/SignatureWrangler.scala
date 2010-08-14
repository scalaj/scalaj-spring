package scalaj.spring

import tools.scalap.scalax.rules.scalasig.ClassFileParser.{parse, ConstValueIndex, Annotation}
import tools.scalap.scalax.rules.scalasig._
import scala.reflect.generic.ByteCodecs
import java.io.{PrintStream, ByteArrayOutputStream}
import tools.scalap.scalax.util.StringUtil
import reflect.ClassManifest

object SignatureWrangler {
  val SCALA_SIG = "ScalaSig"
  val SCALA_SIG_ANNOTATION = "Lscala/reflect/ScalaSignature;"
  val BYTES_VALUE = "bytes"

  def getClassFile(clazz : Class[_]) = ClassFileParser.parse(ByteCode.forClass(clazz))


  def getScalaSig(clazz : Class[_]) : Option[ScalaSig] = {
    val classFile = getClassFile(clazz)
    classFile.attribute(SCALA_SIG).map(_.byteCode).map(ScalaSigAttributeParsers.parse) match {
      // No entries in ScalaSig attribute implies that the signature is stored in the annotation
      case Some(ScalaSig(_, _, entries)) if entries.length == 0 => unpickleFromAnnotation(classFile)
      case Some(scalaSig) => Some(scalaSig)
      case _ => None
    }
  }

  def unpickleFromAnnotation(classFile: ClassFile): Option[ScalaSig] = {
    import classFile._
    classFile.annotation(SCALA_SIG_ANNOTATION) match {
      case Some(Annotation(_, elements)) =>
        val bytesElem = elements.find(elem => constant(elem.elementNameIndex) == BYTES_VALUE).get
        val bytes = ((bytesElem.elementValue match {case ConstValueIndex(index) => constantWrapped(index)})
                .asInstanceOf[StringBytesPair].bytes)
        val length = ByteCodecs.decode(bytes)
        Some(ScalaSigAttributeParsers.parse(ByteCode(bytes.take(length))))
      case _ => None
    }
  }

  def constructorsFromSig(sig : ScalaSig) = {
    val sym = sig.topLevelClasses.head
    sym.children.filter(s => s.name == "<init>").map{_.asInstanceOf[MethodSymbol]}
  }

  def constructorsOf(clazz:Class[_]) = getScalaSig(clazz) map { constructorsFromSig } getOrElse(Nil) 

  def parseScalaSignature(scalaSig: ScalaSig) = {
    val baos = new ByteArrayOutputStream
    val stream = new PrintStream(baos)
    val syms = scalaSig.topLevelClasses ::: scalaSig.topLevelObjects
    syms.head.parent match {
    //Partial match
      case Some(p) if (p.name != "<empty>") => {
        val path = p.path
        val i = path.lastIndexOf(".")
        if (i > 0) {
          stream.print("package ");
          stream.print(path.substring(0, i))
          stream.print("\n")
        }
      }
      case _ =>
    }
    // Print classes
    val printPrivates = true
    val printer = new ScalaSigPrinter(stream, printPrivates)
    for (c <- syms) {
      printer.printSymbol(c)
    }
    baos.toString
  }

  def symbolFullName(sym:Symbol) = StringUtil.cutSubstring(sym.path)(".package")

  def typeToManifest(tpe:TypeRefType) = {
    val prefix : Type = tpe.prefix
    val symbol : Symbol = tpe.symbol
    val typeArgs : Seq[Type] = tpe.typeArgs

    val clazz = java.lang.Class.forName(symbol.path)
    //ClassManifest.classType(clazz)
  }

  def companionOf(sym:Symbol) = sym match {
    case os @ ObjectSymbol(_) => os.infoType.asInstanceOf[TypeRefType].symbol.asInstanceOf[ClassSymbol]
    case cs @ ClassSymbol(_,_) => cs.infoType.asInstanceOf[ClassInfoType].symbol.asInstanceOf[ObjectSymbol]
    case _ => error("no companion")
  }
}