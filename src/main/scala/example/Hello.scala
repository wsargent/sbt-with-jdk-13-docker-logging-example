package example

object Hello extends Greeting {
  private val logger = org.slf4j.LoggerFactory.getLogger(getClass())
  def main(args: Array[String]): Unit = {
    logger.info(greeting)
  }
}

trait Greeting {
  lazy val greeting: String = "hello"
}
