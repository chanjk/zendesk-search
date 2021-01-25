package zendesksearch.database

case class EnrichedOrganization(organization: Organization, users: List[User], tickets: List[Ticket])

object EnrichedOrganization {
  def enrichAll(
    organizations: List[Organization],
    users: List[User],
    tickets: List[Ticket]
  ): List[EnrichedOrganization] = {
    val usersByOrganizationId: Map[Int, List[User]] =
      users.groupBy(_.organizationId).collect { case (Some(id), user) => (id, user) }
    val ticketsByOrganizationId: Map[Int, List[Ticket]] =
      tickets.groupBy(_.organizationId).collect { case (Some(id), ticket) => (id, ticket) }

    organizations.map(organization =>
      EnrichedOrganization(
        organization,
        usersByOrganizationId.get(organization._id).getOrElse(Nil),
        ticketsByOrganizationId.get(organization._id).getOrElse(Nil)
      )
    )
  }

  implicit val indexable: Indexable[EnrichedOrganization] = { case EnrichedOrganization(organization, _, _) =>
    Map(
      "_id" -> Set(organization._id.toString),
      "url" -> Set(organization.url),
      "external_id" -> Set(organization.externalId),
      "name" -> Set(organization.name),
      "domain_names" -> organization.domainNames.toSet,
      "created_at" -> Set(organization.createdAt),
      "details" -> Set(organization.details),
      "shared_tickets" -> Set(organization.sharedTickets.toString),
      "tags" -> organization.tags.toSet
    )
  }

  implicit val renderable: Renderable[EnrichedOrganization] = {
    case EnrichedOrganization(organization, users, tickets) => {
      val organizationFields = List(
        "_id" -> Some(organization._id.toString),
        "url" -> Some(organization.url),
        "external_id" -> Some(organization.externalId),
        "name" -> Some(organization.name),
        "domain_names" -> Some(organization.domainNames.map(name => s""""$name"""").mkString(",")),
        "created_at" -> Some(organization.createdAt),
        "details" -> Some(organization.details),
        "shared_tickets" -> Some(organization.sharedTickets.toString),
        "tags" -> Some(organization.tags.map(tag => s""""$tag"""").mkString(","))
      )
      val usersFields = users.zipWithIndex.map { case (user, index) => s"user_name_$index" -> Some(user.name) }
      val ticketsFields = tickets.zipWithIndex.map { case (ticket, index) =>
        s"ticket_subject_$index" -> Some(ticket.subject)
      }

      organizationFields ++ usersFields ++ ticketsFields
    }
  }
}
