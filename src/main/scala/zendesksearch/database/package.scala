package zendesksearch

package object database {
  type SearchField = String
  type SearchValue = String
  type Index[A] = Map[SearchField, Map[Option[SearchValue], List[A]]]
}
