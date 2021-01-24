package zendesksearch

case class EnrichedUser(
  user: User,
  organization: Option[Organization],
  submittedTickets: List[Ticket],
  assignedTickets: List[Ticket]
)

object EnrichedUser {
  def enrichAll(users: List[User], organizations: List[Organization], tickets: List[Ticket]): List[EnrichedUser] = {
    val organizationById: Map[Int, Organization] = organizations.map(org => (org._id, org)).toMap
    val ticketsBySubmitterId: Map[Int, List[Ticket]] = tickets.groupBy(_.submitterId)
    val ticketsByAssigneeId: Map[Int, List[Ticket]] =
      tickets.groupBy(_.assigneeId).collect { case (Some(id), assignedTickets) => (id, assignedTickets) }

    users.map(user =>
      EnrichedUser(
        user,
        user.organizationId.flatMap(organizationById.get),
        ticketsBySubmitterId.get(user._id).getOrElse(Nil),
        ticketsByAssigneeId.get(user._id).getOrElse(Nil)
      )
    )
  }

  implicit val indexable: Indexable[EnrichedUser] = enrichedUser =>
    Map(
      "_id" -> Set(enrichedUser.user._id.toString),
      "url" -> Set(enrichedUser.user.url),
      "external_id" -> Set(enrichedUser.user.externalId),
      "name" -> Set(enrichedUser.user.name),
      "alias" -> enrichedUser.user.alias.toSet,
      "created_at" -> Set(enrichedUser.user.createdAt),
      "active" -> Set(enrichedUser.user.active.toString),
      "verified" -> enrichedUser.user.verified.map(_.toString).toSet,
      "shared" -> Set(enrichedUser.user.shared.toString),
      "locale" -> enrichedUser.user.locale.toSet,
      "timezone" -> enrichedUser.user.timezone.toSet,
      "last_login_at" -> Set(enrichedUser.user.lastLoginAt),
      "email" -> enrichedUser.user.email.toSet,
      "phone" -> Set(enrichedUser.user.phone),
      "signature" -> Set(enrichedUser.user.signature),
      "organization_id" -> enrichedUser.user.organizationId.map(_.toString).toSet,
      "tags" -> enrichedUser.user.tags.toSet,
      "suspended" -> Set(enrichedUser.user.suspended.toString),
      "role" -> Set(enrichedUser.user.role)
    )

  implicit val renderable: Renderable[EnrichedUser] = {
    case EnrichedUser(user, organization, submittedTickets, assignedTickets) => {
      val userFields = List(
        "_id" -> Some(user._id.toString),
        "url" -> Some(user.url),
        "external_id" -> Some(user.externalId),
        "name" -> Some(user.name),
        "alias" -> user.alias,
        "created_at" -> Some(user.createdAt),
        "active" -> Some(user.active.toString),
        "verified" -> user.verified.map(_.toString),
        "shared" -> Some(user.shared.toString),
        "locale" -> user.locale,
        "timezone" -> user.timezone,
        "last_login_at" -> Some(user.lastLoginAt),
        "email" -> user.email,
        "phone" -> Some(user.phone),
        "signature" -> Some(user.signature),
        "organization_id" -> user.organizationId.map(_.toString),
        "tags" -> Some(user.tags.map(tag => s""""$tag"""").mkString(",")),
        "suspended" -> Some(user.suspended.toString),
        "role" -> Some(user.role)
      )
      val organizationFields = List("organization_name" -> organization.map(_.name))
      val submittedTicketsFields = submittedTickets.zipWithIndex.map { case (ticket, index) =>
        s"submitted_ticket_$index" -> Some(ticket.subject)
      }
      val assignedTicketsFields = assignedTickets.zipWithIndex.map { case (ticket, index) =>
        s"assigned_ticket_$index" -> Some(ticket.subject)
      }

      userFields ++ organizationFields ++ submittedTicketsFields ++ assignedTicketsFields
    }
  }
}
