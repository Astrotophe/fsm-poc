//#full-example
package fr.canal.rocket.workflow.engine

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import fr.canal.rocket.workflow.engine.WorkflowsActor.{GetWorkflows, StartWorkflow, UnknownWorkflow}

case class Message(id: Int, processId: Int, completion: Int) extends Data

class Sender(worflowsActorRef: ActorRef)(implicit system: ActorSystem) extends Actor {
  override def receive: Receive = {
    case StartWorkflow =>
      worflowsActorRef ! StartWorkflow("Workflow1")
    case  GetWorkflows =>
      worflowsActorRef ! GetWorkflows
    case x@_ =>
      println(x)
  }
}

class Receiver extends Actor {
  override def receive: Receive = {
    case message:Message =>
      println(s"Receiver with the path ${self.path} received the message with the process id ${message.processId}")
  }




}

object AkkaQuickstart extends App {

  implicit val system: ActorSystem = ActorSystem("test")

  val workflowsActor = WorkflowsActor()

  val sender = system.actorOf(Props(new Sender(workflowsActor)), "sender")

  /*

  val receiver1 = system.actorOf(Props[Receiver], "workflow1")
  val receiver2 = system.actorOf(Props[Receiver], "workflow2")

  val message1 = Message(1,1,0)
  val message2 = Message(2,2,0)

  system.actorSelection(s"/user/workflow${message1.processId}") ! message1
  system.actorSelection(s"/user/workflow${message2.processId}") ! message2

  */

  sender ! StartWorkflow
  sender ! GetWorkflows

}
