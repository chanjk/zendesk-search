package zendesksearch

import org.scalactic.TypeCheckedTripleEquals
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

class UserDatabaseSpec extends AnyFreeSpec with Matchers with TypeCheckedTripleEquals {
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

  val allUsers = List(
    user1,
    user2,
    userWithNoAlias,
    userWithNoVerified,
    userWithNoLocale,
    userWithNoTimezone,
    userWithNoEmail,
    userWithNoOrganizationId
  )
  val database = new UserDatabase(allUsers)

  "searching" - {
    case class SearchTestCase(
      searchTerm: String,
      matchingSearchValue: String,
      matches: List[User],
      nonMatchingSearchValue: String,
      emptySearchValueMatches: Option[List[User]]
    )

    val testCases = List(
      SearchTestCase("_id", "1", List(user1), "9999", None),
      SearchTestCase("url", "http://initech.zendesk.com/api/v2/users/1.json", allUsers, "http://foo.com", None),
      SearchTestCase(
        "external_id",
        "74341f74-9c79-49d5-9611-87ef9b6eb75f",
        List(user1),
        "00000000-0000-0000-0000-000000000000",
        None
      ),
      SearchTestCase("name", "Francisca Rasmussen", allUsers, "Foo", None),
      SearchTestCase(
        "alias",
        "Miss Coffey",
        allUsers.filterNot(_ == userWithNoAlias),
        "Bar",
        Some(List(userWithNoAlias))
      ),
      SearchTestCase("created_at", "2016-04-15T05:19:46 -10:00", allUsers, "9999-01-01T00:00:00 +00:00", None),
      SearchTestCase("active", "true", allUsers, "false", None),
      SearchTestCase(
        "verified",
        "true",
        allUsers.filterNot(_ == userWithNoVerified),
        "false",
        Some(List(userWithNoVerified))
      ),
      SearchTestCase("shared", "false", allUsers, "true", None),
      SearchTestCase(
        "locale",
        "en-AU",
        allUsers.filterNot(_ == userWithNoLocale),
        "en-GB",
        Some(List(userWithNoLocale))
      ),
      SearchTestCase(
        "timezone",
        "Sri Lanka",
        allUsers.filterNot(_ == userWithNoTimezone),
        "Melbourne",
        Some(List(userWithNoTimezone))
      ),
      SearchTestCase("last_login_at", "2013-08-04T01:03:27 -10:00", allUsers, "9999-01-01T00:00:00 +00:00", None),
      SearchTestCase(
        "email",
        "coffeyrasmussen@flotonic.com",
        allUsers.filterNot(_ == userWithNoEmail),
        "test@test.com",
        Some(List(userWithNoEmail))
      ),
      SearchTestCase("phone", "8335-422-718", allUsers, "0000-000-000", None),
      SearchTestCase("signature", "Don't Worry Be Happy!", allUsers, "Hello World!", None),
      SearchTestCase(
        "organization_id",
        "119",
        allUsers.filterNot(_ == userWithNoOrganizationId),
        "911",
        Some(List(userWithNoOrganizationId))
      ),
      SearchTestCase(
        "tags",
        """["Diaperville","Hartsville/Hartley","Springville","Sutton"]""",
        allUsers,
        """["Foo"]""",
        None
      ),
      SearchTestCase("suspended", "true", allUsers, "false", None),
      SearchTestCase("role", "admin", allUsers, "member", None)
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
