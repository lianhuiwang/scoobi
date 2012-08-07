package com.nicta.scoobi
package acceptance

import Scoobi._
import testing.NictaSimpleJobs
import application.ScoobiConfiguration

import SecondarySort._

class SecondarySortSpec extends NictaSimpleJobs {
  "We can do a secondary sort by using a Grouping on the key" >> { implicit sc: ScoobiConfiguration =>

    val names: DList[(FirstName, LastName)] = DList.apply(
      ("Michael", "Jackson"),
      ("Leonardo", "Da Vinci"),
      ("John", "Kennedy"),
      ("Mark", "Twain"),
      ("Bat", "Man"),
      ("Michael", "Jordan"),
      ("Mark", "Edison"),
      ("Michael", "Landon"),
      ("Leonardo", "De Capro"),
      ("Michael", "J. Fox"))

    val bigKey: DList[((FirstName, LastName), LastName)] = names.map(a => ((a._1, a._2), a._2))

    run(bigKey.groupByKey.map { case ((first, last), values) => ((first, last), values.mkString(", ")) }) === Seq(
      "((Bat,Man),Man)",
      "((John,Kennedy),Kennedy)",
      "((Leonardo,Da Vinci),Da Vinci, De Capro)",
      "((Mark,Edison),Edison, Twain)",
      "((Michael,J. Fox),J. Fox, Jackson, Jordan, Landon)")
  }
}

object SecondarySort {

  type FirstName = String
  type LastName = String

  implicit val grouping: Grouping[(FirstName, LastName)] = new Grouping[(FirstName, LastName)] {

    override def partition(key: (FirstName, LastName), howManyReducers: Int): Int =
      implicitly[Grouping[FirstName]].partition(key._1, howManyReducers)

    override def sortCompare(a: (FirstName, LastName), b: (FirstName, LastName)): Int = {
      val firstNameOrdering = groupCompare(a, b)
      firstNameOrdering match  {
        case 0 => a._2.compareTo(b._2)
        case x => x
      }
    }

    override def groupCompare(a: (FirstName, LastName), b: (FirstName, LastName)): Int = {
      a._1.compareTo(b._1)
    }

  }
}
