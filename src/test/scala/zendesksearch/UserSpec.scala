package zendesksearch

import io.circe.literal._
import org.scalactic.TypeCheckedTripleEquals
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

class UserSpec extends AnyFreeSpec with Matchers with TypeCheckedTripleEquals {
  "decoding a JSON object representing a User" - {
    "when the object has all required fields" - {
      "should produce a User" in {
        val json = json"""
          {
            "_id": 1,
            "url": "http://initech.zendesk.com/api/v2/users/1.json",
            "external_id": "74341f74-9c79-49d5-9611-87ef9b6eb75f",
            "name": "Francisca Rasmussen",
            "alias": "Miss Coffey",
            "created_at": "2016-04-15T05:19:46 -10:00",
            "active": true,
            "verified": true,
            "shared": false,
            "locale": "en-AU",
            "timezone": "Sri Lanka",
            "last_login_at": "2013-08-04T01:03:27 -10:00",
            "email": "coffeyrasmussen@flotonic.com",
            "phone": "8335-422-718",
            "signature": "Don't Worry Be Happy!",
            "organization_id": 119,
            "tags": [
              "Springville",
              "Sutton",
              "Hartsville/Hartley",
              "Diaperville"
            ],
            "suspended": true,
            "role": "admin"
          }
        """

        val expectedUser = User(
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

        json.as[User] should ===(Right(expectedUser))
      }
    }

    "when the object is missing optional fields" - {
      "should produce a User" in {
        val json = json"""
          {
            "_id": 1,
            "url": "http://initech.zendesk.com/api/v2/users/1.json",
            "external_id": "74341f74-9c79-49d5-9611-87ef9b6eb75f",
            "name": "Francisca Rasmussen",
            "created_at": "2016-04-15T05:19:46 -10:00",
            "active": true,
            "shared": false,
            "last_login_at": "2013-08-04T01:03:27 -10:00",
            "phone": "8335-422-718",
            "signature": "Don't Worry Be Happy!",
            "tags": [
              "Springville",
              "Sutton",
              "Hartsville/Hartley",
              "Diaperville"
            ],
            "suspended": true,
            "role": "admin"
          }
        """

        val expectedUser = User(
          1,
          "http://initech.zendesk.com/api/v2/users/1.json",
          "74341f74-9c79-49d5-9611-87ef9b6eb75f",
          "Francisca Rasmussen",
          None,
          "2016-04-15T05:19:46 -10:00",
          true,
          None,
          false,
          None,
          None,
          "2013-08-04T01:03:27 -10:00",
          None,
          "8335-422-718",
          "Don't Worry Be Happy!",
          None,
          List("Springville", "Sutton", "Hartsville/Hartley", "Diaperville"),
          true,
          "admin"
        )

        json.as[User] should ===(Right(expectedUser))
      }
    }

    "when the object is missing required fields" - {
      "should produce an error" in {
        val json = json"""
          {
            "alias": "Miss Coffey"
          }
        """

        json.as[User].isLeft should ===(true)
      }
    }
  }
}
