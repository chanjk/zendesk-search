package zendesksearch

import io.circe.literal._
import org.scalactic.TypeCheckedTripleEquals
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

class TicketSpec extends AnyFreeSpec with Matchers with TypeCheckedTripleEquals {
  "decoding a JSON object representing a ticket" - {
    "when the object has all required fields" - {
      "should produce a ticket" in {
        val json = json"""
          {
            "_id": "436bf9b0-1147-4c0a-8439-6f79833bff5b",
            "url": "http://initech.zendesk.com/api/v2/tickets/436bf9b0-1147-4c0a-8439-6f79833bff5b.json",
            "external_id": "9210cdc9-4bee-485f-a078-35396cd74063",
            "created_at": "2016-04-28T11:19:34 -10:00",
            "type": "incident",
            "subject": "A Catastrophe in Korea (North)",
            "description": "Nostrud ad sit velit cupidatat laboris ipsum nisi amet laboris ex exercitation amet et proident. Ipsum fugiat aute dolore tempor nostrud velit ipsum.",
            "priority": "high",
            "status": "pending",
            "submitter_id": 38,
            "assignee_id": 24,
            "organization_id": 116,
            "tags": [
              "Ohio",
              "Pennsylvania",
              "American Samoa",
              "Northern Mariana Islands"
            ],
            "has_incidents": false,
            "due_at": "2016-07-31T02:37:50 -10:00",
            "via": "web"
          }
        """

        val expectedTicket = Ticket(
          "436bf9b0-1147-4c0a-8439-6f79833bff5b",
          "http://initech.zendesk.com/api/v2/tickets/436bf9b0-1147-4c0a-8439-6f79833bff5b.json",
          "9210cdc9-4bee-485f-a078-35396cd74063",
          "2016-04-28T11:19:34 -10:00",
          Some("incident"),
          "A Catastrophe in Korea (North)",
          Some("Nostrud ad sit velit cupidatat laboris ipsum nisi amet laboris ex exercitation amet et proident. Ipsum fugiat aute dolore tempor nostrud velit ipsum."),
          "high",
          "pending",
          38,
          Some(24),
          Some(116),
          List("Ohio", "Pennsylvania", "American Samoa", "Northern Mariana Islands"),
          false,
          Some("2016-07-31T02:37:50 -10:00"),
          "web"
        )

        json.as[Ticket] should ===(Right(expectedTicket))
      }
    }

    "when the object is missing optional fields" - {
      "should produce a ticket" in {
        val json = json"""
          {
            "_id": "436bf9b0-1147-4c0a-8439-6f79833bff5b",
            "url": "http://initech.zendesk.com/api/v2/tickets/436bf9b0-1147-4c0a-8439-6f79833bff5b.json",
            "external_id": "9210cdc9-4bee-485f-a078-35396cd74063",
            "created_at": "2016-04-28T11:19:34 -10:00",
            "subject": "A Catastrophe in Korea (North)",
            "priority": "high",
            "status": "pending",
            "submitter_id": 38,
            "tags": [
              "Ohio",
              "Pennsylvania",
              "American Samoa",
              "Northern Mariana Islands"
            ],
            "has_incidents": false,
            "via": "web"
          }
        """

        val expectedTicket = Ticket(
          "436bf9b0-1147-4c0a-8439-6f79833bff5b",
          "http://initech.zendesk.com/api/v2/tickets/436bf9b0-1147-4c0a-8439-6f79833bff5b.json",
          "9210cdc9-4bee-485f-a078-35396cd74063",
          "2016-04-28T11:19:34 -10:00",
          None,
          "A Catastrophe in Korea (North)",
          None,
          "high",
          "pending",
          38,
          None,
          None,
          List("Ohio", "Pennsylvania", "American Samoa", "Northern Mariana Islands"),
          false,
          None,
          "web"
        )

        json.as[Ticket] should ===(Right(expectedTicket))
      }
    }

    "when the object is missing required fields" - {
      "should produce an error" in {
        val json = json"""
          {
            "subject": "A Catastrophe in Korea (North)"
          }
        """

        json.as[Ticket].isLeft should ===(true)
      }
    }
  }
}
