package zendesksearch.execution

import de.vandermeer.asciitable.AsciiTable
import zendesksearch.database.Renderable

import scala.util.chaining._

object ResultRenderer {
  def render[A: Renderable](result: A): String = {
    val fieldsAndValues = Renderable[A].apply(result)

    // AsciiTable's addXYZ methods mutate the receiver, and do not return the updated object
    val table = fieldsAndValues.foldLeft(new AsciiTable().tap(_.addRule()))((tableInProgress, fieldAndValue) =>
      tableInProgress
        .tap(_.addRow(fieldAndValue._1, fieldAndValue._2.getOrElse("<n/a>")))
        .tap(_.addRule())
    )

    table.render()
  }
}
