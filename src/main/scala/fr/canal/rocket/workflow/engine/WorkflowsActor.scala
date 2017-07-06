package fr.canal.rocket.workflow.engine

import java.util.UUID

import akka.actor.{Actor, ActorSystem, Props}
import fr.canal.rocket.workflow.engine.WorkflowsActor.{GetWorkflows, StartWorkflow, UnknownWorkflow}
import fr.canal.rocket.workflow.engine.model.{WorkflowInstance, Workflows}

object WorkflowsActor {

  def apply()(implicit system: ActorSystem) = system.actorOf(Props[WorkflowsActor], "workflows-actor")

  case class StartWorkflow(id: String)
  case object GetWorkflows
  case object UnknownWorkflow

}

class WorkflowsActor extends Actor {

  override def receive: Receive = handler(Set.empty)

  def handler (workflows: Set[WorkflowInstance]): Receive = {

    case StartWorkflow(title: String) =>

      Workflows.AllWorkflows.find(w => w.title == title) match {
        case Some(workflow) =>
          val workflowId = UUID.randomUUID().toString
          val workflowActor = context.actorOf(Props(new WorkflowActor), s"workflow-$workflowId")
          val instance = WorkflowInstance(workflowId, workflow)
          workflowActor.tell(WorkflowActor.StartWorkflow(instance), sender)
          context.become(handler(workflows + instance))

        case None =>
          sender ! UnknownWorkflow
      }

    case GetWorkflows =>
      sender ! workflows

  }

}
