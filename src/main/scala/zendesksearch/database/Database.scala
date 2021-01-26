package zendesksearch.database

import cats.Monoid

case class Database[A: Indexable](records: List[A]) {
  private val index: Index[A] = {
    val expandedMappings: List[Index[A]] = records.map(record => {
      val mapping: Map[SearchField, Set[SearchValue]] = Indexable[A].apply(record)
      val recordList: List[A] = List(record)

      mapping.map { case (searchField, searchValues) =>
        searchField -> Map.from {
          if (searchValues.isEmpty) Set(None -> recordList)
          else searchValues.map(Some(_) -> recordList)
        }
      }
    })

    // recursively combine all the mappings, concatenating any lists belonging to identical keys
    Monoid[Index[A]].combineAll(expandedMappings)
  }

  val searchFields: List[SearchField] = index.keys.toList.sorted

  def search(searchField: SearchField, searchValue: Option[SearchValue]): List[A] =
    (for {
      searchValues <- index.get(searchField)
      results <- searchValues.get(searchValue)
    } yield results).toList.flatten
}
