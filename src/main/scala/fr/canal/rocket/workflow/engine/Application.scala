//#full-example
package fr.canal.rocket.workflow.engine

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.event.Logging
import fr.canal.rocket.workflow.engine.WorkflowActor.{MessageUpdate, ProcessNextMessage, TaskToProcess, WorkflowStarted}
import fr.canal.rocket.workflow.engine.WorkflowsActor.{GetWorkflows, StartWorkflow}

case class Message(id: Int, processId: Int, completion: Int) extends Data

class Orchestrator(worflowsActorRef: ActorRef)(implicit system: ActorSystem) extends Actor {

  val log = Logging(context.system, this)

  override def receive: Receive = {
    case StartWorkflow(name: String) =>
      log.info(s"Workflow with name $name is being started")
      worflowsActorRef ! StartWorkflow(name)
    case  GetWorkflows =>
      worflowsActorRef ! GetWorkflows
    case WorkflowStarted(name: String) =>
      sender() ! ProcessNextMessage
    case TaskToProcess(message: Message) =>
      log.info(s"Message received $message")
      sender() ! MessageUpdate(message.copy(completion = 100))
  }
}

class Receiver extends Actor {

  val log = Logging(context.system, this)

  override def receive: Receive = {
    case message:Message =>
      log.info(s"Receiver with the path ${self.path} received the message with the process id ${message.processId}")
  }
}

object AkkaQuickstart extends App {

  implicit val system: ActorSystem = ActorSystem("workflow-engine")

  val workflowsActor = WorkflowsActor()

  val orchestrator = system.actorOf(Props(new Orchestrator(workflowsActor)), "orchestrator")

  /*

  val receiver1 = system.actorOf(Props[Receiver], "workflow1")
  val receiver2 = system.actorOf(Props[Receiver], "workflow2")

  val message1 = Message(1,1,0)
  val message2 = Message(2,2,0)

  system.actorSelection(s"/user/workflow${message1.processId}") ! message1
  system.actorSelection(s"/user/workflow${message2.processId}") ! message2

  */

  orchestrator ! StartWorkflow("IngestEST")
  orchestrator ! GetWorkflows

}
