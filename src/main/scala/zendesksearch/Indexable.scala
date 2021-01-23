package zendesksearch

import cats.Monoid
import zendesksearch.Indexable.Index

trait Indexable[A] {
  def apply(items: List[A]): Index[A]
}

object Indexable {
  type SearchField = String
  type SearchValue = String
  type Index[A] = Map[SearchField, Map[Option[SearchValue], List[A]]]
  type Indexer[A] = Map[SearchField, A => Set[SearchValue]]

  def apply[A](implicit indexable: Indexable[A]): Indexable[A] = indexable

  def fromIndexer[A](indexer: Indexer[A]): Indexable[A] = items =>
    indexer.map { case (searchField, discriminator) =>
      searchField -> {
        val condensedMap: Map[Set[SearchValue], List[A]] = items.groupBy(discriminator)
        val expandedMaps: Iterable[Map[Option[SearchValue], List[A]]] = condensedMap.map {
          case (searchValues, searchResults) => {
            val mappings: Set[(Option[SearchValue], List[A])] =
              if (searchValues.isEmpty) Set(None -> searchResults)
              else searchValues.map(value => Some(value) -> searchResults)

            Map.from(mappings)
          }
        }

        // combine all the maps, concatenating the results for identical keys
        Monoid[Map[Option[SearchValue], List[A]]].combineAll(expandedMaps)
      }
    }
}
