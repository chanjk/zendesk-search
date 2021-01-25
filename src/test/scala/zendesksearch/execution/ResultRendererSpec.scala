package zendesksearch.execution

import org.scalactic.TypeCheckedTripleEquals
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers
import zendesksearch.database.Renderable

class ResultRendererSpec extends AnyFreeSpec with Matchers with TypeCheckedTripleEquals {
  case class Foo(id: Int, name: String, active: Boolean)

  object Foo {
    implicit val renderable: Renderable[Foo] = { case Foo(id, name, active) =>
      List("id" -> Some(id.toString), "name" -> Some(name), "active" -> Some(active.toString))
    }
  }

  "rendering a result" - {
    "should produce a stringified table representing the result" in {
      val foo = Foo(123, "John Foo", true)
      val expectedTable =
        """┌───────────────────────────────────────┬──────────────────────────────────────┐
          |│id                                     │123                                   │
          |├───────────────────────────────────────┼──────────────────────────────────────┤
          |│name                                   │John Foo                              │
          |├───────────────────────────────────────┼──────────────────────────────────────┤
          |│active                                 │true                                  │
          |└───────────────────────────────────────┴──────────────────────────────────────┘""".stripMargin

      ResultRenderer.render(foo) should ===(expectedTable)
    }
  }
}
