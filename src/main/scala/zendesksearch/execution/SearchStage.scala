package zendesksearch.execution

import zendesksearch.database.SearchField

sealed trait SearchStage
case object SearchQueryingType extends SearchStage
case class SearchQueryingField(searchType: SearchType) extends SearchStage
case class SearchQueryingValue(searchType: SearchType, searchField: SearchField) extends SearchStage
