package scalaj.spring

import reflect.Manifest
import org.springframework.core.convert.converter.GenericConverter.ConvertiblePair

class Conversion[-From : Manifest, +To : Manifest](func : From => To) {
  private[this] val evFrom = manifest[From]
  private[this] val evTo = manifest[To]

  def validFor(src: Manifest[_], target: Manifest[_]) = src <:< evFrom && evTo <:< target

  def apply(src : Object)(implicit conversions : Conversions) : Object =
    func(src.asInstanceOf[From]).asInstanceOf[Object]

  def toConvertiblePair : ConvertiblePair = evFrom match {
    case Manifest.Null | Manifest.Unit => new ConvertiblePair(classOf[Object], evTo.erasure)
    case _ =>  new ConvertiblePair(evFrom.erasure, evTo.erasure)
  }
}


object Conversion {
  def apply[F : Manifest, T : Manifest](func : Function1[F,T]) = new Conversion(func)
  implicit def funcToConversion[F : Manifest, T : Manifest](func : Function1[F,T]) = apply(func)
  
}
