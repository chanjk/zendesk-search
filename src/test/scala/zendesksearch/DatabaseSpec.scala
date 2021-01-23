package zendesksearch

import org.scalactic.TypeCheckedTripleEquals
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

class DatabaseSpec extends AnyFreeSpec with Matchers with TypeCheckedTripleEquals {
  case class Foo(id: Int, name: String, active: Boolean)

  object Foo {
    implicit val indexable: Indexable[Foo] = Indexable.fromIndexer(
      Map(
        "id" -> (foo => Set(foo.id.toString)),
        "name" -> (foo => Set(foo.name)),
        "active" -> (foo => Set(foo.active.toString))
      )
    )
  }

  val foo1 = Foo(1, "Alex", true)
  val foo2 = Foo(2, "Blake", true)
  val foo3 = Foo(3, "Charlie", false)

  val database = Database[Foo](List(foo1, foo2, foo3))

  "searching" - {
    "on a search term that exists" - {
      "with a search value that exists" - {
        "should produce the list of matches" in {
          database.search("active", Some("true")) should ===(List(foo1, foo2))
        }
      }

      "with a search value that does not exist" - {
        "should produce no matches" in {
          database.search("id", Some("9999")) should ===(Nil)
        }
      }
    }

    "on a search term that does not exist" - {
      "should produce no matches" in {
        database.search("bar", Some("1")) should ===(Nil)
      }
    }
  }

  "list of search fields" - {
    "should contain all the search terms in the index" in {
      database.searchFields should contain theSameElementsAs List("id", "name", "active")
    }

    "should be sorted" in {
      database.searchFields shouldBe sorted
    }
  }
}
