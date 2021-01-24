package zendesksearch

import zendesksearch.Indexable._

trait Indexable[A] {
  def apply(value: A): Map[SearchField, Set[SearchValue]]
}

object Indexable {
  type SearchField = String
  type SearchValue = String
  type Index[A] = Map[SearchField, Map[Option[SearchValue], List[A]]]

  def apply[A](implicit indexable: Indexable[A]): Indexable[A] = indexable
}
