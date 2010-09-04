package scalaj.spring

import reflect.Manifest
import org.springframework.core.convert.converter.GenericConverter.ConvertiblePair
import TypeWrangler._
import org.springframework.core.convert.TypeDescriptor
import java.lang.reflect.{ Type => JType }

class Conversion[-From : Manifest, +To : Manifest](func : From => To) {
  private[this] val evFrom = manifest[From]
  private[this] val evTo = manifest[To]

  def validFor(src: Manifest[_], target: Manifest[_]) = src <:< evFrom && target <:< evTo

  def apply(src : Object) : Object = func(src.asInstanceOf[From]).asInstanceOf[Object]

  def toConvertiblePair : ConvertiblePair = evFrom match {
    case Manifest.Null => new ConvertiblePair(classOf[Object], evTo.erasure)
    case _ =>  new ConvertiblePair(evFrom.erasure, evTo.erasure)
  }
}

/**
 * convert from null to some more specific type.  Typically an empty collection.
 */
object NullConversion {
  def apply[T : Manifest](generator : => T) = new Conversion((n:Null) => generator _)
}

object Conversion {
  def apply[F : Manifest, T : Manifest](func : Function1[F,T]) = new Conversion(func)
  implicit def funcToConversion[F : Manifest, T : Manifest](func : Function1[F,T]) = apply(func)
  
}
