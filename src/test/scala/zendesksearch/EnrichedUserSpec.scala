package zendesksearch

import org.scalactic.TypeCheckedTripleEquals
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

class EnrichedUserSpec extends AnyFreeSpec with Matchers with TypeCheckedTripleEquals {
  val baseOrganization = Organization(
    1,
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
  val baseSubmittedTicket = Ticket(
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
    Some(116),
    List("Ohio", "Pennsylvania", "American Samoa", "Northern Mariana Islands"),
    false,
    Some("2016-07-31T02:37:50 -10:00"),
    "web"
  )
  val baseAssignedTicket = Ticket(
    "1a227508-9f39-427c-8f57-1b72f3fab87c",
    "http://initech.zendesk.com/api/v2/tickets/1a227508-9f39-427c-8f57-1b72f3fab87c.json",
    "3e5ca820-cd1f-4a02-a18f-11b18e7bb49a",
    "2016-04-14T08:32:31 -10:00",
    Some("incident"),
    "A Catastrophe in Micronesia",
    Some("Aliquip excepteur fugiat ex minim ea aute eu labore"),
    "low",
    "hold",
    71,
    Some(1),
    Some(112),
    List("Puerto Rico", "Idaho", "Oklahoma", "Louisiana"),
    false,
    Some("2016-08-15T05:37:32 -10:00"),
    "chat"
  )

  "enriching users" - {
    "that belong to the same given organization" - {
      "should produce users enriched with that organization" in {
        val users = List.fill(3)(baseUser)
        val expectedEnrichedUsers = users.map(EnrichedUser(_, Some(baseOrganization), Nil, Nil))

        EnrichedUser.enrichAll(
          users,
          List(baseOrganization),
          Nil
        ) should contain theSameElementsAs expectedEnrichedUsers
      }
    }

    "that do not belong to any organization" - {
      "should produce users enriched with no organization" in {
        val users = List.fill(3)(baseUser.copy(organizationId = None))
        val expectedEnrichedUsers = users.map(EnrichedUser(_, None, Nil, Nil))

        EnrichedUser.enrichAll(
          users,
          List(baseOrganization),
          Nil
        ) should contain theSameElementsAs expectedEnrichedUsers
      }
    }

    "that belong to different organizations" - {
      "should produce users enriched with the correct organizations" in {
        val organization1 = baseOrganization
        val organization2 = baseOrganization.copy(_id = baseOrganization._id + 1)
        val allOrganizations = List(organization1, organization2)

        val user1 = baseUser
        val user2 = baseUser
        val user3 = baseUser.copy(organizationId = Some(organization2._id))
        val allUsers = List(user1, user2, user3)

        EnrichedUser.enrichAll(allUsers, allOrganizations, Nil) should contain theSameElementsAs List(
          EnrichedUser(user1, Some(organization1), Nil, Nil),
          EnrichedUser(user2, Some(organization1), Nil, Nil),
          EnrichedUser(user3, Some(organization2), Nil, Nil)
        )
      }
    }

    "that belong to an organization that is not part of the given list" - {
      "should produce users enriched with no organization" in {
        val users = List.fill(3)(baseUser.copy(organizationId = Some(baseOrganization._id + 1)))
        val expectedEnrichedUsers = users.map(EnrichedUser(_, None, Nil, Nil))

        EnrichedUser.enrichAll(
          users,
          List(baseOrganization),
          Nil
        ) should contain theSameElementsAs expectedEnrichedUsers
      }
    }

    "that have submitted tickets" - {
      "should produce users enriched with those submitted tickets" in {
        val user1 = baseUser
        val user2 = baseUser.copy(_id = baseUser._id + 1)
        val user3 = baseUser.copy(_id = baseUser._id + 2)
        val allUsers = List(user1, user2, user3)

        val submittedTicket1 = baseSubmittedTicket
        val submittedTicket2 = baseSubmittedTicket.copy(submitterId = user2._id)
        val submittedTicket3 = baseSubmittedTicket.copy(submitterId = user3._id)
        val allTickets = List(submittedTicket1, submittedTicket2, submittedTicket3)

        EnrichedUser.enrichAll(allUsers, Nil, allTickets) should contain theSameElementsAs List(
          EnrichedUser(user1, None, List(submittedTicket1), Nil),
          EnrichedUser(user2, None, List(submittedTicket2), Nil),
          EnrichedUser(user3, None, List(submittedTicket3), Nil)
        )
      }
    }

    "that have not submitted tickets" - {
      "should produce users enriched with no submitted tickets" in {
        val users = List.fill(3)(baseUser)
        val unrelatedSubmittedTicket = baseSubmittedTicket.copy(submitterId = baseUser._id + 1)
        val expectedEnrichedUsers = users.map(EnrichedUser(_, None, Nil, Nil))

        EnrichedUser.enrichAll(
          users,
          Nil,
          List(unrelatedSubmittedTicket)
        ) should contain theSameElementsAs expectedEnrichedUsers
      }
    }

    "that have been assigned tickets" - {
      "should produce users enriched with those assigned tickets" in {
        val user1 = baseUser
        val user2 = baseUser.copy(_id = baseUser._id + 1)
        val user3 = baseUser.copy(_id = baseUser._id + 2)
        val allUsers = List(user1, user2, user3)

        val assignedTicket1 = baseAssignedTicket
        val assignedTicket2 = baseAssignedTicket.copy(assigneeId = Some(user2._id))
        val assignedTicket3 = baseAssignedTicket.copy(assigneeId = Some(user3._id))
        val allTickets = List(assignedTicket1, assignedTicket2, assignedTicket3)

        EnrichedUser.enrichAll(allUsers, Nil, allTickets) should contain theSameElementsAs List(
          EnrichedUser(user1, None, Nil, List(assignedTicket1)),
          EnrichedUser(user2, None, Nil, List(assignedTicket2)),
          EnrichedUser(user3, None, Nil, List(assignedTicket3))
        )
      }
    }

    "that have not been assigned tickets" - {
      "should produce users enriched with no assigned tickets" in {
        val users = List.fill(3)(baseUser)
        val unrelatedAssignedTicket = baseAssignedTicket.copy(assigneeId = Some(baseUser._id + 1))
        val expectedEnrichedUsers = users.map(EnrichedUser(_, None, Nil, Nil))

        EnrichedUser.enrichAll(
          users,
          Nil,
          List(unrelatedAssignedTicket)
        ) should contain theSameElementsAs expectedEnrichedUsers
      }
    }
  }
}
