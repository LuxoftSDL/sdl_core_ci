// Rename jobs and view from source

import jenkins.*
import jenkins.model.*
import hudson.*
import hudson.model.*
import groovy.xml.XmlUtil

println "=== Parameters: ==="
def params = [:]
build?.actions.find{ it instanceof ParametersAction }?.parameters.each {
def k = "${it.name}"
def v = "${it.value}"
if (v) {
 params[k] = v
  println "${k}: ${v}"
 }
}
def user = "luxoft_ci_tech@luxoft.com"
def token = params["CI_PASSWORD"]
def src
def trg
if (params["SOURCE_VIEW"]) { src = params["SOURCE_VIEW"] }
if (params["TARGET_VIEW"]) { trg = params["TARGET_VIEW"] }

if (!src) {
  println "ERROR: Source view is not defined"
  return 1
}

if (!trg) {
  println "ERROR: Target view is not defined"
  return 1
}

Jenkins jenkins = Jenkins.getInstance()

def trgView = new ListView(trg)

jenkins.addView(trgView)
def reqGet = ["bash", "-c", "curl -X GET http://localhost:8080/view/${src}/config.xml"]
def process = reqGet.execute()
process.waitFor()
def srcCfg = process.text
def trgCfg = srcCfg.replaceAll(src, trg).replaceAll("[\n\r]", "").trim()
def reqPost = ["bash", "-c", "echo '${trgCfg}' | curl -X POST -d @- http://localhost:8080/view/${trg}/config.xml -u ${user}:${token}"]
def process2 = reqPost.execute()
process2.waitFor()

def srcView = jenkins.getView(src)

println "=== Jobs renamed: ==="
for(item in srcView.getItems()) {
  def oldJobName = item.getName()
  def newJobName = oldJobName.replace(src, trg)
  item.renameTo(newJobName);
  println "${oldJobName} => ${newJobName}"
  trgView.doAddJobToView("${item.name}")
}

jenkins.deleteView(srcView)

println "=== Views renamed: ==="
println "${src} => ${trg}"

jenkins.save()

return 0
