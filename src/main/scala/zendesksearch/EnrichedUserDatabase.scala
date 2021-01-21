package zendesksearch

case class EnrichedUserDatabase(enrichedUsers: List[EnrichedUser]) {
  private val index: Map[String, Map[String, List[EnrichedUser]]] = Map(
    "_id" -> enrichedUsers.groupBy(_.user._id.toString),
    "url" -> enrichedUsers.groupBy(_.user.url),
    "external_id" -> enrichedUsers.groupBy(_.user.externalId),
    "name" -> enrichedUsers.groupBy(_.user.name),
    "alias" -> enrichedUsers.groupBy(_.user.alias.getOrElse("")),
    "created_at" -> enrichedUsers.groupBy(_.user.createdAt),
    "active" -> enrichedUsers.groupBy(_.user.active.toString),
    "verified" -> enrichedUsers.groupBy(_.user.verified.fold("")(_.toString)),
    "shared" -> enrichedUsers.groupBy(_.user.shared.toString),
    "locale" -> enrichedUsers.groupBy(_.user.locale.getOrElse("")),
    "timezone" -> enrichedUsers.groupBy(_.user.timezone.getOrElse("")),
    "last_login_at" -> enrichedUsers.groupBy(_.user.lastLoginAt),
    "email" -> enrichedUsers.groupBy(_.user.email.getOrElse("")),
    "phone" -> enrichedUsers.groupBy(_.user.phone),
    "signature" -> enrichedUsers.groupBy(_.user.signature),
    "organization_id" -> enrichedUsers.groupBy(_.user.organizationId.fold("")(_.toString)),
    "tags" -> enrichedUsers.groupBy(_.user.tags.sorted.map(tag => s""""$tag"""").mkString("[", ",", "]")),
    "suspended" -> enrichedUsers.groupBy(_.user.suspended.toString),
    "role" -> enrichedUsers.groupBy(_.user.role)
  )

  def search(searchTerm: String, searchValue: String): List[EnrichedUser] =
    (for {
      searchValues <- index.get(searchTerm)
      enrichedUsers <- searchValues.get(searchValue)
    } yield enrichedUsers).toList.flatten

  def searchFields: List[String] = index.keys.toList.sorted
}
