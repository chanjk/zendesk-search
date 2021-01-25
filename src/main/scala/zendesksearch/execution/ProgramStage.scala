package zendesksearch.execution

sealed trait ProgramStage
case object ProgramShowSearchOptions extends ProgramStage
case class ProgramSearching(stage: SearchStage) extends ProgramStage
