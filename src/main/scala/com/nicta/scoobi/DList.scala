/**
  * Copyright: [2011] Ben Lever
  */
package com.nicta.scoobi


/** A list that is distributed accross multiple machines. */
class DList[A : Manifest : HadoopWritable](/*FIXME: add private*/val ast: Smart.DList[A]) {

  // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
  // Primitive functionality.
  // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

  /** For each element of the distributed list produce zero or more elements by
    * applying a specified function. The resulting collection of elements form a
    * new distributed list. */
  def flatMap[B]
      (f: A => Iterable[B])
      (implicit mB:  Manifest[B],
                wtB: HadoopWritable[B]): DList[B] = new DList(Smart.FlatMap(ast, f))

  /** Concatenate one or more distributed lists to this distributed list. */
  def concat(ins: DList[A]*): DList[A] = new DList(Smart.Flatten(List(ast) ::: ins.map(_.ast).toList))

  /** Group the values of a distributed list with key-value elements by key. */
  def groupByKey[K, V]
      (implicit ev:   Smart.DList[A] <:< Smart.DList[(K, V)],
                mK:   Manifest[K],
                wtK:  HadoopWritable[K],
                ordK: Ordering[K],
                mV:   Manifest[V],
                wtV:  HadoopWritable[V]): DList[(K, Iterable[V])] = new DList(Smart.GroupByKey(ast))

  /** Apply an associative function to reduce the collection of values to a single value in a
    * key-value-collection distribued list. */
  def combine[K, V]
      (f: (V, V) => V)
      (implicit ev:   Smart.DList[A] <:< Smart.DList[(K,Iterable[V])],
                mK:   Manifest[K],
                wtK:  HadoopWritable[K],
                mV:   Manifest[V],
                wtV:  HadoopWritable[V]): DList[(K, V)] = new DList(Smart.Combine(ast, f))


  // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
  // Derrived functionality.
  // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

  /** For each element of the distributed list produce a new element by applying a
    * specified function. The resulting collection of elements form a new
    * distributed list. */
  def map[B]
      (f: A => B)
      (implicit mB:  Manifest[B],
                wtB: HadoopWritable[B]): DList[B] = flatMap(x => List(f(x)))

  /** Remove elements from the distributedl list that do not pass a spcecified
    * predicate function. */
  def filter(f: A => Boolean): DList[A] = flatMap(x => if (f(x)) List(x) else Nil)

  /** Group the values of distrubed list with key-value elements by key then reduce
    * the collection of values for each key using a specified associative function. */
  def reduceByKey[K, V]
      (f: (V, V) => V)
      (implicit ev:   Smart.DList[A] <:< Smart.DList[(K, V)],
                mK:   Manifest[K],
                wtK:  HadoopWritable[K],
                ordK: Ordering[K],
                mV:   Manifest[V],
                wtV:  HadoopWritable[V]): DList[(K, V)] = groupByKey.combine(f)
}


object DList {

  /** A class that specifies how to make a distributed list persisitent. */
  class DListPersister[A](val dl: DList[A], val persister: Smart.Persister[A])


  /** Persist one or more distributed lists. */
  def persist(outputs: DListPersister[_]*) = {

    val outMap: Map[Smart.DList[_], Smart.Persister[_]] = outputs.map(o => (o.dl.ast, o.persister)).toMap


    // TODO:
    //  - call Sean's MSCR code
    //  - call executePlan code

  }
}