package zendesksearch

import cats.effect._

import scala.io.StdIn.readLine

object Main extends IOApp {
  def run(args: List[String]): IO[ExitCode] = for {
    users <- JsonFileReader.readAs[List[User]]("src/main/resources/users.json")
    _ <- program(UserDatabase(users))
  } yield ExitCode.Success

  private def program(userDatabase: UserDatabase): IO[Unit] = {
    def handleInput(input: String): IO[Unit] = input.toLowerCase match {
      case "2" => IO(println(searchableFieldsOutput(userDatabase.searchFields)))
      case "quit" => IO(sys.exit())
      case _ => IO(println(userDatabase.search("_id", "1")))
    }

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

  private def searchableFieldsOutput(userSearchFields: List[String]): String =
    s"""Search Users with:
      |
      |${userSearchFields.mkString("\n")}
      |""".stripMargin
}
