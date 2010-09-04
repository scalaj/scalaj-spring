package scalaj
package spring

import java.{ lang => jl, util => ju }
import scala.collection.mutable
import spring.Implicits._

import org.springframework.core.convert.TypeDescriptor
import java.util.{concurrent => juc}
import org.springframework.core.convert.converter.{ConditionalGenericConverter, GenericConverter}
import reflect.{Manifest, ClassManifest}
import org.springframework.core.convert.converter.GenericConverter.ConvertiblePair
import scala.collection.JavaConversions._

import TypeWrangler._


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

  import Conversion.funcToConversion

//  def emptyConcurrentMap[A,B] = asConcurrentMap(new juc.ConcurrentHashMap[A,B])

  val conversions : Seq[Conversion[_,_]] = Seq(
    asMap(_ : ju.Properties), //asMap
    asMap(_ : ju.Dictionary[_,_]), //asMap

    asConcurrentMap(_ : juc.ConcurrentMap[_,_]),
    asMap(_ : ju.Map[_,_]),

    asSet(_ : ju.Set[_]),
    asBuffer(_ : ju.List[_]),
    asIterable(_ : ju.Collection[_]),

    asIterable(_ : jl.Iterable[_]),
    asIterator(_ : ju.Iterator[_]),
    asIterator(_ : ju.Enumeration[_]),


//    implicitly[ju.Properties => mutable.Map[String, String]], //asMap
//    implicitly[ju.List[_] => Seq[_]],


//    (_:Null) => emptyConcurrentMap,
    (_:Null) => mutable.Map.empty,
    (_:Null) => Map.empty,

    (n:Null) => mutable.Buffer.empty,
    (n:Null) => mutable.Buffer.empty,
    (n:Null) => mutable.Seq.empty,
    (n:Null) => Seq.empty,

    (n:Null) => mutable.Set.empty,
    (n:Null) => Set.empty,

    (n:Null) => Iterable.empty,
    (n:Null) => Iterator.empty
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
