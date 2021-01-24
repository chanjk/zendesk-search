package zendesksearch

import org.scalactic.TypeCheckedTripleEquals
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

class EnrichedOrganizationSpec extends AnyFreeSpec with Matchers with TypeCheckedTripleEquals {
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
    Some(baseOrganization._id),
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
    Some(baseOrganization._id),
    List("Ohio", "Pennsylvania", "American Samoa", "Northern Mariana Islands"),
    false,
    Some("2016-07-31T02:37:50 -10:00"),
    "web"
  )

  "enriching organizations" - {
    "that have users" - {
      "should produce organizations enriched with their corresponding users" in {
        val organization1 = baseOrganization
        val organization2 = baseOrganization.copy(_id = baseOrganization._id + 1)
        val allOrganizations = List(organization1, organization2)

        val user1 = baseUser
        val user2 = baseUser
        val user3 = baseUser.copy(organizationId = Some(organization2._id))
        val allUsers = List(user1, user2, user3)

        EnrichedOrganization.enrichAll(allOrganizations, allUsers, Nil) should contain theSameElementsAs List(
          EnrichedOrganization(organization1, List(user1, user2), Nil),
          EnrichedOrganization(organization2, List(user3), Nil)
        )
      }
    }

    "that have no users" - {
      "should produce organizations enriched with no users" in {
        val organizations = List.fill(3)(baseOrganization)
        val unrelatedUser = baseUser.copy(organizationId = Some(baseOrganization._id + 1))
        val expectedEnrichedOrganizations = organizations.map(EnrichedOrganization(_, Nil, Nil))

        EnrichedOrganization.enrichAll(
          organizations,
          List(unrelatedUser),
          Nil
        ) should contain theSameElementsAs expectedEnrichedOrganizations
      }
    }

    "that have tickets" - {
      "should produce organizations enriched with their corresponding tickets" in {
        val organization1 = baseOrganization
        val organization2 = baseOrganization.copy(_id = baseOrganization._id + 1)
        val allOrganizations = List(organization1, organization2)

        val ticket1 = baseTicket
        val ticket2 = baseTicket
        val ticket3 = baseTicket.copy(organizationId = Some(organization2._id))
        val allTickets = List(ticket1, ticket2, ticket3)

        EnrichedOrganization.enrichAll(allOrganizations, Nil, allTickets) should contain theSameElementsAs List(
          EnrichedOrganization(organization1, Nil, List(ticket1, ticket2)),
          EnrichedOrganization(organization2, Nil, List(ticket3))
        )
      }
    }

    "that have no tickets" - {
      "should produce organizations enriched with no tickets" in {
        val organizations = List.fill(3)(baseOrganization)
        val unrelatedTicket = baseTicket.copy(organizationId = Some(baseOrganization._id + 1))
        val expectedEnrichedOrganizations = organizations.map(EnrichedOrganization(_, Nil, Nil))

        EnrichedOrganization.enrichAll(
          organizations,
          Nil,
          List(unrelatedTicket)
        ) should contain theSameElementsAs expectedEnrichedOrganizations
      }
    }
  }

  "the Indexable instance" - {
    "should define the correct fields and values for searching" in {
      val fieldsToValues = Indexable[EnrichedOrganization].apply(
        EnrichedOrganization(baseOrganization, List(baseUser), List(baseTicket))
      )

      fieldsToValues should ===(
        Map(
          "_id" -> Set("101"),
          "url" -> Set("http://initech.zendesk.com/api/v2/organizations/101.json"),
          "external_id" -> Set("9270ed79-35eb-4a38-a46f-35725197ea8d"),
          "name" -> Set("Enthaze"),
          "domain_names" -> Set("kage.com", "ecratic.com", "endipin.com", "zentix.com"),
          "created_at" -> Set("2016-05-21T11:10:28 -10:00"),
          "details" -> Set("MegaCorp"),
          "shared_tickets" -> Set("false"),
          "tags" -> Set("Fulton", "West", "Rodriguez", "Farley")
        )
      )
    }
  }

  "the Renderable instance" - {
    "should define the correct fields and values for rendering" in {
      val fieldsAndValues = Renderable[EnrichedOrganization].apply(
        EnrichedOrganization(baseOrganization, List(baseUser), List(baseTicket))
      )

      fieldsAndValues should ===(
        List(
          "_id" -> Some("101"),
          "url" -> Some("http://initech.zendesk.com/api/v2/organizations/101.json"),
          "external_id" -> Some("9270ed79-35eb-4a38-a46f-35725197ea8d"),
          "name" -> Some("Enthaze"),
          "domain_names" -> Some(""""kage.com","ecratic.com","endipin.com","zentix.com""""),
          "created_at" -> Some("2016-05-21T11:10:28 -10:00"),
          "details" -> Some("MegaCorp"),
          "shared_tickets" -> Some("false"),
          "tags" -> Some(""""Fulton","West","Rodriguez","Farley""""),
          "user_0" -> Some("Francisca Rasmussen"),
          "ticket_0" -> Some("A Catastrophe in Korea (North)")
        )
      )
    }
  }
}
