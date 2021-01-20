package zendesksearch

case class UserDatabase(users: List[User]) {
  private val index: Map[String, Map[String, List[User]]] = Map(
    "_id" -> users.groupBy(_._id.toString),
    "url" -> users.groupBy(_.url),
    "external_id" -> users.groupBy(_.externalId),
    "name" -> users.groupBy(_.name),
    "alias" -> users.groupBy(_.alias.getOrElse("")),
    "created_at" -> users.groupBy(_.createdAt),
    "active" -> users.groupBy(_.active.toString),
    "verified" -> users.groupBy(_.verified.fold("")(_.toString)),
    "shared" -> users.groupBy(_.shared.toString),
    "locale" -> users.groupBy(_.locale.getOrElse("")),
    "timezone" -> users.groupBy(_.timezone.getOrElse("")),
    "last_login_at" -> users.groupBy(_.lastLoginAt),
    "email" -> users.groupBy(_.email.getOrElse("")),
    "phone" -> users.groupBy(_.phone),
    "signature" -> users.groupBy(_.signature),
    "organization_id" -> users.groupBy(_.organizationId.fold("")(_.toString)),
    "tags" -> users.groupBy(_.tags.sorted.map(tag => s""""$tag"""").mkString("[", ",", "]")),
    "suspended" -> users.groupBy(_.suspended.toString),
    "role" -> users.groupBy(_.role)
  )

  def search(searchTerm: String, searchValue: String): List[User] =
    (for {
      searchValues <- index.get(searchTerm)
      users <- searchValues.get(searchValue)
    } yield users).toList.flatten

  def searchFields: List[String] = index.keys.toList.sorted
}
