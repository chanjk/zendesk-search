package zendesksearch.database

import io.circe.Decoder
import io.circe.generic.extras.Configuration
import io.circe.generic.extras.semiauto.deriveConfiguredDecoder

case class Ticket(
  _id: String,
  url: String,
  externalId: String,
  createdAt: String,
  `type`: Option[String],
  subject: String,
  description: Option[String],
  priority: String,
  status: String,
  submitterId: Int,
  assigneeId: Option[Int],
  organizationId: Option[Int],
  tags: List[String],
  hasIncidents: Boolean,
  dueAt: Option[String],
  via: String
)

object Ticket {
  implicit val jsonConfig: Configuration = Configuration.default.withSnakeCaseMemberNames
  implicit val jsonDecoder: Decoder[Ticket] = deriveConfiguredDecoder
}
