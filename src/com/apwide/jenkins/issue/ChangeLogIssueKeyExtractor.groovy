package com.apwide.jenkins.issue

import com.apwide.jenkins.util.ScriptWrapper
import com.cloudbees.groovy.cps.NonCPS
import hudson.model.Result
import hudson.plugins.git.GitChangeSet
import hudson.scm.ChangeLogSet

import static com.apwide.jenkins.issue.IssueKeyStringExtractor.extractIssueKeys

/**
 * Parses the change log from the current build and extracts the issue keys from the commit
 * messages. It also tries to extract from squashed commits.
 */
class ChangeLogIssueKeyExtractor {
  private static final Integer ISSUE_KEY_MAX_LIMIT = 100;

  private final ScriptWrapper script;

  ChangeLogIssueKeyExtractor(ScriptWrapper script) {
    this.script = script;
  }

  Collection<String> extract() {
    // Collect all build data in CPS context (cannot call Jenkins methods inside @NonCPS)
    def buildDataList = []
    
    // Add current build
    def currentBuildDisplayName = script.getCurrentBuildFullDisplayName()
    def currentChangeSets = script.getChangeSets()
    buildDataList << [displayName: currentBuildDisplayName, changeSets: currentChangeSets]
    
    // Traverse previous builds and collect their data
    def previousBuild = script.getPreviousBuild()
    int totalIssueCount = 0
    while (Objects.nonNull(previousBuild) && totalIssueCount < ISSUE_KEY_MAX_LIMIT) {
      def buildResult = previousBuild.getResult()
      if (buildResult != null && Result.SUCCESS.toString() == buildResult.toString()) {
        break // Stop at first successful build
      }
      
      buildDataList << [
        displayName: previousBuild.getFullDisplayName(),
        changeSets: previousBuild.getChangeSets()
      ]
      
      previousBuild = previousBuild.getPreviousBuild()
      totalIssueCount = buildDataList.size() * 10 // Rough estimate to avoid infinite loops
    }
    
    // Now process all the collected data in @NonCPS method
    final Map<String, Collection<String>> issueKeysByBuild = extractChanges(buildDataList);
    def issueKeys = issueKeysByBuild.values().flatten().<String>toSet()
    script.debug("List of Previous Build(s) parsed: ${issueKeysByBuild.keySet()}")
    script.debug("List of Issue Key(s) found:: ${issueKeys}")
    return issueKeys
  }

  @NonCPS
  private Map<String, Collection<String>> extractChanges(List<Map> buildDataList) {
    final Map<String, Collection<String>> issueKeysByBuild = new LinkedHashMap<>();
    
    int totalIssues = 0
    for (Map buildData : buildDataList) {
      if (totalIssues >= ISSUE_KEY_MAX_LIMIT) {
        break
      }
      Collection<String> issues = extractFromChangeSets(buildData.changeSets)
      issueKeysByBuild.put(buildData.displayName, issues)
      totalIssues += issues.size()
    }

    return issueKeysByBuild
  }

  @NonCPS
  private Collection<String> extractFromChangeSets(List<ChangeLogSet<? extends ChangeLogSet.Entry>> changeSets = new ArrayList<>()) {
    Collection<String> issueKeys = new ArrayList<>()
    for (def changeSet : changeSets) {
      def iterator = changeSet.iterator()
      while (iterator.hasNext() && issueKeys.size() < ISSUE_KEY_MAX_LIMIT) {
        def entry = iterator.next()

        String changeContent = "Message: " + entry.getMsg() + "\n"
        if (entry instanceof GitChangeSet) {
          changeContent += "Comment: " + ((GitChangeSet) entry).getComment()
        }
        def contentIssueKeys = extractIssueKeys(changeContent)
        issueKeys.addAll(contentIssueKeys)
      }
    }
    return issueKeys
  }

  private boolean isBuildSuccessful(final build) {
    return Result.SUCCESS.toString() == build.getResult().toString()
  }
}
