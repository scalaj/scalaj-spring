package scalaj.spring
import org.springframework.core.convert.TypeDescriptor
import org.springframework.core.GenericTypeResolver
import org.springframework.core.MethodParameter

import java.{lang => jl}
import java.lang.reflect.{ Type => JType, Array => _, _ }
import scala.reflect.Manifest
import scala.reflect.Manifest.{ classType, intersectionType, arrayType, wildcardType }

//import tools.scalap.scalax.rules.scalasig.{ScalaSig, ScalaSigParser}


/**
 * Negotiates between Spring's TypeDescriptor and Scala's Manifest mechanisms
 */

object TypeWrangler {

  def primaryCtorMethodParam(clazz : Class[_], idx: Int = 0) = {
    val ctor = clazz.getConstructors.apply(0)
    val methodParam = new MethodParameter(ctor, idx)
    methodParam
  }

  val ByteClass = classOf[scala.Byte]
  val ShortClass = classOf[scala.Short]
  val CharClass = classOf[scala.Char]
  val IntClass = classOf[scala.Int]
  val LongClass = classOf[scala.Long]
  val FloatClass = classOf[scala.Float]
  val DoubleClass = classOf[scala.Double]
  val BooleanClass = classOf[scala.Boolean]
  val NullClass = classOf[scala.Null]
  val UnitClass = classOf[scala.Unit]

  val JByteClass = classOf[jl.Byte]
  val JShortClass = classOf[jl.Short]
  val JCharClass = classOf[jl.Character]
  val JIntClass = classOf[jl.Integer]
  val JLongClass = classOf[jl.Long]
  val JFloatClass = classOf[jl.Float]
  val JDoubleClass = classOf[jl.Double]
  val JBooleanClass = classOf[jl.Boolean]


  // Manifest.classType(x) will return a Manifest  
  def manifestOf(c : Class[_]) = c match {
    case JByteClass    | jl.Byte.TYPE      | ByteClass    => Manifest.Byte
    case JShortClass   | jl.Short.TYPE     | ShortClass   => Manifest.Short
    case JCharClass    | jl.Character.TYPE | CharClass    => Manifest.Char
    case JIntClass     | jl.Integer.TYPE   | IntClass     => Manifest.Int
    case JLongClass    | jl.Long.TYPE      | LongClass    => Manifest.Long
    case JFloatClass   | jl.Float.TYPE     | FloatClass   => Manifest.Float
    case JDoubleClass  | jl.Double.TYPE    | DoubleClass  => Manifest.Double
    case JBooleanClass | jl.Boolean.TYPE   | BooleanClass => Manifest.Boolean
    case                 jl.Void.TYPE      | UnitClass    => Manifest.Unit
    case                 null              | NullClass    => Manifest.Null
    case x => classType(x)
  }


 def intersect(tps: JType*): Manifest[_] = intersectionType(tps map javaType: _*)
 def javaType(tp: JType): Manifest[_] = tp match {
   case null => Manifest.Null
   case x: Class[_]            => manifestOf(x)
   case x: ParameterizedType   =>
     val owner = x.getOwnerType
     val raw   = x.getRawType() match { case clazz: Class[_] => clazz }
     val targs = x.getActualTypeArguments() map javaType

     (owner == null, targs.isEmpty) match {
       case (true, true)   => javaType(raw)
       case (true, false)  => classType(raw, targs.head, targs.tail: _*)
       case (false, _)     => classType(javaType(owner), raw, targs: _*)
     }
   case x: GenericArrayType    => arrayType(javaType(x.getGenericComponentType))
   case x: WildcardType        => wildcardType(intersect(x.getLowerBounds: _*), intersect(x.getUpperBounds: _*))
   case x: TypeVariable[_]     => intersect(x.getBounds(): _*)
 }



  def typeParamsFor(td : TypeDescriptor) : List[JType] = {
    val targetType = Option(td.getMethodParameter) map { GenericTypeResolver.getTargetType }
    targetType match {
      case Some(pt : ParameterizedType) => pt.getActualTypeArguments.toList
      case _ => Nil
    }
  }

  def typeFromDescriptor(td : TypeDescriptor) =
    Option(td.getMethodParameter) map {_.getGenericParameterType} getOrElse (td.getType)

  implicit def manifestFromTypeDescriptor(td : TypeDescriptor) = javaType(typeFromDescriptor(td))

  def classFromTypeDescriptor(td : TypeDescriptor) = {
    td.getType match {
      case null => classOf[Null]
      case _ => td.getObjectType
    }
  }
}