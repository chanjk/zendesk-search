package zendesksearch

class TextSearchDatabase(users: List[User]) {
  private val usersMap: Map[String, Map[String, List[User]]] = Map(
    "_id" -> users.groupBy(_._id.toString),
    "url" -> users.groupBy(_.url),
    "external_id" -> users.groupBy(_.externalId),
    "name" -> users.groupBy(_.name),
    "alias" -> users.groupBy(_.alias),
    "created_at" -> users.groupBy(_.createdAt),
    "active" -> users.groupBy(_.active.toString),
    "verified" -> users.groupBy(_.verified.toString),
    "shared" -> users.groupBy(_.shared.toString),
    "locale" -> users.groupBy(_.locale),
    "timezone" -> users.groupBy(_.timezone),
    "last_login_at" -> users.groupBy(_.lastLoginAt),
    "email" -> users.groupBy(_.email),
    "phone" -> users.groupBy(_.phone),
    "signature" -> users.groupBy(_.signature),
    "organization_id" -> users.groupBy(_.organizationId.toString),
    "tags" -> users.groupBy(_.tags.sorted.map(tag => s""""$tag"""").mkString("[", ",", "]")),
    "suspended" -> users.groupBy(_.suspended.toString),
    "role" -> users.groupBy(_.role)
  )

  def userSearchFields: List[String] = usersMap.keys.toList.sorted
}
