package scalaj
package spring

import java.{ util => ju }
import scala.collection.JavaConversions._
import org.springframework.core.convert.TypeDescriptor
import org.springframework.core.convert.converter.ConditionalGenericConverter
import org.springframework.core.convert.converter.GenericConverter.ConvertiblePair
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
trait AbstractConverter {
  def convertibleTypes : Set[ConvertiblePair]
  def getConvertibleTypes : ju.Set[ConvertiblePair] = convertibleTypes

  def matches(srcType : TypeDescriptor, targetType : TypeDescriptor) : Boolean

  def convert(source : Object, srcType : TypeDescriptor, targetType : TypeDescriptor) : Object

}

class JavaToScalaCollectionConverter extends ConditionalGenericConverter with AbstractConverter {

  implicit val conversions = Conversions.allToScala

  def convertibleTypes : Set[ConvertiblePair] = conversions.toConvertiblePairSet

  def matches(srcType : TypeDescriptor, targetType : TypeDescriptor) : Boolean =
    conversions.exists(srcType, targetType)

  def convert(source : Object, srcType : TypeDescriptor, targetType : TypeDescriptor) : Object =
    conversions.get(srcType, targetType) match {
      case Some(conversion) => conversion.apply(source)
      case None => error("can't do this")
    }
}
