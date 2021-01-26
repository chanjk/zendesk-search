package zendesksearch.execution

import cats.effect._
import org.scalactic.TypeCheckedTripleEquals
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.{BeforeAndAfter, EitherValues}
import zendesksearch.database._

import scala.collection.mutable

class ProgramSpec extends AnyFreeSpec with Matchers with TypeCheckedTripleEquals with EitherValues with BeforeAndAfter {
  val baseUser = User(
    1,
    "http://initech.zendesk.com/api/v2/users/1.json",
    "74341f74-9c79-49d5-9611-87ef9b6eb75f",
    "Francisca Rasmussen",
    Some("Miss Coffey"),
    "2016-04-15T05:19:46 -10:00",
    true,
    Some(true),
    false,
    Some("en-AU"),
    Some("Sri Lanka"),
    "2013-08-04T01:03:27 -10:00",
    Some("coffeyrasmussen@flotonic.com"),
    "8335-422-718",
    "Don't Worry Be Happy!",
    None,
    List("Springville", "Sutton", "Hartsville/Hartley", "Diaperville"),
    true,
    "admin"
  )
  val baseTicket = Ticket(
    "436bf9b0-1147-4c0a-8439-6f79833bff5b",
    "http://initech.zendesk.com/api/v2/tickets/436bf9b0-1147-4c0a-8439-6f79833bff5b.json",
    "9210cdc9-4bee-485f-a078-35396cd74063",
    "2016-04-28T11:19:34 -10:00",
    Some("incident"),
    "A Catastrophe in Korea (North)",
    Some("Nostrud ad sit velit cupidatat laboris"),
    "high",
    "pending",
    1,
    Some(24),
    Some(700),
    List("Ohio", "Pennsylvania", "American Samoa", "Northern Mariana Islands"),
    false,
    Some("2016-07-31T02:37:50 -10:00"),
    "web"
  )
  val baseOrganization = Organization(
    101,
    "http://initech.zendesk.com/api/v2/organizations/101.json",
    "9270ed79-35eb-4a38-a46f-35725197ea8d",
    "Enthaze",
    List("kage.com", "ecratic.com", "endipin.com", "zentix.com"),
    "2016-05-21T11:10:28 -10:00",
    "MegaCorp",
    false,
    List("Fulton", "West", "Rodriguez", "Farley")
  )

  val enrichedUser = EnrichedUser(baseUser, None, Nil, Nil)
  val enrichedTicket = EnrichedTicket(baseTicket, None, None, None)
  val enrichedOrganization = EnrichedOrganization(baseOrganization, Nil, Nil)

  val enrichedUserDatabase = Database[EnrichedUser](List(enrichedUser))
  val enrichedTicketDatabase = Database[EnrichedTicket](List(enrichedTicket))
  val enrichedOrganizationDatabase = Database[EnrichedOrganization](List(enrichedOrganization))

  val outputBuffer = mutable.ListBuffer.empty[String]
  val writeOutput = (string: String) => IO(outputBuffer.addOne(string)).void

  val baseProgram = Program(
    ProgramShowSearchOptions,
    enrichedUserDatabase,
    enrichedTicketDatabase,
    enrichedOrganizationDatabase,
    writeOutput
  )

  before { outputBuffer.clear() }

  "displaying the search options" - {
    val currentProgram = baseProgram

    "should print the appropriate message" in {
      currentProgram.tick.attempt.unsafeRunSync()

      outputBuffer.lastOption should ===(
        Some(
          """Select search options:
          |* Press 1 to search Zendesk
          |* Press 2 to view a list of searchable fields
          |* Type 'quit' to exit
          |""".stripMargin
        )
      )
    }

    "when 1 is received as input" - {
      "should progress to search type selection" in {
        val handler = currentProgram.tick.attempt.unsafeRunSync().value
        val expectedProgram = currentProgram.copy(programStage = ProgramSearching(SearchQueryingType))

        handler("1").attempt.unsafeRunSync() should ===(Right(expectedProgram))
      }
    }

    "when 2 is received as input" - {
      "should print the list of searchable fields" in {
        val handler = currentProgram.tick.attempt.unsafeRunSync().value

        handler("2").attempt.unsafeRunSync()

        outputBuffer.lastOption should ===(
          Some(
            """Search Users with:
            |
            |_id
            |active
            |alias
            |created_at
            |email
            |external_id
            |last_login_at
            |locale
            |name
            |organization_id
            |phone
            |role
            |shared
            |signature
            |suspended
            |tags
            |timezone
            |url
            |verified
            |------------------------------------------
            |Search Tickets with:
            |
            |_id
            |assignee_id
            |created_at
            |description
            |due_at
            |external_id
            |has_incidents
            |organization_id
            |priority
            |status
            |subject
            |submitter_id
            |tags
            |type
            |url
            |via
            |------------------------------------------
            |Search Organizations with:
            |
            |_id
            |created_at
            |details
            |domain_names
            |external_id
            |name
            |shared_tickets
            |tags
            |url
            |""".stripMargin
          )
        )
      }

      "should progress back to displaying the search options" in {
        val handler = currentProgram.tick.attempt.unsafeRunSync().value

        handler("2").attempt.unsafeRunSync() should ===(Right(currentProgram))
      }
    }

    "when something else is received as input" - {
      "should progress back to displaying the search options" in {
        val handler = currentProgram.tick.attempt.unsafeRunSync().value

        handler("foo").attempt.unsafeRunSync() should ===(Right(currentProgram))
      }
    }
  }

  "querying for search type" - {
    val currentProgram = baseProgram.copy(programStage = ProgramSearching(SearchQueryingType))

    "should print the appropriate message" in {
      currentProgram.tick.attempt.unsafeRunSync()

      outputBuffer.lastOption should ===(Some("Select 1) Users or 2) Tickets or 3) Organizations"))
    }

    "when 1 is received as input" - {
      "should progress to querying for user search field" in {
        val handler = currentProgram.tick.attempt.unsafeRunSync().value
        val expectedProgram = currentProgram.copy(programStage = ProgramSearching(SearchQueryingField(SearchUser)))

        handler("1").attempt.unsafeRunSync() should ===(Right(expectedProgram))
      }
    }

    "when 2 is received as input" - {
      "should progress to querying for ticket search field" in {
        val handler = currentProgram.tick.attempt.unsafeRunSync().value
        val expectedProgram = currentProgram.copy(programStage = ProgramSearching(SearchQueryingField(SearchTicket)))

        handler("2").attempt.unsafeRunSync() should ===(Right(expectedProgram))
      }
    }

    "when 3 is received as input" - {
      "should progress to querying for organization search field" in {
        val handler = currentProgram.tick.attempt.unsafeRunSync().value
        val expectedProgram =
          currentProgram.copy(programStage = ProgramSearching(SearchQueryingField(SearchOrganization)))

        handler("3").attempt.unsafeRunSync() should ===(Right(expectedProgram))
      }
    }

    "when something else is received as input" - {
      "should progress back to querying for search type" in {
        val handler = currentProgram.tick.attempt.unsafeRunSync().value

        handler("foo").attempt.unsafeRunSync() should ===(Right(currentProgram))
      }
    }
  }

  "querying for search field" - {
    val currentProgram = baseProgram.copy(programStage = ProgramSearching(SearchQueryingField(SearchUser)))

    "should print the appropriate message" in {
      currentProgram.tick.attempt.unsafeRunSync()

      outputBuffer.lastOption should ===(Some("Enter search field"))
    }

    "when a non-empty search field is received as input" - {
      "should progress to querying for search value" in {
        val handler = currentProgram.tick.attempt.unsafeRunSync().value
        val expectedProgram =
          currentProgram.copy(programStage = ProgramSearching(SearchQueryingValue(SearchUser, "_id")))

        handler("_id").attempt.unsafeRunSync() should ===(Right(expectedProgram))
      }
    }

    "when an empty search field is received as input" - {
      "should print an error message" in {
        val handler = currentProgram.tick.attempt.unsafeRunSync().value

        handler("").attempt.unsafeRunSync()

        outputBuffer.lastOption should ===(Some("Search field cannot be empty"))
      }

      "should progress back to querying for search field" in {
        val handler = currentProgram.tick.attempt.unsafeRunSync().value

        handler("").attempt.unsafeRunSync() should ===(Right(currentProgram))
      }
    }
  }

  "querying for search value" - {
    val currentProgram = baseProgram.copy(programStage = ProgramSearching(SearchQueryingValue(SearchUser, "_id")))

    "should print the appropriate message" in {
      currentProgram.tick.attempt.unsafeRunSync()

      outputBuffer.lastOption should ===(Some("Enter search value"))
    }

    "when the search field exists" - {
      "when a non-empty search value is received as input" - {
        "when there are matches for that search value" - {
          "should print the matches" in {
            val handler = currentProgram.tick.attempt.unsafeRunSync().value

            handler(enrichedUser.user._id.toString).attempt.unsafeRunSync()

            outputBuffer.lastOption should ===(
              Some(
                """1 result(s) found with _id: '1'
                |┌───────────────────────────────────────┬──────────────────────────────────────┐
                |│_id                                    │1                                     │
                |├───────────────────────────────────────┼──────────────────────────────────────┤
                |│url                                    │http://initech.zendesk.com/api/v2/user│
                |│                                       │s/1.json                              │
                |├───────────────────────────────────────┼──────────────────────────────────────┤
                |│external_id                            │74341f74-9c79-49d5-9611-87ef9b6eb75f  │
                |├───────────────────────────────────────┼──────────────────────────────────────┤
                |│name                                   │Francisca Rasmussen                   │
                |├───────────────────────────────────────┼──────────────────────────────────────┤
                |│alias                                  │Miss Coffey                           │
                |├───────────────────────────────────────┼──────────────────────────────────────┤
                |│created_at                             │2016-04-15T05:19:46 -10:00            │
                |├───────────────────────────────────────┼──────────────────────────────────────┤
                |│active                                 │true                                  │
                |├───────────────────────────────────────┼──────────────────────────────────────┤
                |│verified                               │true                                  │
                |├───────────────────────────────────────┼──────────────────────────────────────┤
                |│shared                                 │false                                 │
                |├───────────────────────────────────────┼──────────────────────────────────────┤
                |│locale                                 │en-AU                                 │
                |├───────────────────────────────────────┼──────────────────────────────────────┤
                |│timezone                               │Sri Lanka                             │
                |├───────────────────────────────────────┼──────────────────────────────────────┤
                |│last_login_at                          │2013-08-04T01:03:27 -10:00            │
                |├───────────────────────────────────────┼──────────────────────────────────────┤
                |│email                                  │coffeyrasmussen@flotonic.com          │
                |├───────────────────────────────────────┼──────────────────────────────────────┤
                |│phone                                  │8335-422-718                          │
                |├───────────────────────────────────────┼──────────────────────────────────────┤
                |│signature                              │Don't Worry Be Happy!                 │
                |├───────────────────────────────────────┼──────────────────────────────────────┤
                |│organization_id                        │<n/a>                                 │
                |├───────────────────────────────────────┼──────────────────────────────────────┤
                |│tags                                   │"Springville","Sutton","Hartsville/Har│
                |│                                       │tley","Diaperville"                   │
                |├───────────────────────────────────────┼──────────────────────────────────────┤
                |│suspended                              │true                                  │
                |├───────────────────────────────────────┼──────────────────────────────────────┤
                |│role                                   │admin                                 │
                |├───────────────────────────────────────┼──────────────────────────────────────┤
                |│organization_name                      │<n/a>                                 │
                |└───────────────────────────────────────┴──────────────────────────────────────┘
                |""".stripMargin
              )
            )
          }
        }

        "when there are no matches for that search value" - {
          "should print no search results" in {
            val handler = currentProgram.tick.attempt.unsafeRunSync().value

            handler("9000").attempt.unsafeRunSync()

            outputBuffer.lastOption should ===(Some("0 result(s) found with _id: '9000'\n\n"))
          }
        }
      }

      "when an empty search value is received as input" - {
        "when there are records that are missing a value for that search field" - {
          "should print those records" in {
            val program =
              currentProgram.copy(programStage = ProgramSearching(SearchQueryingValue(SearchUser, "organization_id")))
            val handler = program.tick.attempt.unsafeRunSync().value

            handler("").attempt.unsafeRunSync()

            outputBuffer.lastOption should ===(
              Some(
                """1 result(s) found with organization_id: ''
                |┌───────────────────────────────────────┬──────────────────────────────────────┐
                |│_id                                    │1                                     │
                |├───────────────────────────────────────┼──────────────────────────────────────┤
                |│url                                    │http://initech.zendesk.com/api/v2/user│
                |│                                       │s/1.json                              │
                |├───────────────────────────────────────┼──────────────────────────────────────┤
                |│external_id                            │74341f74-9c79-49d5-9611-87ef9b6eb75f  │
                |├───────────────────────────────────────┼──────────────────────────────────────┤
                |│name                                   │Francisca Rasmussen                   │
                |├───────────────────────────────────────┼──────────────────────────────────────┤
                |│alias                                  │Miss Coffey                           │
                |├───────────────────────────────────────┼──────────────────────────────────────┤
                |│created_at                             │2016-04-15T05:19:46 -10:00            │
                |├───────────────────────────────────────┼──────────────────────────────────────┤
                |│active                                 │true                                  │
                |├───────────────────────────────────────┼──────────────────────────────────────┤
                |│verified                               │true                                  │
                |├───────────────────────────────────────┼──────────────────────────────────────┤
                |│shared                                 │false                                 │
                |├───────────────────────────────────────┼──────────────────────────────────────┤
                |│locale                                 │en-AU                                 │
                |├───────────────────────────────────────┼──────────────────────────────────────┤
                |│timezone                               │Sri Lanka                             │
                |├───────────────────────────────────────┼──────────────────────────────────────┤
                |│last_login_at                          │2013-08-04T01:03:27 -10:00            │
                |├───────────────────────────────────────┼──────────────────────────────────────┤
                |│email                                  │coffeyrasmussen@flotonic.com          │
                |├───────────────────────────────────────┼──────────────────────────────────────┤
                |│phone                                  │8335-422-718                          │
                |├───────────────────────────────────────┼──────────────────────────────────────┤
                |│signature                              │Don't Worry Be Happy!                 │
                |├───────────────────────────────────────┼──────────────────────────────────────┤
                |│organization_id                        │<n/a>                                 │
                |├───────────────────────────────────────┼──────────────────────────────────────┤
                |│tags                                   │"Springville","Sutton","Hartsville/Har│
                |│                                       │tley","Diaperville"                   │
                |├───────────────────────────────────────┼──────────────────────────────────────┤
                |│suspended                              │true                                  │
                |├───────────────────────────────────────┼──────────────────────────────────────┤
                |│role                                   │admin                                 │
                |├───────────────────────────────────────┼──────────────────────────────────────┤
                |│organization_name                      │<n/a>                                 │
                |└───────────────────────────────────────┴──────────────────────────────────────┘
                |""".stripMargin
              )
            )
          }
        }

        "when there are no records that are missing a value for that search field" - {
          "should print no search results" in {
            val program =
              currentProgram.copy(programStage = ProgramSearching(SearchQueryingValue(SearchTicket, "organization_id")))
            val handler = program.tick.attempt.unsafeRunSync().value

            handler("").attempt.unsafeRunSync()

            outputBuffer.lastOption should ===(Some("0 result(s) found with organization_id: ''\n\n"))
          }
        }
      }
    }

    "when the search field does not exist" - {
      "should print no search results" in {
        val program = currentProgram.copy(programStage = ProgramSearching(SearchQueryingValue(SearchUser, "foo")))
        val handler = program.tick.attempt.unsafeRunSync().value

        handler("1").attempt.unsafeRunSync()

        outputBuffer.lastOption should ===(Some("0 result(s) found with foo: '1'\n\n"))
      }
    }

    "should progress back to displaying the search options" in {
      val handler = currentProgram.tick.attempt.unsafeRunSync().value

      handler("1").attempt.unsafeRunSync() should ===(
        Right(currentProgram.copy(programStage = ProgramShowSearchOptions))
      )
    }
  }
}
