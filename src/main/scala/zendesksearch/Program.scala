package zendesksearch

import cats.effect._

case class Program(programStage: ProgramStage, enrichedUserDatabase: EnrichedUserDatabase) {
  def tick: IO[String => IO[Program]] = run.as(handleInput)

  private def toStage(programStage: ProgramStage): Program = copy(programStage = programStage)

  private def run: IO[Unit] = programStage match {
    case ProgramShowSearchOptions   => runProgramShowSearchOptions
    case ProgramSearchActive(stage) => runProgramSearchActive(stage)
  }

  private def runProgramShowSearchOptions: IO[Unit] = IO(println(searchOptionsMessage))

  private def runProgramSearchActive(searchStage: SearchStage): IO[Unit] = searchStage match {
    case SearchQueryingType        => IO(println(searchQueryingTypeMessage))
    case SearchQueryingTerm(_)     => IO(println(searchQueryingTermMessage))
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
        _ <- IO(println(searchableFieldsOutput(enrichedUserDatabase.searchFields)))
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
        case Some(searchType) => IO.pure(toStage(ProgramSearchActive(SearchQueryingTerm(searchType))))
        case None             => IO.pure(toStage(ProgramSearchActive(searchStage)))
      }
    }

    case SearchQueryingTerm(searchType) => IO.pure(toStage(ProgramSearchActive(SearchQueryingValue(searchType, input))))

    case SearchQueryingValue(searchType, searchTerm) => {
      val stringifiedResults: List[String] = searchType match {
        case SearchUser         => enrichedUserDatabase.search(searchTerm, input).map(ResultRenderer.render[EnrichedUser])
        case SearchTicket       => enrichedUserDatabase.search(searchTerm, input).map(_.toString) // use user database for now
        case SearchOrganization => enrichedUserDatabase.search(searchTerm, input).map(_.toString) // use user database for now
      }
      val summary = s"${stringifiedResults.length} result(s) found with $searchTerm: '$input'"

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

  private val searchQueryingTermMessage: String = "Enter search term"

  private val searchQueryingValueMessage: String = "Enter search value"

  private def searchableFieldsOutput(userSearchFields: List[String]): String =
    s"""Search Users with:
       |
       |${userSearchFields.mkString("\n")}
       |""".stripMargin
}
