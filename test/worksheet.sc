import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
import scalaz._
import scala.concurrent.ExecutionContext.Implicits.global

def convert[V](of: Option[Future[V]]): Future[Option[V]] = {
  Traverse[Option].sequence(of)
}
Await.result(convert(Some(Future{3})), 2 seconds)