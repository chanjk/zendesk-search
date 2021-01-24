package zendesksearch

import cats.effect._

import scala.io.StdIn.readLine

object Main extends IOApp {
  def run(args: List[String]): IO[ExitCode] = for {
    users <- JsonFileReader.readAs[List[User]]("src/main/resources/users.json")
    organizations <- JsonFileReader.readAs[List[Organization]]("src/main/resources/organizations.json")
    tickets <- JsonFileReader.readAs[List[Ticket]]("src/main/resources/tickets.json")
    enrichedUsers = EnrichedUser.enrichAll(users, organizations, tickets)
    enrichedOrganizations = EnrichedOrganization.enrichAll(organizations, users, tickets)
    enrichedUserDatabase = Database[EnrichedUser](enrichedUsers)
    enrichedOrganizationDatabase = Database[EnrichedOrganization](enrichedOrganizations)
    _ <- IO(println(welcomeMessage))
    _ <- runProgram(Program(ProgramShowSearchOptions, enrichedUserDatabase, enrichedOrganizationDatabase))
  } yield ExitCode.Success

  private def runProgram(program: Program): IO[Unit] = for {
    getNextProgram <- program.tick
    input <- IO(readLine("> "))
    _ <- IO(println())
    nextProgram <- input.trim match {
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
