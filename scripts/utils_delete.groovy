// Delete jobs and view

import jenkins.*
import jenkins.model.*
import hudson.*
import hudson.model.*

println "=== Parameters: ==="
def viewName
def parameters = build?.actions.find{ it instanceof ParametersAction }?.parameters
parameters.each {
  println "${it.name}: ${it.value}"
  if ("${it.name}" == "DELETE_VIEW") { viewName = "${it.value}" }
}

if (!viewName) {
  println "ERROR: Delete view is not defined"
  return 1
}

Jenkins jenkins = Jenkins.getInstance()
View view = jenkins.getView(viewName)

if (!view) {
  println "ERROR: Delete view is not found"
  return 1
}

println "=== Jobs deleted: ==="
view.items.each {
  job ->
    job.delete()
    println "${job.name}"
}

println "=== Views deleted: ==="
jenkins.deleteView(view)
println "${view.name}"

return 0
