package zendesksearch.execution

import cats.effect._
import zendesksearch.database._
import zendesksearch.execution.Program._
import zendesksearch.execution.ResultRenderer.render

case class Program(
  programStage: ProgramStage,
  enrichedUserDatabase: Database[EnrichedUser],
  enrichedTicketDatabase: Database[EnrichedTicket],
  enrichedOrganizationDatabase: Database[EnrichedOrganization],
  writeOutput: String => IO[Unit]
) {
  private val searchableFieldsMessage =
    s"""Search Users with:
       |
       |${enrichedUserDatabase.searchFields.mkString("\n")}
       |------------------------------------------
       |Search Tickets with:
       |
       |${enrichedTicketDatabase.searchFields.mkString("\n")}
       |------------------------------------------
       |Search Organizations with:
       |
       |${enrichedOrganizationDatabase.searchFields.mkString("\n")}
       |""".stripMargin

  def tick: IO[String => IO[Program]] = run.as(handleInput)

  private def toStage(programStage: ProgramStage): Program = copy(programStage = programStage)

  private def run: IO[Unit] = programStage match {
    case ProgramShowSearchOptions => writeOutput(searchOptionsMessage)

    case ProgramSearching(stage) =>
      stage match {
        case SearchQueryingType        => writeOutput(searchQueryingTypeMessage)
        case SearchQueryingField(_)    => writeOutput(searchQueryingFieldMessage)
        case SearchQueryingValue(_, _) => writeOutput(searchQueryingValueMessage)
      }
  }

  private def handleInput(input: String): IO[Program] = programStage match {
    case ProgramShowSearchOptions =>
      input match {
        case "1" => IO.pure(toStage(ProgramSearching(SearchQueryingType)))
        case "2" => writeOutput(searchableFieldsMessage).as(toStage(ProgramShowSearchOptions))
        case _   => IO.pure(toStage(ProgramShowSearchOptions))
      }

    case ProgramSearching(stage) =>
      stage match {
        case SearchQueryingType => {
          val inputToSearchType = Map("1" -> SearchUser, "2" -> SearchTicket, "3" -> SearchOrganization)
          val nextSearchStage = inputToSearchType.get(input).fold[SearchStage](SearchQueryingType)(SearchQueryingField)

          IO.pure(toStage(ProgramSearching(nextSearchStage)))
        }
        case SearchQueryingField(searchType) =>
          Some(input).filter(_.nonEmpty) match {
            case Some(searchField) => IO.pure(toStage(ProgramSearching(SearchQueryingValue(searchType, searchField))))
            case None =>
              writeOutput(emptySearchFieldMessage).as(toStage(ProgramSearching(SearchQueryingField(searchType))))
          }
        case SearchQueryingValue(searchType, searchField) => {
          val searchValue = Some(input).filter(_.nonEmpty)

          val stringifiedResults = searchType match {
            case SearchUser   => enrichedUserDatabase.search(searchField, searchValue).map(render[EnrichedUser])
            case SearchTicket => enrichedTicketDatabase.search(searchField, searchValue).map(render[EnrichedTicket])
            case SearchOrganization =>
              enrichedOrganizationDatabase.search(searchField, searchValue).map(render[EnrichedOrganization])
          }
          val summary = s"${stringifiedResults.length} result(s) found with $searchField: '$input'"

          writeOutput(stringifiedResults.mkString(s"$summary\n", "\n\n", "\n")).as(toStage(ProgramShowSearchOptions))
        }
      }
  }
}

object Program {
  val quitCommand = "quit"

  val welcomeMessage =
    s"""Welcome to Zendesk Search
       |Type '$quitCommand' to exit at any time.
       |""".stripMargin

  val searchOptionsMessage =
    s"""Select search options:
       |* Press 1 to search Zendesk
       |* Press 2 to view a list of searchable fields
       |* Type '$quitCommand' to exit
       |""".stripMargin

  val searchQueryingTypeMessage = "Select 1) Users or 2) Tickets or 3) Organizations"

  val searchQueryingFieldMessage = "Enter search field"

  val emptySearchFieldMessage = "Search field cannot be empty"

  val searchQueryingValueMessage = "Enter search value"
}
