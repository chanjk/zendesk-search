package zendesksearch

import cats.effect._

object Main extends IOApp {
  def run(args: List[String]): IO[ExitCode] = IO(println("Hello World!")).as(ExitCode.Success)
}
