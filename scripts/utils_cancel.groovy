// Cancel jobs

import jenkins.*
import jenkins.model.*
import hudson.*
import hudson.model.*

println "=== Parameters: ==="
def viewName
def parameters = build?.actions.find{ it instanceof ParametersAction }?.parameters
parameters.each {
  println "${it.name}: ${it.value}"
  if ("${it.name}" == "CANCEL_VIEW") { viewName = "${it.value}" }
}

if (!viewName) {
  println "ERROR: Cancel view is not defined"
  return 1
}

Jenkins jenkins = Jenkins.getInstance()

View view = jenkins.getView(viewName)

if (!view) {
  println "ERROR: Cancel view is not found"
  return 1
}

def jobs = []

view.items.each { jobs.add(it.name) }

println "=== Jobs cancelled: ==="
def queue = jenkins.queue
queue.items.findAll {
  jobs.contains(it.task.name)
}.each {
  queue.cancel(it.task)
  println it.task.name
}

println "=== Jobs stopped: ==="
jenkins.getAllItems(Job.class).findAll {
  it.isBuilding() && jobs.contains(it.name)
}.each {
  it.builds.each {
    if (it.isBuilding()) { it.doStop() }
  }
  println it.name
}

return
