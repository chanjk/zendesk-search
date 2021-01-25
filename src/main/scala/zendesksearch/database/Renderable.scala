package zendesksearch.database

trait Renderable[A] {
  def apply(value: A): List[(String, Option[String])]
}

object Renderable {
  def apply[A](implicit renderable: Renderable[A]): Renderable[A] = renderable
}
