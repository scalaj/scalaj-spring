package scalaj.spring

import java.{ lang => jl, util => ju }
import java.util.{concurrent => juc}
import scala.collection.JavaConversions._
import scala.collection.mutable
import org.springframework.core.convert.converter.GenericConverter.ConvertiblePair
import org.springframework.core.convert.TypeDescriptor

import TypeWrangler._


class Conversions(val seq : Conversion[_,_]*) {
  def exists(src : TypeDescriptor, target : TypeDescriptor) = seq exists {_.validFor(src, target)}
  def apply(src : TypeDescriptor, target : TypeDescriptor) = seq find {_.validFor(src, target)}
  def toConvertiblePairSet = (seq map (_.toConvertiblePair)).toSet

  def ++(other : Conversions) = new Conversions(this.seq ++ other.seq : _*)
}


object Conversions {
  import Conversion.funcToConversion

  def apply(seq : Conversion[_,_]*) = new Conversions(seq : _*)

  val javaToScalaCollections = Conversions(
    asMap(_ : ju.Properties),
    asMap(_ : ju.Dictionary[_,_]),
    asConcurrentMap(_ : juc.ConcurrentMap[_,_]),
    asMap(_ : ju.Map[_,_]),
    asSet(_ : ju.Set[_]),
    asBuffer(_ : ju.List[_]),
    asIterable(_ : ju.Collection[_]),
    asIterable(_ : jl.Iterable[_]),
    asIterator(_ : ju.Iterator[_]),
    asIterator(_ : ju.Enumeration[_])
//  implicitly[ju.Properties => mutable.Map[String, String]], //asMap
//  implicitly[ju.List[_] => Seq[_]],
  )

  val scalaPrimitiveWidening = Conversions(
    byte2short, byte2int, byte2long, byte2float, byte2double,
    short2int, short2long, short2float, short2double,
    char2int, char2long, char2float, char2double,
    int2long, int2float, int2double,
    long2float, long2double,
    float2double
  )

  val autoboxScalaPrimitives = Conversions(
    byte2Byte,
    short2Short,
    char2Character,
    int2Integer,
    long2Long,
    float2Float,
    double2Double,
    boolean2Boolean
  )


  //  private[this] def emptyConcurrentMap[A,B] = asConcurrentMap(new juc.ConcurrentHashMap[A,B])

  val nullToScalaCollections = Conversions(
//  (_:Null) => emptyConcurrentMap,
    (_:Null) => mutable.Map.empty,
    (_:Null) => Map.empty,
    (_:Null) => mutable.Buffer.empty,
    (_:Null) => mutable.Buffer.empty,
    (_:Null) => mutable.Seq.empty,
    (_:Null) => Seq.empty,
    (_:Null) => mutable.Set.empty,
    (_:Null) => Set.empty,
    (_:Null) => Iterable.empty,
    (_:Null) => Iterator.empty
  )

  val allToScala = javaToScalaCollections ++ nullToScalaCollections
}