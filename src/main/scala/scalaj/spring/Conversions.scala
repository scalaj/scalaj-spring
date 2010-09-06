package scalaj.spring

import java.{ lang => jl, util => ju }
import java.util.{concurrent => juc}
import scala.collection.JavaConversions._
import scala.collection.mutable

import tools.util.StringOps


class Conversions(val seq : Conversion[_,_]*) {
  def exists(src : Manifest[_], target : Manifest[_]) = seq exists {_.validFor(src, target)}
  def get(src : Manifest[_], target : Manifest[_]) = seq find {_.validFor(src, target)}
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
    byte2short _,
    byte2int _,
    byte2long _,
    byte2float _,
    byte2double _,
    short2int _,
    short2long _,
    short2float _,
    short2double _,
    char2int _,
    char2long _,
    char2float _,
    char2double _,
    int2long _,
    int2float _,
    int2double _,
    long2float _,
    long2double _,
    float2double _
  )

  val autoboxScalaPrimitives = Conversions(
    byte2Byte _,
    short2Short _,
    char2Character _,
    int2Integer _,
    long2Long _,
    float2Float _,
    double2Double _,
    boolean2Boolean _
  )

  val stringsToPrimitives = Conversions(
    (s:String) => s.toByte,
    (s:String) => s.toShort,
    (s:String) => s.head, //toCharacter
    (s:String) => s.toInt,
    (s:String) => s.toLong,
    (s:String) => s.toFloat,
    (s:String) => s.toDouble,
    (s:String) => s.toBoolean
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