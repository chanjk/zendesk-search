package zendesksearch.execution

import org.scalactic.TypeCheckedTripleEquals
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

class ExecutionErrorSpec extends AnyFreeSpec with Matchers with TypeCheckedTripleEquals {
  "FileDecodeError" - {
    "should produce the correct error message" in {
      val error = FileDecodeError("path/to/file", "Boom!")

      error.getMessage should ===("Failed to decode path/to/file: Boom!")
    }
  }
}
