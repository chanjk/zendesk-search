package zendesksearch

import cats.Monoid
import zendesksearch.Indexable._

case class Database[A: Indexable](records: List[A]) {
  private val index: Index[A] = {
    val expandedMappings: List[Index[A]] = records.map(record => {
      val mapping: Map[SearchField, Set[SearchValue]] = Indexable[A].apply(record)

      mapping.map { case (searchField, searchValues) =>
        searchField -> Map.from {
          if (searchValues.isEmpty) Set(None -> List(record))
          else searchValues.map(Some(_) -> List(record))
        }
      }
    })

    // recursively combine all the mappings, concatenating the results for identical keys
    Monoid[Index[A]].combineAll(expandedMappings)
  }

  val searchFields: List[SearchField] = index.keys.toList.sorted

  def search(searchField: SearchField, searchValue: Option[SearchValue]): List[A] =
    (for {
      searchValues <- index.get(searchField)
      results <- searchValues.get(searchValue)
    } yield results).toList.flatten
}
