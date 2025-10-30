package com.apwide.jenkins.issue

import com.apwide.jenkins.util.ScriptWrapper
import com.cloudbees.groovy.cps.NonCPS
import hudson.model.Result
import hudson.plugins.git.GitChangeSet

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
    // Resolve the RunWrapper in CPS; everything below runs in one NonCPS call (no CPS method may be called from there)
    final Map<String, Collection<String>> issueKeysByBuild = extractChanges(script.getCurrentBuild())
    def issueKeys = issueKeysByBuild.values().flatten().<String>toSet()
    script.debug("List of Previous Build(s) parsed: ${issueKeysByBuild.keySet()}")
    script.debug("List of Issue Key(s) found:: ${issueKeys}")
    return issueKeys
  }

  @NonCPS
  private static Map<String, Collection<String>> extractChanges(currentBuild) {
    final Map<String, Collection<String>> issueKeysByBuild = new LinkedHashMap<>()
    int count = 0
    // https://javadoc.jenkins.io/plugin/workflow-support/org/jenkinsci/plugins/workflow/support/steps/build/RunWrapper.html
    def build = currentBuild
    while (build != null && count < ISSUE_KEY_MAX_LIMIT) {
      if (!build.is(currentBuild) && build.getResult() == Result.SUCCESS.toString()) {
        break
      }
      Collection<String> keys = extractFromChangeSets(build.getChangeSets())
      issueKeysByBuild.put(build.getFullDisplayName(), keys)
      count += keys.size()
      build = build.getPreviousBuild()
    }
    return issueKeysByBuild
  }

  @NonCPS
  private static Collection<String> extractFromChangeSets(changeSets) {
    Collection<String> issueKeys = new LinkedHashSet<>()
    for (def changeSet : (changeSets ?: [])) {
      for (def entry : changeSet.getItems()) {
        if (issueKeys.size() >= ISSUE_KEY_MAX_LIMIT) {
          return issueKeys
        }
        String changeContent = "Message: " + entry.getMsg() + "\n"
        if (entry instanceof GitChangeSet) {
          changeContent += "Comment: " + ((GitChangeSet) entry).getComment()
        }
        issueKeys.addAll(extractIssueKeys(changeContent))
      }
    }
    return issueKeys
  }
}
