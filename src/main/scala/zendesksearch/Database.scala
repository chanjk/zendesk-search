package zendesksearch

import zendesksearch.Indexable._

case class Database[A : Indexable](records: List[A]) {
  private val index: Index[A] = Indexable[A].apply(records)

  val searchFields: List[SearchField] = index.keys.toList.sorted

  def search(searchField: SearchField, searchValue: Option[SearchValue]): List[A] =
    (for {
      searchValues <- index.get(searchField)
      results <- searchValues.get(searchValue)
    } yield results).toList.flatten
}
