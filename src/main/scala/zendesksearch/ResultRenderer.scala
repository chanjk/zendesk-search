package zendesksearch

import de.vandermeer.asciitable.AsciiTable

import scala.util.chaining._

object ResultRenderer {
  def render[A: Renderable](result: A): String = {
    val fieldsAndValues = Renderable[A].apply(result)
    val table = fieldsAndValues.foldLeft(new AsciiTable().tap(_.addRule()))((table, fieldAndValue) =>
      table.tap(_.addRow(fieldAndValue._1, fieldAndValue._2.getOrElse("<n/a>"))).tap(_.addRule())
    )

    table.render()
  }
}
