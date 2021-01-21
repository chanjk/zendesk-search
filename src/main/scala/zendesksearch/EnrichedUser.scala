package zendesksearch

case class EnrichedUser(user: User, organization: Option[Organization])

object EnrichedUser {
  def enrichAll(users: List[User], organizations: List[Organization]): List[EnrichedUser] = {
    val organizationMap: Map[Int, Organization] = organizations.map(org => (org._id, org)).toMap

    users.map(user => EnrichedUser(user, user.organizationId.flatMap(organizationMap.get)))
  }
}
