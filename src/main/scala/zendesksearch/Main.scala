package zendesksearch

import cats.effect._

import scala.io.StdIn.readLine

object Main extends IOApp {
  def run(args: List[String]): IO[ExitCode] = program.as(ExitCode.Success)

  private def program: IO[Unit] = {
    def innerLoop: IO[Unit] = for {
      _ <- IO(println(searchOptionsMessage))
      input <- IO(readLine())
      _ <- handleInput(input)
      _ <- innerLoop
    } yield ()

    for {
      _ <- IO(println(welcomeMessage))
      _ <- innerLoop
    } yield ()
  }

  private def handleInput(input: String): IO[Unit] = input.toLowerCase match {
    case "quit" => IO(sys.exit())
    case _ => IO(println(s"You have entered: ${input}\n"))
  }

  private val welcomeMessage =
    """Welcome to Zendesk Search
      |Type 'quit' to exit at any time, press 'Enter' to continue.
      |""".stripMargin

  private val searchOptionsMessage =
    """Select search options:
      |* Press 1 to search Zendesk
      |* Press 2 to view a list of searchable fields
      |* Type 'quit' to exit
      |""".stripMargin
}
