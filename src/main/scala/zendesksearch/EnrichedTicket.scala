package zendesksearch

case class EnrichedTicket(
  ticket: Ticket,
  organization: Option[Organization],
  submitter: Option[User],
  assignee: Option[User]
)

object EnrichedTicket {
  def enrichAll(tickets: List[Ticket], organizations: List[Organization], users: List[User]): List[EnrichedTicket] = {
    val organizationById: Map[Int, Organization] = organizations.map(org => (org._id, org)).toMap
    val userById: Map[Int, User] = users.map(user => (user._id, user)).toMap

    tickets.map(ticket =>
      EnrichedTicket(
        ticket,
        ticket.organizationId.flatMap(organizationById.get),
        userById.get(ticket.submitterId),
        ticket.assigneeId.flatMap(userById.get)
      )
    )
  }

  implicit val indexable: Indexable[EnrichedTicket] = { case EnrichedTicket(ticket, _, _, _) =>
    Map(
      "_id" -> Set(ticket._id),
      "url" -> Set(ticket.url),
      "external_id" -> Set(ticket.externalId),
      "created_at" -> Set(ticket.createdAt),
      "type" -> ticket.`type`.toSet,
      "subject" -> Set(ticket.subject),
      "description" -> ticket.description.toSet,
      "priority" -> Set(ticket.priority),
      "status" -> Set(ticket.status),
      "submitter_id" -> Set(ticket.submitterId.toString),
      "assignee_id" -> ticket.assigneeId.map(_.toString).toSet,
      "organization_id" -> ticket.organizationId.map(_.toString).toSet,
      "tags" -> ticket.tags.toSet,
      "has_incidents" -> Set(ticket.hasIncidents.toString),
      "due_at" -> ticket.dueAt.toSet,
      "via" -> Set(ticket.via)
    )
  }

  implicit val renderable: Renderable[EnrichedTicket] = {
    case EnrichedTicket(ticket, organization, submitter, assignee) => {
      val ticketFields = List(
        "_id" -> Some(ticket._id),
        "url" -> Some(ticket.url),
        "external_id" -> Some(ticket.externalId),
        "created_at" -> Some(ticket.createdAt),
        "type" -> ticket.`type`,
        "subject" -> Some(ticket.subject),
        "description" -> ticket.description,
        "priority" -> Some(ticket.priority),
        "status" -> Some(ticket.status),
        "submitter_id" -> Some(ticket.submitterId.toString),
        "assignee_id" -> ticket.assigneeId.map(_.toString),
        "organization_id" -> ticket.organizationId.map(_.toString),
        "tags" -> Some(ticket.tags.map(tag => s""""$tag"""").mkString(",")),
        "has_incidents" -> Some(ticket.hasIncidents.toString),
        "due_at" -> ticket.dueAt,
        "via" -> Some(ticket.via)
      )
      val organizationFields = List("organization_name" -> organization.map(_.name))
      val submitterFields = List("submitter_name" -> submitter.map(_.name))
      val assigneeFields = List("assignee_name" -> assignee.map(_.name))

      ticketFields ++ organizationFields ++ submitterFields ++ assigneeFields
    }
  }
}
