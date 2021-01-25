package zendesksearch.database

import io.circe.literal._
import org.scalactic.TypeCheckedTripleEquals
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

class OrganizationSpec extends AnyFreeSpec with Matchers with TypeCheckedTripleEquals {
  "decoding a JSON object representing an organization" - {
    "when the object has all required fields" - {
      "should produce an organization" in {
        val json = json"""
          {
            "_id": 101,
            "url": "http://initech.zendesk.com/api/v2/organizations/101.json",
            "external_id": "9270ed79-35eb-4a38-a46f-35725197ea8d",
            "name": "Enthaze",
            "domain_names": [
              "kage.com",
              "ecratic.com",
              "endipin.com",
              "zentix.com"
            ],
            "created_at": "2016-05-21T11:10:28 -10:00",
            "details": "MegaCorp",
            "shared_tickets": false,
            "tags": [
              "Fulton",
              "West",
              "Rodriguez",
              "Farley"
            ]
          }
        """

        val expectedOrganization = Organization(
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

        json.as[Organization] should ===(Right(expectedOrganization))
      }
    }

    "when the object is missing required fields" - {
      "should produce an error" in {
        val json = json"""
          {
            "name": "Enthaze"
          }
        """

        json.as[Organization].isLeft should ===(true)
      }
    }
  }
}
