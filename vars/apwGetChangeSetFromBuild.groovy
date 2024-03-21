import com.apwide.jenkins.util.Parameters
import com.apwide.jenkins.util.ScriptWrapper
import com.apwide.jenkins.issue.ChangeLogIssueKeyExtractor
// import com.apwide.jenkins.util.auth.JiraAuthenticator
// import hudson.triggers.*
// import org.jenkinsci.plugins.workflow.job.*
import jenkins.model.Jenkins

import static com.apwide.jenkins.util.Utilities.executeStep

def call(Map config = null) {
  executeStep(this, config) { ScriptWrapper script, Parameters parameters ->
    def cause  = script.script.currentBuild.getBuildCauses()[0]
    // TODO ignore non upstream
    def upstreamProject = cause.upstreamProject
    def updateBuild = cause.upstreamBuild

    def job = Jenkins.getInstance().getItemByFullName(upstreamProject)
    def build = job.getBuild("${updateBuild}")
    def changeSets = build.changeSets ? new ArrayList(build.changeSets) : new ArrayList<>()
    /*
    def changes = build.changeSets.collectMany{ changeSet ->
      changeSet.collect{ entry -> entry.msg + " " }
    }
     */
    return new ChangeLogIssueKeyExtractor().extractIssueKeysFrom(script, changeSets)
  }
}

// https://github.com/jenkinsci/jenkins/blob/master/core/src/main/java/hudson/scm/ChangeLogSet.java
// https://jenkins.apwide.com/env-vars.html/
/*
BUILD_ID
BUILD_NUMBER
JOB_NAME
JOB_BASE_NAME

import hudson.model.*
import jenkins.model.*
import hudson.scm.ChangeLogSet

def output = ""
Jenkins.getInstance().getJobNames()
Jenkins.getInstance().getItemByFullName("apps/job/golive/job/chore%252Fbuild/1")
def job = Jenkins.getInstance().getItemByFullName("apps/golive/develop")
//Jenkins.getInstance().getAllItems(Build.class)
//Jenkins.getInstance().getAllItems(Build.class)
job.getBuilds().each{ build ->
  build.changeSets.each { changeSet ->
    changeSet.each { entry ->
      output += entry.msg + "\n"
    }
  }
}
return output
*/
