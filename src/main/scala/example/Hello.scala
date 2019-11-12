package example

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.event.Logging
import akka.pattern.{BackoffOpts, BackoffSupervisor}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Sink, Source}

object Hello extends App {
  implicit val actorSystem = ActorSystem("test")
  implicit val ec = actorSystem.dispatcher
  implicit val mat = ActorMaterializer()

  class MyActor extends Actor {
    val log = Logging(context.system, this)
    Source(List(1,2,3,4,5)).map{v =>
      println(s"$v arrived from stream")
      if (v == 4) {
      throw new IllegalArgumentException("4 on stream")
    } else v}.runWith(Sink.actorRefWithAck(self, "initMessage", "ackMessage", "completeMessage"))
    var counter: Int = 0

    def receive = {
      case "initMessage" =>
        println("init message arrived ")
        sender() ! "ackMessage"
      case _ =>
        sender() ! "ackMessage"

    }

    override def preRestart(reason: Throwable, message: Option[Any]): Unit = {
      log.info("executing pre restart")
      super.preRestart(reason, message)
    }
  }

  import scala.concurrent.duration._

  val props = BackoffSupervisor.props(BackoffOpts.onFailure(
    Props[MyActor],
    "supervised-onfailure",
    1.second,
    10.second,
    0.2
  ).withAutoReset(10 second))

  val r = actorSystem.actorOf(props)



//  val testActorRef: ActorRef =  actorSystem.actorOf(Props[MyActor])
//  val testActorSupervisedProps: Props = BackoffActorSupervisor.props(Props[MyActor], "supervisedActor")
//
//
//  val testActorSupervised = actorSystem.actorOf(testActorSupervisedProps)
//
//  testActorRef ! "increment"
//  testActorRef ! "increment"
//  testActorRef ! "increment"
//  testActorRef ! "increment"
//  testActorRef ! "bullshit"
//  testActorRef ! "increment"
//  testActorRef ! "increment"
//  testActorRef ! "increment"
//
//  testActorSupervised ! "increment"
//  testActorSupervised ! "increment"
//  testActorSupervised ! "increment"
//  testActorSupervised ! "increment"
//  testActorSupervised ! "bullshit"
//  Thread.sleep(2000)
//  testActorSupervised ! "increment"
//  Thread.sleep(2000)
//  testActorSupervised ! "increment"
//  Thread.sleep(2000)
//  testActorSupervised ! "increment"

}
