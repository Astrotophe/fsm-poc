package fr.canal.rocket.workflow.engine

import akka.actor.{Actor, FSM}
import fr.canal.rocket.workflow.engine.WorkflowActor.WorkflowStarted
import fr.canal.rocket.workflow.engine.model.WorkflowInstance


object WorkflowActor {
  case class ReceiveMessage(message: Int)
  case class StartWorkflow(instance: WorkflowInstance)
  case class WorkflowStarted(id: String)

}

// Etats
sealed trait State

case object Idle extends State

case object SendMessage extends State

case object WaitingForMessage extends State

case object Notify extends State

// Data
trait Data

case object Uninitialized extends Data

case class InitializedWorkflow(instance: WorkflowInstance) extends Data

case class WorkflowInProgress(instance: WorkflowInstance, currentStep: Message) extends Data {
  def step = {
    instance.workflow.steps(1)
  }
}

case class CompletedWorkflow(instance: WorkflowInstance) extends Data

class WorkflowActor extends Actor with FSM[State, Data] {

  startWith(Idle, Uninitialized)

  when(Idle) {
    case Event(WorkflowActor.StartWorkflow(instance: WorkflowInstance), Uninitialized) =>
      println(s"Workflow instance ${instance.id} begins")
      goto(WaitingForMessage) using InitializedWorkflow(instance) replying WorkflowStarted(instance.id)
  }

  when(WaitingForMessage) {
    case Event(WorkflowActor.ReceiveMessage(message), InitializedWorkflow(instance)) =>
      println(s"Message with id $message received!")
      goto(Notify)
  }

  when(Notify) {
    case Event(_, message: Message) if message.completion != 100 => goto(WaitingForMessage)
    case Event(_, message: Message) if message.completion == 100 => goto(Idle)
  }

  initialize()

}
