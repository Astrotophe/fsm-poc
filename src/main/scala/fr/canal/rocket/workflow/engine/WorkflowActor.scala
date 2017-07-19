package fr.canal.rocket.workflow.engine

import akka.actor.{Actor, FSM}
import fr.canal.rocket.workflow.engine.WorkflowActor.{DoNotify, ProcessNextMessage, TaskToProcess, WorkflowStarted}
import fr.canal.rocket.workflow.engine.model.WorkflowInstance


object WorkflowActor {

  case class TaskToProcess(message: Message)

  case class StartWorkflow(instance: WorkflowInstance)

  case object ProcessNextMessage

  case class DoNotify(message: Message)

  case class MessageUpdate(message: Message)

  case class WorkflowStarted(id: String)

}

// Etats
sealed trait State

case object Idle extends State

case object WaitingForInputs extends State

case object SendMessage extends State

case object WaitingForMessage extends State

case object Notify extends State

// Data
trait Data

case object Uninitialized extends Data

case class InitializedWorkflow(instance: WorkflowInstance) extends Data

case class WorkflowInProgress(instance: WorkflowInstance, currentStep: Message) extends Data {
  def step: Message = {
    instance.workflow.steps(1)
  }
}

case class CompletedWorkflow(instance: WorkflowInstance) extends Data

class WorkflowActor extends Actor with FSM[State, Data] {

  startWith(Idle, Uninitialized)

  when(Idle) {
    case Event(WorkflowActor.StartWorkflow(instance: WorkflowInstance), Uninitialized) =>
      log.info(s"Workflow instance ${instance.id} begins")
      goto(SendMessage) using InitializedWorkflow(instance) replying WorkflowStarted(instance.id)
  }

  when(SendMessage) {
    case Event(ProcessNextMessage, InitializedWorkflow(instance)) =>
      log.info("Sending order Message 1 to the orchestrator")
      goto(WaitingForMessage) using InitializedWorkflow(instance) replying TaskToProcess(Message(1, 1, 0))
  }

  when(WaitingForMessage) {
    case Event(WorkflowActor.MessageUpdate(message), InitializedWorkflow(instance)) =>
      log.info(s"Message with id ${message.id} Updated!")
      self ! DoNotify(message)
      goto(Notify)
  }

  when(Notify) {
    case Event(DoNotify(message: Message), InitializedWorkflow(instance)) if message.completion != 100 =>
      log.info(s"Notify the message $message")
      goto(SendMessage)
    case Event(DoNotify(message: Message), InitializedWorkflow(instance)) if message.completion == 100 =>
      log.info(s"Message $message fully processed")
      goto(SendMessage)
  }

  initialize()

}
