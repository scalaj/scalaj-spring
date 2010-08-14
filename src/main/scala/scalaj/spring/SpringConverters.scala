package scalaj
package spring

import java.{ lang => jl, util => ju }
import scala.collection.mutable
import spring.Implicits._

import org.springframework.core.convert.TypeDescriptor
import java.util.{concurrent => juc}
import org.springframework.core.convert.converter.{ConditionalGenericConverter, GenericConverter}
import reflect.{Manifest, ClassManifest}

/**
 * Oh boy, is this ever embarrassing!
 * 
 * It seems that Spring is rather null-happy, and null in Java-land?
 * well... it doesn't actually have a type as such...
 *
 * This is somewhat difficult, as it means the only way can convert from
 * null values is to be a bit of a type-slut and *claim* we'll do anything!
 * (we won't of course, but it never hurts to advertise)
 *
 * Of course, this wouldn't actually be a problem if Spring stopped pimping nulls and
 * instead represented an empty collection as, well, an empty collection!
 * Whoever claimed "it's not the type that matters, it's how you use it" has clearly
 * been only involved with a few well-intentioned but ultimately unsatisfying libraries.
 * I feel sorry for them...
 *
 * Never mind, eh?  We'll just make ourselves a `Conditional`GenericConverter, and
 * pretend we're up for whatever sordid games the compiler claims we'll play.
 * Then we can pause briefly in the `matches` method before the *real* sports kick off.
 *
 * That way, when it actually comes to the crunch, there's always one last chance
 * at runtime to develop cold feet and say "well, actually..."
 */
class JavaToScalaCollectionConverter extends ConditionalGenericConverter {
  import GenericConverter.ConvertiblePair

  def resolveGenerics(td : TypeDescriptor) = {
    import org.springframework.core.GenericTypeResolver
    if (td.getMethodParameter != null) {
      val tpe = GenericTypeResolver.getTargetType(td.getMethodParameter)
      Some(tpe)
    } else None
  }

  implicit def classFromTypeDescriptor(td : TypeDescriptor) = {
    println("converting from " + td)
    println(resolveGenerics(td))
    val result = td.getType match {
      case null => classOf[Null]
      case _ => td.getObjectType
    }
    println("converted to " + result)
    result
  }

  class Conversion[-From : ClassManifest, +To : ClassManifest](func : From => To) {
    private[this] val evFrom = classManifest[From]
    private[this] val evTo = classManifest[To]

    def validFor(srcType: Class[_], targetType: Class[_]) = {
      ClassManifest.fromClass(srcType) <:< evFrom && ClassManifest.fromClass(targetType) <:< evTo
    }

    def apply(src : Object) : Object = func(src.asInstanceOf[From]).asInstanceOf[Object]

    def toConvertiblePair : ConvertiblePair = evFrom match {
      case Manifest.Null => new ConvertiblePair(classOf[Object], evTo.erasure)
      case _ =>  new ConvertiblePair(evFrom.erasure, evTo.erasure)
    }
  }

  object Conversion {
    def apply[F : ClassManifest, T : ClassManifest](func : Function1[F,T]) = new Conversion(func)
  }

//  object ImplicitConversion {
//    def apply[F <% T : ClassManifest, T : ClassManifest] = new Conversion(implicitly[F <%< T])
//  }

  object NullConversion {
    def apply[T : ClassManifest](generator : => T) = new Conversion((n:Null) => generator _)
  }

  //def emptyConcurrentMap[A,B] = asConcurrentMap(new juc.ConcurrentHashMap[A,B])

  /*
   * Scala Types Referenced:
   *
   *  Iterator
   *  Iterable
   *    Set
   *      mutable.Set
   *    Seq
   *      mutable.Seq
   *        mutable.Buffer
   *    Map
   *      mutable.Map
   *        mutable.ConcurrentMap
   *
   * Java Types Referenced:
   *
   *  Iterator
   *  Iterable
   *  Enumeration
   *  Collection
   *    List
   *    Set
   *    Map
   *      ConcurrentMap
   *  Dictionary
   *  Properties
   *
   * relationships shown by indentation
   */

  //def conversion[From[_,_], To[_,_], A, B](from:From[A,B], to:To[A,B]) = from <%< To

  val conversions : Seq[Conversion[_,_]] = Seq(
//    Conversion(implicitly[ju.Properties => mutable.Map[String, String]]), //asMap
//    Conversion(asMap(_ : ju.Dictionary[_,_])), //asMap
//
//    Conversion(asConcurrentMap(_ : juc.ConcurrentMap[_,_])),
//    Conversion(asMap(_ : ju.Map[_,_])),
//
//    Conversion(asSet(_ : ju.Set[_])),
//    Conversion(asBuffer(_ : ju.List[_])),
    Conversion(implicitly[ju.List[_] => Seq[_]])
//    Conversion(asIterable(_ : ju.Collection[_])),
//
//    Conversion(asIterable(_ : jl.Iterable[_])),
//    Conversion(asIterator(_ : ju.Iterator[_])),
//    Conversion(asIterator(_ : ju.Enumeration[_])),
//
//    NullConversion(emptyConcurrentMap),
//    NullConversion(mutable.Map.empty),
//    NullConversion(Map.empty),
//
//    NullConversion(mutable.Buffer.empty),
//    NullConversion(mutable.Seq.empty),
//    NullConversion(Seq.empty),
//
//    NullConversion(mutable.Set.empty),
//    NullConversion(Set.empty),
//
//    NullConversion(Iterable.empty),
//    NullConversion(Iterator.empty)
  )

  def convertibleTypes : Set[ConvertiblePair] = (conversions map (_.toConvertiblePair)).toSet
  def getConvertibleTypes : ju.Set[ConvertiblePair] = convertibleTypes

  def matches(srcType : TypeDescriptor, targetType : TypeDescriptor) : Boolean =
    conversions.exists(_.validFor(srcType, targetType))




  def convert(source : Object, srcType : TypeDescriptor, targetType : TypeDescriptor) : Object =
    conversions find {_.validFor(srcType, targetType)} match {
      case Some(conversion) => conversion(source)
      case None => error("can't do this")
    }
}
