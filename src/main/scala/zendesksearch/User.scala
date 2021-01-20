package zendesksearch

import io.circe.Decoder
import io.circe.generic.extras.Configuration
import io.circe.generic.extras.semiauto._

case class User(
  _id: Int,
  url: String,
  externalId: String,
  name: String,
  alias: Option[String],
  createdAt: String,
  active: Boolean,
  verified: Option[Boolean],
  shared: Boolean,
  locale: Option[String],
  timezone: Option[String],
  lastLoginAt: String,
  email: Option[String],
  phone: String,
  signature: String,
  organizationId: Option[Int],
  tags: List[String],
  suspended: Boolean,
  role: String
)

object User {
  implicit val jsonConfig: Configuration = Configuration.default.withSnakeCaseMemberNames
  implicit val jsonDecoder: Decoder[User] = deriveConfiguredDecoder
}
