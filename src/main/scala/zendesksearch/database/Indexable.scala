package zendesksearch.database

trait Indexable[A] {
  def apply(value: A): Map[SearchField, Set[SearchValue]]
}

object Indexable {
  def apply[A](implicit indexable: Indexable[A]): Indexable[A] = indexable
}
