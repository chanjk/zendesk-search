package zendesksearch

import org.scalactic.TypeCheckedTripleEquals
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

class EnrichedUserDatabaseSpec extends AnyFreeSpec with Matchers with TypeCheckedTripleEquals {
  val user1 = User(
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
  val user2 = user1.copy(_id = 2, externalId = "c9995ea4-ff72-46e0-ab77-dfe0ae1ef6c2")
  val userWithNoAlias = user1.copy(_id = 3, externalId = "85c599c1-ebab-474d-a4e6-32f1c06e8730", alias = None)
  val userWithNoVerified = user1.copy(_id = 4, externalId = "37c9aef5-cf01-4b07-af24-c6c49ac1d1c7", verified = None)
  val userWithNoLocale = user1.copy(_id = 5, externalId = "29c18801-fb42-433d-8674-f37d63e637df", locale = None)
  val userWithNoTimezone = user1.copy(_id = 6, externalId = "ed106e63-396d-4d16-ae49-d3dd37049ba3", timezone = None)
  val userWithNoEmail = user1.copy(_id = 7, externalId = "bce94e82-b4f4-438f-bc0b-2440e8265705", email = None)
  val userWithNoOrganizationId =
    user1.copy(_id = 8, externalId = "fa13ffa4-0ba1-41d1-be4a-c1e7a92f25e4", organizationId = None)

  val organization = Organization(
    119,
    "http://initech.zendesk.com/api/v2/organizations/119.json",
    "9270ed79-35eb-4a38-a46f-35725197ea8d",
    "Enthaze",
    List("kage.com", "ecratic.com", "endipin.com", "zentix.com"),
    "2016-05-21T11:10:28 -10:00",
    "MegaCorp",
    false,
    List("Fulton", "West", "Rodriguez", "Farley")
  )

  val enrichedUser1 = EnrichedUser(user1, Some(organization), Nil, Nil)
  val enrichedUser2 = enrichedUser1.copy(user = user2)
  val enrichedUserWithNoAlias = enrichedUser1.copy(user = userWithNoAlias)
  val enrichedUserWithNoVerified = enrichedUser1.copy(user = userWithNoVerified)
  val enrichedUserWithNoLocale = enrichedUser1.copy(user = userWithNoLocale)
  val enrichedUserWithNoTimezone = enrichedUser1.copy(user = userWithNoTimezone)
  val enrichedUserWithNoEmail = enrichedUser1.copy(user = userWithNoEmail)
  val enrichedUserWithNoOrganizationId = EnrichedUser(userWithNoOrganizationId, None, Nil, Nil)

  val allEnrichedUsers = List(
    enrichedUser1,
    enrichedUser2,
    enrichedUserWithNoAlias,
    enrichedUserWithNoVerified,
    enrichedUserWithNoLocale,
    enrichedUserWithNoTimezone,
    enrichedUserWithNoEmail,
    enrichedUserWithNoOrganizationId
  )
  val database = new EnrichedUserDatabase(allEnrichedUsers)

  "searching" - {
    case class SearchTestCase(
      searchTerm: String,
      matchingSearchValue: String,
      matches: List[EnrichedUser],
      nonMatchingSearchValue: String,
      emptySearchValueMatches: Option[List[EnrichedUser]]
    )

    val testCases = List(
      SearchTestCase("_id", "1", List(enrichedUser1), "9999", None),
      SearchTestCase("url", "http://initech.zendesk.com/api/v2/users/1.json", allEnrichedUsers, "http://foo.com", None),
      SearchTestCase(
        "external_id",
        "74341f74-9c79-49d5-9611-87ef9b6eb75f",
        List(enrichedUser1),
        "00000000-0000-0000-0000-000000000000",
        None
      ),
      SearchTestCase("name", "Francisca Rasmussen", allEnrichedUsers, "Foo", None),
      SearchTestCase(
        "alias",
        "Miss Coffey",
        allEnrichedUsers.filterNot(_ == enrichedUserWithNoAlias),
        "Bar",
        Some(List(enrichedUserWithNoAlias))
      ),
      SearchTestCase("created_at", "2016-04-15T05:19:46 -10:00", allEnrichedUsers, "9999-01-01T00:00:00 +00:00", None),
      SearchTestCase("active", "true", allEnrichedUsers, "false", None),
      SearchTestCase(
        "verified",
        "true",
        allEnrichedUsers.filterNot(_ == enrichedUserWithNoVerified),
        "false",
        Some(List(enrichedUserWithNoVerified))
      ),
      SearchTestCase("shared", "false", allEnrichedUsers, "true", None),
      SearchTestCase(
        "locale",
        "en-AU",
        allEnrichedUsers.filterNot(_ == enrichedUserWithNoLocale),
        "en-GB",
        Some(List(enrichedUserWithNoLocale))
      ),
      SearchTestCase(
        "timezone",
        "Sri Lanka",
        allEnrichedUsers.filterNot(_ == enrichedUserWithNoTimezone),
        "Melbourne",
        Some(List(enrichedUserWithNoTimezone))
      ),
      SearchTestCase("last_login_at", "2013-08-04T01:03:27 -10:00", allEnrichedUsers, "9999-01-01T00:00:00 +00:00", None),
      SearchTestCase(
        "email",
        "coffeyrasmussen@flotonic.com",
        allEnrichedUsers.filterNot(_ == enrichedUserWithNoEmail),
        "test@test.com",
        Some(List(enrichedUserWithNoEmail))
      ),
      SearchTestCase("phone", "8335-422-718", allEnrichedUsers, "0000-000-000", None),
      SearchTestCase("signature", "Don't Worry Be Happy!", allEnrichedUsers, "Hello World!", None),
      SearchTestCase(
        "organization_id",
        "119",
        allEnrichedUsers.filterNot(_ == enrichedUserWithNoOrganizationId),
        "911",
        Some(List(enrichedUserWithNoOrganizationId))
      ),
      SearchTestCase(
        "tags",
        """["Diaperville","Hartsville/Hartley","Springville","Sutton"]""",
        allEnrichedUsers,
        """["Foo"]""",
        None
      ),
      SearchTestCase("suspended", "true", allEnrichedUsers, "false", None),
      SearchTestCase("role", "admin", allEnrichedUsers, "member", None)
    )

    testCases.foreach { testCase =>
      s"on ${testCase.searchTerm}" - {
        "when there are matches" - {
          "should produce the list of matches" in {
            database.search(testCase.searchTerm, testCase.matchingSearchValue) should ===(testCase.matches)
          }
        }

        "when there are no matches" - {
          "should produce an empty list" in {
            database.search(testCase.searchTerm, testCase.nonMatchingSearchValue) should ===(Nil)
          }
        }

        testCase.emptySearchValueMatches.foreach { emptyValueMatches =>
          "when the search value is an empty string" - {
            s"should produce the list of matches with no ${testCase.searchTerm}" in {
              database.search(testCase.searchTerm, "") should ===(emptyValueMatches)
            }
          }
        }
      }
    }
  }

  "list of search fields" - {
    "should be complete" in {
      database.searchFields should ===(
        List(
          "_id",
          "active",
          "alias",
          "created_at",
          "email",
          "external_id",
          "last_login_at",
          "locale",
          "name",
          "organization_id",
          "phone",
          "role",
          "shared",
          "signature",
          "suspended",
          "tags",
          "timezone",
          "url",
          "verified"
        )
      )
    }
  }
}
