package zendesksearch

sealed trait SearchStage
case object SearchQueryingType extends SearchStage
case class SearchQueryingTerm(searchType: SearchType) extends SearchStage
case class SearchQueryingValue(searchType: SearchType, searchTerm: String) extends SearchStage
