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
}
