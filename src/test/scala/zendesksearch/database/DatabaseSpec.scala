package zendesksearch.database

import org.scalactic.TypeCheckedTripleEquals
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

class DatabaseSpec extends AnyFreeSpec with Matchers with TypeCheckedTripleEquals {
  case class Foo(
    id: Int,
    name: String,
    active: Boolean,
    email: Option[String],
    tags: List[String],
    hobbies: List[String]
  )

  object Foo {
    implicit val indexable: Indexable[Foo] = foo =>
      Map(
        "id" -> Set(foo.id.toString),
        "name" -> Set(foo.name),
        "active" -> Set(foo.active.toString),
        "email" -> foo.email.toSet,
        "tags" -> foo.tags.toSet,
        "hobbies" -> foo.hobbies.toSet
      )
  }

  val fooWithFullData = Foo(1, "Alex", true, Some("alex@foo.com"), List("Un", "Deux", "Trois"), List("running"))
  val fooWithNoEmail = Foo(2, "Blake", true, None, List("Trois", "Quatre", "Cinq"), List("reading", "eating"))
  val inactiveFooWithNoTags = Foo(3, "Charlie", false, Some("charlie@foo.com"), Nil, List("swimming"))

  val database = Database[Foo](List(fooWithFullData, fooWithNoEmail, inactiveFooWithNoTags))

  "searching" - {
    "on a search field that exists" - {
      "when the field can have at most one value" - {
        "with a non-empty search value" - {
          "that matches the relevant value for at least one record" - {
            "should produce the list of those records" in {
              database.search("active", Some("true")) should ===(List(fooWithFullData, fooWithNoEmail))
            }
          }

          "that matches the relevant value for no records" - {
            "should produce no matching records" in {
              database.search("id", Some("9999")) should ===(Nil)
            }
          }
        }

        "with an empty search value" - {
          "and the relevant value is absent for at least one record" - {
            "should produce the list of those records" in {
              database.search("email", None) should ===(List(fooWithNoEmail))
            }
          }

          "and the relevant value is absent for no records" - {
            "should produce no matching records" in {
              database.search("id", None) should ===(Nil)
            }
          }
        }
      }

      "when the field can have multiple values" - {
        "with a non-empty search value" - {
          "that matches one of the relevant values for at least one record" - {
            "should produce the list of those records" in {
              database.search("tags", Some("Trois")) should ===(List(fooWithFullData, fooWithNoEmail))
            }
          }

          "that matches one of the relevant values for no records" - {
            "should produce no matching records" in {
              database.search("tags", Some("Zero")) should ===(Nil)
            }
          }
        }

        "with an empty search value" - {
          "and the relevant values are absent for at least one record" - {
            "should produce the list of those records" in {
              database.search("tags", None) should ===(List(inactiveFooWithNoTags))
            }
          }

          "and the relevant values are absent for no records" - {
            "should produce no matching records" in {
              database.search("hobbies", None) should ===(Nil)
            }
          }
        }
      }
    }

    "on a search field that does not exist" - {
      "should produce no matches" in {
        database.search("bar", Some("1")) should ===(Nil)
      }
    }
  }

  "list of search fields" - {
    "should contain all the search fields in the index" in {
      database.searchFields should contain theSameElementsAs List("id", "name", "active", "email", "tags", "hobbies")
    }

    "should be sorted" in {
      database.searchFields shouldBe sorted
    }
  }
}
