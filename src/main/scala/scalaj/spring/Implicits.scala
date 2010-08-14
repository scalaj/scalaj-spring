package scalaj.spring

import java.{ lang => jl, util => ju }
import collection.JavaConversions
import scalaj.collection.Implicits._
import scala.collection.mutable
/*
 defined in JavaConversions:

 // Java => Scala

 implicit def asIterator[A](i : ju.Enumeration[A]): Iterator[A]
 implicit def asIterator[A](i : ju.Iterator[A]): Iterator[A]
 implicit def asIterable[A](i : jl.Iterable[A]): Iterable[A]
 implicit def asIterable[A](i : ju.Collection[A]): Iterable[A]
 implicit def asBuffer[A](l : ju.List[A]): mutable.Buffer[A]
 implicit def asSet[A](s : ju.Set[A]): mutable.Set[A]
 implicit def asMap[A, B](m : ju.Map[A, B]): mutable.Map[A, B]
 implicit def asConcurrentMap[A, B](m: juc.ConcurrentMap[A, B]): mutable.ConcurrentMap[A, B]
 implicit def asMap[A, B](p: ju.Dictionary[A, B]): mutable.Map[A, B]
 implicit def asMap(p: ju.Properties): mutable.Map[String, String]

 // Scala => Java

 implicit def asEnumeration[A](i : Iterator[A]): ju.Enumeration[A]
 implicit def asIterator[A](i : Iterator[A]): ju.Iterator[A]
 implicit def asIterable[A](i : Iterable[A]): jl.Iterable[A]
 implicit def asCollection[A](i : Iterable[A]): ju.Collection[A]
 implicit def asSet[A](s: Set[A]): ju.Set[A]
 implicit def asSet[A](s : mutable.Set[A]): ju.Set[A]
 implicit def asList[A](b : Seq[A]): ju.List[A]
 implicit def asList[A](b : mutable.Seq[A]): ju.List[A]
 implicit def asList[A](b : mutable.Buffer[A]): ju.List[A]
 implicit def asMap[A, B](m : Map[A, B]): ju.Map[A, B]
 implicit def asMap[A, B](m : mutable.Map[A, B]): ju.Map[A, B]
 implicit def asConcurrentMap[A, B](m: mutable.ConcurrentMap[A, B]): juc.ConcurrentMap[A, B]
 implicit def asDictionary[A, B](m : mutable.Map[A, B]): ju.Dictionary[A, B]
*/

object Implicits {
  implicit def jlist2sseq(l : ju.List[_]) : Seq[_] = l.asScala

  implicit def sset2jset[A](s: Set[A]) : ju.Set[A] = s.asJava

}