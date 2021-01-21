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

  "enriching users" - {
    "that belong to the same organization" - {
      "should produce users enriched with that organization" in {
        val users = List.fill(3)(baseUser)
        val expectedEnrichedUsers = users.map(EnrichedUser(_, Some(baseOrganization)))

        EnrichedUser.enrichAll(users, List(baseOrganization)) should contain theSameElementsAs(expectedEnrichedUsers)
      }
    }

    "that do not belong to any organization" - {
      "should produce users enriched with no organization" in {
        val users = List.fill(3)(baseUser.copy(organizationId = None))
        val expectedEnrichedUsers = users.map(EnrichedUser(_, None))

        EnrichedUser.enrichAll(users, List(baseOrganization)) should contain theSameElementsAs(expectedEnrichedUsers)
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

        EnrichedUser.enrichAll(allUsers, allOrganizations) should contain theSameElementsAs List(
          EnrichedUser(user1, Some(organization1)),
          EnrichedUser(user2, Some(organization1)),
          EnrichedUser(user3, Some(organization2))
        )
      }
    }
  }
}
