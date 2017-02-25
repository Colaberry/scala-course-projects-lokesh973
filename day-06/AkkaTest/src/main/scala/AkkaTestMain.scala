/**
  * Created by lokesh0973 on 2/15/2017.
  */
object AkkaTestMain {
  def main(args: Array[String]): Unit = {
    val actorName =classOf[AkkaSender].getName()

    akka.Main.main(Array(actorName))
  }
}
