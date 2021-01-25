package zendesksearch.execution

sealed trait SearchType
case object SearchUser extends SearchType
case object SearchTicket extends SearchType
case object SearchOrganization extends SearchType
