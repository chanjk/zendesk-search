package zendesksearch

import cats.effect._

import scala.io.StdIn.readLine

object Main extends IOApp {
  def run(args: List[String]): IO[ExitCode] = for {
    users <- JsonFileReader.readAs[List[User]]("src/main/resources/users.json")
    // for now, just testing that organizations.json can be decoded
    _ <- JsonFileReader.readAs[List[Organization]]("src/main/resources/organizations.json")
    userDatabase = UserDatabase(users)
    _ <- IO(println(welcomeMessage))
    _ <- runProgram(Program(ProgramShowSearchOptions, userDatabase))
  } yield ExitCode.Success

  private def runProgram(program: Program): IO[Unit] = for {
    getNextProgram <- program.tick
    input <- IO(readLine("> "))
    _ <- IO(println())
    nextProgram <- input match {
      case quit if quit.toLowerCase == "quit" => IO(sys.exit())
      case otherInput => getNextProgram(otherInput)
    }
    _ <- runProgram(nextProgram)
  } yield ()

  private val welcomeMessage: String =
    """Welcome to Zendesk Search
      |Type 'quit' to exit at any time, press 'Enter' to continue.
      |""".stripMargin
}
