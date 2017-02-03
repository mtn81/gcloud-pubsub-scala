import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

object Hoge extends App {

  var c = 0
  def hoge: Future[Unit] = Future {
    c = c + 1
    println(c, Thread.currentThread().getName)
//    Thread.sleep(100)
    hoge
  }

  hoge

  while(true) Thread.sleep(100000)

}
