package tanukkii.reactivezk

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestKit}
import org.apache.zookeeper.{KeeperException, CreateMode}
import org.apache.zookeeper.ZooDefs.Ids
import org.scalatest.funsuite.AnyFunSuiteLike
import scala.concurrent.duration._

class ZooKeeperSessionActorTest extends TestKit(ActorSystem("ZooKeeperSessionActorTest"))
  with AnyFunSuiteLike with ZooKeeperTest with ImplicitSender with StopSystemAfterAll {

  val dataDir: String = "target/zookeeper/ZooKeeperSessionActorTest"

  test("create znode") {

    val zooKeeperActor = system.actorOf(ZooKeeperSessionActor.props(zkConnectString, 10 seconds))

    zooKeeperActor ! ZKOperations.Create("/test-create", "test data".getBytes(), Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT)

    expectMsg(ZKOperations.Created("/test-create", "/test-create", NoContext))
  }

  test("get data of znode") {

    val zooKeeperActor = system.actorOf(ZooKeeperSessionActor.props(zkConnectString, 10 seconds))

    zooKeeperActor ! ZKOperations.Create("/test-get-data", "test data".getBytes(), Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT)

    expectMsg(ZKOperations.Created("/test-get-data", "/test-get-data", NoContext))

    zooKeeperActor ! ZKOperations.GetData("/test-get-data")

    val result = expectMsgType[ZKOperations.DataGot]
    assert(result.path === "/test-get-data")
    assert(result.data  === "test data".getBytes())
  }

  test("set data of znode") {

    val zooKeeperActor = system.actorOf(ZooKeeperSessionActor.props(zkConnectString, 10 seconds))

    zooKeeperActor ! ZKOperations.Create("/test-set-data", "test data".getBytes(), Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT)

    expectMsg(ZKOperations.Created("/test-set-data", "/test-set-data", NoContext))

    zooKeeperActor ! ZKOperations.GetData("/test-set-data")

    val result = expectMsgType[ZKOperations.DataGot]
    assert(result.path === "/test-set-data")
    assert(result.data === "test data".getBytes())
    assert(result.stat.getVersion === 0)

    zooKeeperActor ! ZKOperations.SetData("/test-set-data", "modified data".getBytes(), 0)

    assert(expectMsgType[ZKOperations.DataSet].path === "/test-set-data")

    zooKeeperActor ! ZKOperations.GetData("/test-set-data")

    val modified = expectMsgType[ZKOperations.DataGot]
    assert(modified.path === "/test-set-data")
    assert(modified.data === "modified data".getBytes())
    assert(modified.stat.getVersion === 1)
  }

  test("check existence of znode") {

    val zooKeeperActor = system.actorOf(ZooKeeperSessionActor.props(zkConnectString, 10 seconds))

    zooKeeperActor ! ZKOperations.Create("/test-exists", "test data".getBytes(), Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT)

    expectMsg(ZKOperations.Created("/test-exists", "/test-exists", NoContext))

    zooKeeperActor ! ZKOperations.Exists("/test-exists")

    val result = expectMsgType[ZKOperations.DoesExist]
    assert(result.path === "/test-exists")
    assert(result.stat.isDefined === true)
  }

  test("get children of znode") {

    val zooKeeperActor = system.actorOf(ZooKeeperSessionActor.props(zkConnectString, 10 seconds))

    zooKeeperActor ! ZKOperations.Create("/test-get-children", "test data".getBytes(), Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT)

    expectMsg(ZKOperations.Created("/test-get-children", "/test-get-children", NoContext))

    0 to 2 foreach { idx =>
      zooKeeperActor ! ZKOperations.Create(s"/test-get-children/$idx", "test data".getBytes(), Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT)
    }

    expectMsgAllOf(
      ZKOperations.Created("/test-get-children/0", "/test-get-children/0", NoContext),
      ZKOperations.Created("/test-get-children/1", "/test-get-children/1", NoContext),
      ZKOperations.Created("/test-get-children/2", "/test-get-children/2", NoContext)
    )

    zooKeeperActor ! ZKOperations.GetChildren("/test-get-children")

    val result = expectMsgType[ZKOperations.ChildrenGot]
    assert(result.path === "/test-get-children")
    assert(result.children === List("0", "1", "2"))
  }

  test("delete znode") {

    val zooKeeperActor = system.actorOf(ZooKeeperSessionActor.props(zkConnectString, 10 seconds))

    zooKeeperActor ! ZKOperations.Create("/test-delete", "test data".getBytes(), Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT)

    expectMsg(ZKOperations.Created("/test-delete", "/test-delete", NoContext))

    zooKeeperActor ! ZKOperations.GetData("/test-delete")

    val result = expectMsgType[ZKOperations.DataGot]
    assert(result.path ===  "/test-delete")
    assert(result.data === "test data".getBytes())

    zooKeeperActor ! ZKOperations.Delete("/test-delete", 0)

    expectMsg(ZKOperations.Deleted("/test-delete", NoContext))

    zooKeeperActor ! ZKOperations.GetData("/test-delete")

    val deleted = expectMsgType[ZKOperations.GetDataFailure]
    assert(deleted.path === "/test-delete")
    assert(deleted.error.code() === KeeperException.Code.NONODE)
  }

  test("restart session") {
    val zooKeeperActor = system.actorOf(ZooKeeperSessionActor.props(zkConnectString, 10 seconds))

    zooKeeperActor ! ZooKeeperSession.Restart
    expectMsg(ZooKeeperSession.Restarted)

    zooKeeperActor ! ZKOperations.Create("/test-restart", "test data".getBytes(), Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT)
    expectMsg(ZKOperations.Created("/test-restart", "/test-restart", NoContext))
  }
}
