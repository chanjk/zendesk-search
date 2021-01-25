package zendesksearch.database

import org.scalactic.TypeCheckedTripleEquals
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

class EnrichedTicketSpec extends AnyFreeSpec with Matchers with TypeCheckedTripleEquals {
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
  val baseSubmitter = User(
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
    Some(119),
    List("Springville", "Sutton", "Hartsville/Hartley", "Diaperville"),
    true,
    "admin"
  )
  val baseAssignee = User(
    100,
    "http://initech.zendesk.com/api/v2/users/100.json",
    "c9995ea4-ff72-46e0-ab77-dfe0ae1ef6c2",
    "Cross Barlow",
    Some("Miss Joni"),
    "2016-06-23T10:31:39 -10:00",
    true,
    Some(true),
    false,
    Some("zh-CN"),
    Some("Armenia"),
    "2012-04-12T04:03:28 -10:00",
    Some("jonibarlow@flotonic.com"),
    "9575-552-585",
    "Don't Worry Be Happy!",
    Some(106),
    List("Foxworth", "Woodlands", "Herlong", "Henrietta"),
    false,
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
    baseSubmitter._id,
    Some(baseAssignee._id),
    Some(baseOrganization._id),
    List("Ohio", "Pennsylvania", "American Samoa", "Northern Mariana Islands"),
    false,
    Some("2016-07-31T02:37:50 -10:00"),
    "web"
  )

  "enriching tickets" - {
    "that belong to the same given organization" - {
      "should produce tickets enriched with that organization" in {
        val tickets = List.fill(3)(baseTicket)
        val expectedEnrichedTickets = tickets.map(EnrichedTicket(_, Some(baseOrganization), None, None))

        EnrichedTicket.enrichAll(
          tickets,
          List(baseOrganization),
          Nil
        ) should contain theSameElementsAs expectedEnrichedTickets
      }
    }

    "that do not belong to any organization" - {
      "should produce tickets enriched with no organization" in {
        val tickets = List.fill(3)(baseTicket.copy(organizationId = None))
        val expectedEnrichedTickets = tickets.map(EnrichedTicket(_, None, None, None))

        EnrichedTicket.enrichAll(
          tickets,
          List(baseOrganization),
          Nil
        ) should contain theSameElementsAs expectedEnrichedTickets
      }
    }

    "that belong to different organizations" - {
      "should produce tickets enriched with the correct organizations" in {
        val organization1 = baseOrganization
        val organization2 = baseOrganization.copy(_id = baseOrganization._id + 1)
        val allOrganizations = List(organization1, organization2)

        val ticket1 = baseTicket
        val ticket2 = baseTicket
        val ticket3 = baseTicket.copy(organizationId = Some(organization2._id))
        val allTickets = List(ticket1, ticket2, ticket3)

        EnrichedTicket.enrichAll(allTickets, allOrganizations, Nil) should contain theSameElementsAs List(
          EnrichedTicket(ticket1, Some(organization1), None, None),
          EnrichedTicket(ticket2, Some(organization1), None, None),
          EnrichedTicket(ticket3, Some(organization2), None, None)
        )
      }
    }

    "that belong to an organization that is not part of the given list" - {
      "should produce tickets enriched with no organization" in {
        val tickets = List.fill(3)(baseTicket.copy(organizationId = Some(baseOrganization._id + 1)))
        val expectedEnrichedTickets = tickets.map(EnrichedTicket(_, None, None, None))

        EnrichedTicket.enrichAll(
          tickets,
          List(baseOrganization),
          Nil
        ) should contain theSameElementsAs expectedEnrichedTickets
      }
    }

    "that have the same submitter" - {
      "should produce tickets enriched with that submitter" in {
        val tickets = List.fill(3)(baseTicket)
        val expectedEnrichedTickets = tickets.map(EnrichedTicket(_, None, Some(baseSubmitter), None))

        EnrichedTicket.enrichAll(
          tickets,
          Nil,
          List(baseSubmitter)
        ) should contain theSameElementsAs expectedEnrichedTickets
      }
    }

    "that have different submitters" - {
      "should produce tickets enriched with the correct submitters" in {
        val submitter1 = baseSubmitter
        val submitter2 = baseSubmitter.copy(_id = baseSubmitter._id + 1)
        val allSubmitters = List(submitter1, submitter2)

        val ticket1 = baseTicket
        val ticket2 = baseTicket
        val ticket3 = baseTicket.copy(submitterId = submitter2._id)
        val allTickets = List(ticket1, ticket2, ticket3)

        EnrichedTicket.enrichAll(allTickets, Nil, allSubmitters) should contain theSameElementsAs List(
          EnrichedTicket(ticket1, None, Some(submitter1), None),
          EnrichedTicket(ticket2, None, Some(submitter1), None),
          EnrichedTicket(ticket3, None, Some(submitter2), None)
        )
      }
    }

    "that have a submitter that is not part of the given list" - {
      "should produce tickets enriched with no submitter" in {
        val tickets = List.fill(3)(baseTicket.copy(submitterId = baseSubmitter._id + 1))
        val expectedEnrichedTickets = tickets.map(EnrichedTicket(_, None, None, None))

        EnrichedTicket.enrichAll(
          tickets,
          Nil,
          List(baseSubmitter)
        ) should contain theSameElementsAs expectedEnrichedTickets
      }
    }

    "that have the same assignee" - {
      "should produce tickets enriched with that assignee" in {
        val tickets = List.fill(3)(baseTicket)
        val expectedEnrichedTickets = tickets.map(EnrichedTicket(_, None, None, Some(baseAssignee)))

        EnrichedTicket.enrichAll(
          tickets,
          Nil,
          List(baseAssignee)
        ) should contain theSameElementsAs expectedEnrichedTickets
      }
    }

    "that do not have any assignee" - {
      "should produce tickets enriched with no assignee" in {
        val tickets = List.fill(3)(baseTicket.copy(assigneeId = None))
        val expectedEnrichedTickets = tickets.map(EnrichedTicket(_, None, None, None))

        EnrichedTicket.enrichAll(
          tickets,
          Nil,
          List(baseAssignee)
        ) should contain theSameElementsAs expectedEnrichedTickets
      }
    }

    "that have different assignees" - {
      "should produce tickets enriched with the correct assignees" in {
        val assignee1 = baseAssignee
        val assignee2 = baseAssignee.copy(_id = baseAssignee._id + 1)
        val allAssignees = List(assignee1, assignee2)

        val ticket1 = baseTicket
        val ticket2 = baseTicket
        val ticket3 = baseTicket.copy(assigneeId = Some(assignee2._id))
        val allTickets = List(ticket1, ticket2, ticket3)

        EnrichedTicket.enrichAll(allTickets, Nil, allAssignees) should contain theSameElementsAs List(
          EnrichedTicket(ticket1, None, None, Some(assignee1)),
          EnrichedTicket(ticket2, None, None, Some(assignee1)),
          EnrichedTicket(ticket3, None, None, Some(assignee2))
        )
      }
    }

    "that have an assignee that is not part of the given list" - {
      "should produce tickets enriched with no assignee" in {
        val tickets = List.fill(3)(baseTicket.copy(assigneeId = Some(baseAssignee._id + 1)))
        val expectedEnrichedTickets = tickets.map(EnrichedTicket(_, None, None, None))

        EnrichedTicket.enrichAll(
          tickets,
          Nil,
          List(baseAssignee)
        ) should contain theSameElementsAs expectedEnrichedTickets
      }
    }
  }

  "the Indexable instance" - {
    "should define the correct fields and values for searching" in {
      val fieldsToValues = Indexable[EnrichedTicket].apply(
        EnrichedTicket(baseTicket, Some(baseOrganization), Some(baseSubmitter), Some(baseAssignee))
      )

      fieldsToValues should ===(
        Map(
          "_id" -> Set("436bf9b0-1147-4c0a-8439-6f79833bff5b"),
          "url" -> Set("http://initech.zendesk.com/api/v2/tickets/436bf9b0-1147-4c0a-8439-6f79833bff5b.json"),
          "external_id" -> Set("9210cdc9-4bee-485f-a078-35396cd74063"),
          "created_at" -> Set("2016-04-28T11:19:34 -10:00"),
          "type" -> Set("incident"),
          "subject" -> Set("A Catastrophe in Korea (North)"),
          "description" -> Set("Nostrud ad sit velit cupidatat laboris"),
          "priority" -> Set("high"),
          "status" -> Set("pending"),
          "submitter_id" -> Set("1"),
          "assignee_id" -> Set("100"),
          "organization_id" -> Set("101"),
          "tags" -> Set("Ohio", "Pennsylvania", "American Samoa", "Northern Mariana Islands"),
          "has_incidents" -> Set("false"),
          "due_at" -> Set("2016-07-31T02:37:50 -10:00"),
          "via" -> Set("web")
        )
      )
    }
  }

  "the Renderable instance" - {
    "should define the correct fields and values for rendering" in {
      val fieldsAndValues = Renderable[EnrichedTicket].apply(
        EnrichedTicket(baseTicket, Some(baseOrganization), Some(baseSubmitter), Some(baseAssignee))
      )

      fieldsAndValues should ===(
        List(
          "_id" -> Some("436bf9b0-1147-4c0a-8439-6f79833bff5b"),
          "url" -> Some("http://initech.zendesk.com/api/v2/tickets/436bf9b0-1147-4c0a-8439-6f79833bff5b.json"),
          "external_id" -> Some("9210cdc9-4bee-485f-a078-35396cd74063"),
          "created_at" -> Some("2016-04-28T11:19:34 -10:00"),
          "type" -> Some("incident"),
          "subject" -> Some("A Catastrophe in Korea (North)"),
          "description" -> Some("Nostrud ad sit velit cupidatat laboris"),
          "priority" -> Some("high"),
          "status" -> Some("pending"),
          "submitter_id" -> Some("1"),
          "assignee_id" -> Some("100"),
          "organization_id" -> Some("101"),
          "tags" -> Some(""""Ohio","Pennsylvania","American Samoa","Northern Mariana Islands""""),
          "has_incidents" -> Some("false"),
          "due_at" -> Some("2016-07-31T02:37:50 -10:00"),
          "via" -> Some("web"),
          "organization_name" -> Some("Enthaze"),
          "submitter_name" -> Some("Francisca Rasmussen"),
          "assignee_name" -> Some("Cross Barlow")
        )
      )
    }
  }
}
