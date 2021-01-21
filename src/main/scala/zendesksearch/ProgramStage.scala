package zendesksearch

sealed trait ProgramStage
case object ProgramShowSearchOptions extends ProgramStage
case class ProgramSearchActive(stage: SearchStage) extends ProgramStage
