package zendesksearch

import cats.effect._
import zendesksearch.database._
import zendesksearch.execution._

import scala.io.StdIn.readLine

object Main extends IOApp {
  private val usersFilePath = "src/main/resources/users.json"
  private val organizationsFilePath = "src/main/resources/organizations.json"
  private val ticketsFilePath = "src/main/resources/tickets.json"

  def run(args: List[String]): IO[ExitCode] = for {
    users <- JsonFileReader.readAs[List[User]](usersFilePath)
    organizations <- JsonFileReader.readAs[List[Organization]](organizationsFilePath)
    tickets <- JsonFileReader.readAs[List[Ticket]](ticketsFilePath)

    enrichedUsers = EnrichedUser.enrichAll(users, organizations, tickets)
    enrichedTickets = EnrichedTicket.enrichAll(tickets, organizations, users)
    enrichedOrganizations = EnrichedOrganization.enrichAll(organizations, users, tickets)

    enrichedUserDatabase = Database(enrichedUsers)
    enrichedTicketDatabase = Database(enrichedTickets)
    enrichedOrganizationDatabase = Database(enrichedOrganizations)

    _ <- IO(println(Program.welcomeMessage))
    _ <- runProgram(
      Program(
        ProgramShowSearchOptions,
        enrichedUserDatabase,
        enrichedTicketDatabase,
        enrichedOrganizationDatabase,
        string => IO(println(string))
      )
    )
  } yield ExitCode.Success

  private def runProgram(program: Program): IO[Unit] = for {
    getNextProgram <- program.tick
    input <- IO(readLine("> ")) <* IO(println())
    _ <- input.trim match {
      case quit if quit.toLowerCase == Program.quitCommand => IO.unit
      case otherInput                                      => getNextProgram(otherInput).flatMap(runProgram)
    }
  } yield ()
}
