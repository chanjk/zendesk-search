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

  implicit val indexable: Indexable[EnrichedUser] = Indexable.fromIndexer(
    Map(
      "_id" -> (enrichedUser => Set(enrichedUser.user._id.toString)),
      "url" -> (enrichedUser => Set(enrichedUser.user.url)),
      "external_id" -> (enrichedUser => Set(enrichedUser.user.externalId)),
      "name" -> (enrichedUser => Set(enrichedUser.user.name)),
      "alias" -> (enrichedUser => enrichedUser.user.alias.toSet),
      "created_at" -> (enrichedUser => Set(enrichedUser.user.createdAt)),
      "active" -> (enrichedUser => Set(enrichedUser.user.active.toString)),
      "verified" -> (enrichedUser => enrichedUser.user.verified.map(_.toString).toSet),
      "shared" -> (enrichedUser => Set(enrichedUser.user.shared.toString)),
      "locale" -> (enrichedUser => enrichedUser.user.locale.toSet),
      "timezone" -> (enrichedUser => enrichedUser.user.timezone.toSet),
      "last_login_at" -> (enrichedUser => Set(enrichedUser.user.lastLoginAt)),
      "email" -> (enrichedUser => enrichedUser.user.email.toSet),
      "phone" -> (enrichedUser => Set(enrichedUser.user.phone)),
      "signature" -> (enrichedUser => Set(enrichedUser.user.signature)),
      "organization_id" -> (enrichedUser => enrichedUser.user.organizationId.map(_.toString).toSet),
      "tags" -> (enrichedUser => enrichedUser.user.tags.toSet),
      "suspended" -> (enrichedUser => Set(enrichedUser.user.suspended.toString)),
      "role" -> (enrichedUser => Set(enrichedUser.user.role))
    )
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
