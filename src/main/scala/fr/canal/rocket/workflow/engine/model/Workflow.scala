package fr.canal.rocket.workflow.engine.model

import java.util.UUID

import fr.canal.rocket.workflow.engine.Message

case class WorkflowInstance(id: String, workflow: Workflow)

case class Workflow(id: String, title: String, description: String, steps: Seq[Message])

object Workflows {

  implicit def workflow(id: String): WorkflowTitleBuilder = new WorkflowTitleBuilder(id)

  class WorkflowTitleBuilder(id: String) {
    def withTitle(t: String) = new WorkflowDescriptionBuilder(id, t)
  }

  class WorkflowDescriptionBuilder(val id: String, val title: String) {
    def withDescription(description: String) = new WorkflowStepsBuilder(this, description)
  }

  class WorkflowStepsBuilder(workflowDescriptionBuilder: WorkflowDescriptionBuilder, description: String) {
    def hasSteps(steps: Seq[Message]) = Workflow(workflowDescriptionBuilder.id, workflowDescriptionBuilder.title, description, steps)
  }

  val Workflow1 = workflow(UUID.randomUUID().toString) withTitle "IngestEST" withDescription "Ceci est un workflow decrivant un ingest EST" hasSteps Seq(
    Message(1,1,0), Message(1,2,0)
  )

  val Workflow2 = workflow(UUID.randomUUID().toString) withTitle "ContributionDigital" withDescription "Ceci est un workflow permettant la contribution digitale" hasSteps Seq(
    Message(2,1,0), Message(2,2,0), Message(3,3,0)
  )

  val AllWorkflows = Set(Workflow1, Workflow2)

}
