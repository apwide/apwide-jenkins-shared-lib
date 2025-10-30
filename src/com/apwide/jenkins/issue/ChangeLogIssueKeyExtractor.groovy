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
    final Map<String, Collection<String>> issueKeysByBuild = extractChanges();
    def issueKeys = issueKeysByBuild.values().flatten().<String>toSet()
    script.debug("List of Previous Build(s) parsed: ${issueKeysByBuild.keySet()}")
    script.debug("List of Issue Key(s) found:: ${issueKeys}")
    return issueKeys
  }

  @NonCPS
  private Map<String, Collection<String>> extractChanges() {
    final Map<String, Collection<String>> issueKeysByBuild = new LinkedHashMap<>();
    issueKeysByBuild.put(script.getCurrentBuildFullDisplayName(), extractFromChangeSets(script.getChangeSets()))

    // https://javadoc.jenkins.io/plugin/workflow-support/org/jenkinsci/plugins/workflow/support/steps/build/RunWrapper.html
    def previousBuild = script.getPreviousBuild()
    while (Objects.nonNull(previousBuild) && !isBuildSuccessful(previousBuild) && issueKeysByBuild.values().flatten().size() < ISSUE_KEY_MAX_LIMIT) {
      issueKeysByBuild.put(previousBuild.getFullDisplayName(), extractFromChangeSets(previousBuild.getChangeSets()))
      previousBuild = previousBuild.getPreviousBuild()
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
