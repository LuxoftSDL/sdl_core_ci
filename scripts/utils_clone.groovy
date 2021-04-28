// Clone jobs and view from source

import jenkins.*
import jenkins.model.*
import hudson.*
import hudson.model.*
import groovy.xml.XmlUtil
import groovy.lang.*
import groovy.lang.Binding;
import groovy.util.XmlSlurper
import groovy.lang.Tuple2
import groovy.util.slurpersupport.GPathResult
import jenkins.model.Jenkins
import hudson.model.ListView
import javax.xml.xpath.*
import javax.xml.parsers.DocumentBuilderFactory

println '=== Parameters: ==='
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
boolean createIssueJobs = params["CREATE_FEATURE_JOBS"].toBoolean()

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

println "=== New views created: ==="
println "${trgView.name}"

def srcView = jenkins.getView(src)

def processXml( String xml, String xpathQuery ) {
  def xpath = XPathFactory.newInstance().newXPath()
  def builder     = DocumentBuilderFactory.newInstance().newDocumentBuilder()
  def inputStream = new ByteArrayInputStream( xml.bytes )
  def records     = builder.parse(inputStream).documentElement
  xpath.evaluate( xpathQuery, records )
}

println "=== New jobs created: ==="
for(item in srcView.getItems()) {
  if (item.name.matches("(.*)_Issue_(.*)") && !createIssueJobs) { continue }
  def config = item.getConfigFile()
  File file = config.getFile()
  String fileContent = file.getText('UTF-8').replaceAll(src, trg)
  if (item.name.matches("(.*)=RUN=")) {
    def xml = new XmlParser().parseText(fileContent)
    //println processXml( fileContent, '//hudson.model.ParametersDefinitionProperty/parameterDefinitions' )
    // jobParams = xml.properties."hudson.model.ParametersDefinitionProperty".parameterDefinitions."hudson.model.StringParameterDefinition"
    def jobParams = processXml( fileContent, '//properties/hudson.model.ParametersDefinitionProperty/parameterDefinitions/hudson.model.StringParameterDefinition' )
    println jobParams
    // jobParams.each {
    //   it ->
    //     // def k = "${it.value}"
    //     println "++++++++++++++++++++++++++++"
    //     println "this is iteration ${i}"
    //     println "it name is ${it.name}"
    //     println "it value is ${it.value}"
    //     // jobParams << k
    //     println "stop iteration ${i}"
    //     println "++++++++++++++++++++++++++++"
    //     i = i + 1
    // }
    jobParams.each {
      it ->
        def k = "${it.value}"
        // println("++++++++")
        // println k[0]
        // println("++++++++++")
        if (params[k]) { it.defaultValue[0].value = params[k] }
    }
    fileContent = XmlUtil.serialize(xml)
  }
  def jobName = item.getName().replace(src, trg)
  def stream = new ByteArrayInputStream(fileContent.getBytes())
  def job = jenkins.createProjectFromXML(jobName, stream)
  println "${job.name}"
  trgView.doAddJobToView("${job.name}")
}

jenkins.save()

return 0