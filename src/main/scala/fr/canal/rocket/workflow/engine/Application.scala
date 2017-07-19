//#full-example
package fr.canal.rocket.workflow.engine

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.event.Logging
import fr.canal.rocket.workflow.engine.WorkflowActor.{MessageUpdate, ProcessNextMessage, TaskToProcess, WorkflowStarted}
import fr.canal.rocket.workflow.engine.WorkflowsActor.{GetWorkflows, StartWorkflow}

case class Message(id: Int, processId: Int, completion: Int) extends Data


/**
  *
  * This is the orchestator
  *
  * @param worflowsActorRef Reference to the father of all workflows
  * @param system ActorySystem from the engine
  */
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

/**
  *
  * Quick Launcher
  *
  */
object AkkaQuickstart extends App {

  implicit val system: ActorSystem = ActorSystem("workflow-engine")

  val workflowsActor = WorkflowsActor()

  val orchestrator = system.actorOf(Props(new Orchestrator(workflowsActor)), "orchestrator")

  orchestrator ! StartWorkflow("IngestEST")
  orchestrator ! GetWorkflows

}
