package fr.canal.rocket.workflow.engine

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.event.Logging
import fr.canal.rocket.workflow.engine.WorkflowActor.{MessageUpdate, ProcessNextMessage, TaskToProcess, WorkflowStarted}
import fr.canal.rocket.workflow.engine.WorkflowsActor.{GetWorkflows, StartWorkflow}

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

case class Message(id: Int, processId: Int, completion: Int) extends Data


/**
  *
  * This is the orchestator
  *
  * @param worflowsActorRef Reference to the father of all workflows
  * @param system           ActorySystem from the engine
  */
class Orchestrator(worflowsActorRef: ActorRef)(implicit system: ActorSystem, executionContext: ExecutionContext) extends Actor {

  val log = Logging(context.system, this)

  override def preStart(): Unit = {
    super.preStart()
    log.debug("Orchestrator started")
  }

  override def postStop(): Unit = {
    super.postStop()
    log.debug("Orchestrator stopped")
  }

  override def receive: Receive = {

    case StartWorkflow(name: String) =>
      log.info(s"Workflow with name $name is being started")
      worflowsActorRef ! StartWorkflow(name)

    case GetWorkflows =>
      worflowsActorRef ! GetWorkflows

    case WorkflowStarted(name: String) =>
      sender() ! ProcessNextMessage

    case TaskToProcess(message: Message) =>
      log.info(s"Message submitted to the processors $message")
      system.scheduler.scheduleOnce(5.seconds, sender(), MessageUpdate(message.copy(completion = 25)))
      system.scheduler.scheduleOnce(10.seconds, sender(), MessageUpdate(message.copy(completion = 50)))
      system.scheduler.scheduleOnce(15.seconds, sender(), MessageUpdate(message.copy(completion = 75)))
      system.scheduler.scheduleOnce(20.seconds, sender(), MessageUpdate(message.copy(completion = 100)))
  }
}

/**
  *
  * Quick Launcher
  *
  */
object AkkaQuickstart extends App {

  implicit val system: ActorSystem = ActorSystem("workflow-engine")
  // ExecutionContext from the default dispatcher
  implicit val executionContext = system.dispatchers.defaultGlobalDispatcher

  val workflowsActor = WorkflowsActor()

  val orchestrator = system.actorOf(Props(new Orchestrator(workflowsActor)), "orchestrator")

  orchestrator ! StartWorkflow("IngestEST")
  orchestrator ! GetWorkflows

}
