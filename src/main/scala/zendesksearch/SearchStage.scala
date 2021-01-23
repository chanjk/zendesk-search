package zendesksearch

sealed trait SearchStage
case object SearchQueryingType extends SearchStage
case class SearchQueryingField(searchType: SearchType) extends SearchStage
case class SearchQueryingValue(searchType: SearchType, searchField: String) extends SearchStage
