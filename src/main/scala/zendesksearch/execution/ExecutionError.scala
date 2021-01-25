package zendesksearch.execution

sealed trait ExecutionError extends Throwable {
  override def getMessage: String = this match {
    case FileDecodeError(filePath, cause) => s"Failed to decode $filePath: $cause"
  }
}
case class FileDecodeError(filePath: String, cause: String) extends ExecutionError
