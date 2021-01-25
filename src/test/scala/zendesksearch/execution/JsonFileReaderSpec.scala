package zendesksearch.execution

import io.circe.Decoder
import io.circe.generic.semiauto.deriveDecoder
import org.scalactic.TypeCheckedTripleEquals
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

class JsonFileReaderSpec extends AnyFreeSpec with Matchers with TypeCheckedTripleEquals {
  case class Foo(id: Int, name: String, active: Boolean)

  object Foo {
    implicit val jsonDecoder: Decoder[Foo] = deriveDecoder
  }

  "decoding a valid JSON file" - {
    "that has a valid schema" - {
      "should produce a successful result" in {
        val filePath = "src/test/resources/valid-json-file-with-valid-schema.json"
        val io = JsonFileReader.readAs[List[Foo]](filePath)

        io.attempt.unsafeRunSync() should ===(Right(List(Foo(1, "Saturn", true), Foo(2, "Jupiter", false))))
      }
    }

    "that has an invalid schema" - {
      "should produce an appropriate error" in {
        val filePath = "src/test/resources/valid-json-file-with-invalid-schema.json"
        val io = JsonFileReader.readAs[List[Foo]](filePath)

        io.attempt.unsafeRunSync() should matchPattern { case Left(FileDecodeError(`filePath`, _)) => }
      }
    }
  }

  "decoding an invalid JSON file" - {
    "should produce an appropriate error" in {
      val filePath = "src/test/resources/invalid-json-file.json"
      val io = JsonFileReader.readAs[List[Foo]](filePath)

      io.attempt.unsafeRunSync() should matchPattern { case Left(FileDecodeError(`filePath`, _)) => }
    }
  }

  "decoding a file that does not exist" - {
    "should produce an appropriate error" in {
      val filePath = "path/to/nothing.json"
      val io = JsonFileReader.readAs[List[Foo]](filePath)

      io.attempt.unsafeRunSync() should matchPattern { case Left(FileDecodeError(`filePath`, _)) => }
    }
  }
}
