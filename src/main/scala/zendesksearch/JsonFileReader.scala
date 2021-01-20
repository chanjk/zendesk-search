package zendesksearch

import cats.effect.IO
import io.circe.Decoder
import io.circe.jawn.decodeFile
import cats.syntax.all._

import java.io.File

object JsonFileReader {
  def readAs[A : Decoder](filePath: String): IO[A] =
    IO.suspend {
      val errorOrData = decodeFile[A](new File(filePath))
        .leftMap(error => FileDecodeError(filePath, error.show))

      IO.fromEither(errorOrData)
    }
}
