package zendesksearch

import cats.effect._
import zendesksearch.database._
import zendesksearch.execution._

import scala.io.StdIn.readLine

object Main extends IOApp {
  private val usersFilePath = "src/main/resources/users.json"
  private val organizationsFilePath = "src/main/resources/organizations.json"
  private val ticketsFilePath = "src/main/resources/tickets.json"

  def run(args: List[String]): IO[ExitCode] = {
    val io = for {
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
    } yield ()

    io.attempt.flatMap {
      case Left(error) =>
        for {
          _ <- IO(println("Uh-oh! Zendesk Search has encountered an error:\n"))
          _ <- IO(error.printStackTrace())
          _ <- IO(println())
          _ <- IO(println("Please resolve the error before running the application again."))
          _ <- IO(println("If you require assistance, please contact the application maintainer."))
        } yield ExitCode.Error
      case Right(_) => IO(println("Thank you for using Zendesk Search!")).as(ExitCode.Success)
    }
  }

  private def runProgram(program: Program): IO[Unit] = for {
    getNextProgram <- program.tick
    input <- IO(readLine("> "))
    _ <- IO(println())
    _ <- input.trim match {
      case quit if quit.toLowerCase == Program.quitCommand => IO.unit
      case otherInput                                      => getNextProgram(otherInput).flatMap(runProgram)
    }
  } yield ()
}
