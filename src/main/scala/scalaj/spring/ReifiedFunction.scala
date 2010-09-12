package scalaj.spring

import reflect.Manifest
import org.springframework.core.convert.converter.GenericConverter.ConvertiblePair

class ReifiedFunction[From : Manifest, To : Manifest](func : From => To) {
  val evFrom = manifest[From]
  val evTo = manifest[To]

  def validFor(src: Manifest[_], target: Manifest[_]) = {
    val result = src <:< evFrom && evTo <:< target
    if (src <:< evFrom)
      println("validFor(%s => %s, %s => %s = %s)".format(src, evFrom, evTo, target, evTo <:< target))
    result
  }

  def apply(src : Object) : Object =
    func(src.asInstanceOf[From]).asInstanceOf[Object]

  def toConvertiblePair : ConvertiblePair = evFrom match {
    case Manifest.Null | Manifest.Unit => new ConvertiblePair(classOf[Object], evTo.erasure)
    case _ =>  new ConvertiblePair(evFrom.erasure, evTo.erasure)
  }
}


object ReifiedFunction {
  implicit def apply[F : Manifest, T : Manifest](func : Function1[F,T]) = new ReifiedFunction(func)
  implicit def reify[F : Manifest, T : Manifest](func : Function1[F,T]) = apply(func)
}
