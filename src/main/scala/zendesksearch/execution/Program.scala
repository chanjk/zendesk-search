package zendesksearch.execution

import cats.effect._
import zendesksearch.database._

case class Program(
  programStage: ProgramStage,
  enrichedUserDatabase: Database[EnrichedUser],
  enrichedTicketDatabase: Database[EnrichedTicket],
  enrichedOrganizationDatabase: Database[EnrichedOrganization]
) {
  def tick: IO[String => IO[Program]] = run.as(handleInput)

  private def toStage(programStage: ProgramStage): Program = copy(programStage = programStage)

  private def run: IO[Unit] = programStage match {
    case ProgramShowSearchOptions   => runProgramShowSearchOptions
    case ProgramSearchActive(stage) => runProgramSearchActive(stage)
  }

  private def runProgramShowSearchOptions: IO[Unit] = IO(println(searchOptionsMessage))

  private def runProgramSearchActive(searchStage: SearchStage): IO[Unit] = searchStage match {
    case SearchQueryingType        => IO(println(searchQueryingTypeMessage))
    case SearchQueryingField(_)    => IO(println(searchQueryingFieldMessage))
    case SearchQueryingValue(_, _) => IO(println(searchQueryingValueMessage))
  }

  private def handleInput(input: String): IO[Program] = programStage match {
    case ProgramShowSearchOptions   => handleProgramShowSearchOptionsInput(input)
    case ProgramSearchActive(stage) => handleProgramSearchActiveInput(stage, input)
  }

  private def handleProgramShowSearchOptionsInput(input: String): IO[Program] = input match {
    case "1" => IO.pure(toStage(ProgramSearchActive(SearchQueryingType)))

    case "2" =>
      for {
        _ <- IO(
          println(searchableFieldsOutput(enrichedUserDatabase.searchFields, enrichedTicketDatabase.searchFields, enrichedOrganizationDatabase.searchFields))
        )
      } yield toStage(ProgramShowSearchOptions)

    case _ => IO.pure(toStage(ProgramShowSearchOptions))
  }

  private def handleProgramSearchActiveInput(searchStage: SearchStage, input: String): IO[Program] = searchStage match {
    case SearchQueryingType => {
      val maybeSearchType: Option[SearchType] = input match {
        case "1" => Some(SearchUser)
        case "2" => Some(SearchTicket)
        case "3" => Some(SearchOrganization)
        case _   => None
      }

      maybeSearchType match {
        case Some(searchType) => IO.pure(toStage(ProgramSearchActive(SearchQueryingField(searchType))))
        case None             => IO.pure(toStage(ProgramSearchActive(SearchQueryingType)))
      }
    }

    case SearchQueryingField(searchType) =>
      IO.pure(toStage(ProgramSearchActive(SearchQueryingValue(searchType, input))))

    case SearchQueryingValue(searchType, searchField) => {
      val searchValue = Some(input).filter(_.nonEmpty)

      val stringifiedResults: List[String] = searchType match {
        case SearchUser =>
          enrichedUserDatabase.search(searchField, searchValue).map(ResultRenderer.render[EnrichedUser])
        case SearchTicket =>
          enrichedTicketDatabase.search(searchField, searchValue).map(ResultRenderer.render[EnrichedTicket])
        case SearchOrganization =>
          enrichedOrganizationDatabase.search(searchField, searchValue).map(ResultRenderer.render[EnrichedOrganization])
      }
      val summary = s"${stringifiedResults.length} result(s) found with $searchField: '$input'"

      IO(println(stringifiedResults.mkString(s"$summary\n", "\n\n", "\n"))).as(toStage(ProgramShowSearchOptions))
    }
  }

  private val searchOptionsMessage: String =
    """Select search options:
      |* Press 1 to search Zendesk
      |* Press 2 to view a list of searchable fields
      |* Type 'quit' to exit
      |""".stripMargin

  private val searchQueryingTypeMessage: String = "Select 1) Users or 2) Tickets or 3) Organizations"

  private val searchQueryingFieldMessage: String = "Enter search field"

  private val searchQueryingValueMessage: String = "Enter search value"

  private def searchableFieldsOutput(
    enrichedUserSearchFields: List[String],
    enrichedTicketSearchFields: List[String],
    enrichedOrganizationSearchFields: List[String]
  ): String =
    s"""Search Users with:
       |
       |${enrichedUserSearchFields.mkString("\n")}
       |------------------------------------------
       |Search Tickets with:
       |
       |${enrichedTicketSearchFields.mkString("\n")}
       |------------------------------------------
       |Search Organizations with:
       |
       |${enrichedOrganizationSearchFields.mkString("\n")}
       |""".stripMargin
}
