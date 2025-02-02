package tanukkii.reactivezk

import org.apache.zookeeper.AsyncCallback.ChildrenCallback
import scala.jdk.CollectionConverters._

trait ChildrenCallbackConversion {
  implicit def toChildrenCallback[Ctx](f: (Int, String, Ctx, List[String]) => Unit): ChildrenCallback = new ChildrenCallback {
    def processResult(rc: Int, path: String, ctx: Any, children: java.util.List[String]): Unit = {
      f(rc, path, ctx.asInstanceOf[Ctx], Option(children).fold(List.empty[String])(_.asScala.toList)) //children may be null
    }
  }
}

object ChildrenCallbackConversion extends ChildrenCallbackConversion
