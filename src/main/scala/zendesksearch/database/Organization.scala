package zendesksearch.database

import io.circe.Decoder
import io.circe.generic.extras.Configuration
import io.circe.generic.extras.semiauto._

case class Organization(
  _id: Int,
  url: String,
  externalId: String,
  name: String,
  domainNames: List[String],
  createdAt: String,
  details: String,
  sharedTickets: Boolean,
  tags: List[String]
)

object Organization {
  implicit val jsonConfig: Configuration = Configuration.default.withSnakeCaseMemberNames
  implicit val jsonDecoder: Decoder[Organization] = deriveConfiguredDecoder
}
