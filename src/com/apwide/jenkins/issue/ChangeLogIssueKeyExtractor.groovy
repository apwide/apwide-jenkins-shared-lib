package com.apwide.jenkins.issue

import hudson.model.Result
import hudson.plugins.git.GitChangeSet
import hudson.scm.ChangeLogSet


import com.apwide.jenkins.util.ScriptWrapper

/**
 * Parses the change log from the current build and extracts the issue keys from the commit
 * messages. It also tries to extract from squashed commits.
 */
class ChangeLogIssueKeyExtractor implements IssueKeyExtractor {

    Collection<String> extractIssueKeys(final ScriptWrapper script) {

        final Collection<IssueKey> allIssueKeys = new ArrayList<IssueKey>()
        final List<ChangeLogSet<? extends ChangeLogSet.Entry>> changeSets =
                script.getChangeSets()? new ArrayList<>(script.getChangeSets()) : new ArrayList<>()

        script.debug("ChangeLogSet: ${changeSets}")

        def previousBuild = script.getPreviousBuild()
        script.debug("Previous build: ${previousBuild}")
        script.debug("Previous build result: ${previousBuild.getResult()}")
        while (Objects.nonNull(previousBuild) && Objects.nonNull(previousBuild.getChangeSets()) && !isBuildSuccessful(previousBuild)) {
            changeSets.addAll((Set)previousBuild.getChangeSets())
            script.debug("Previous build change sets added: ${previousBuild.getChangeSets()}")
            previousBuild = previousBuild.getPreviousBuild()
        }

        for (ChangeLogSet<? extends ChangeLogSet.Entry> changeSet : changeSets) {
            final Object[] changeSetEntries = changeSet.getItems()
            script.debug("changeSetEntries: ${changeSetEntries}")

            for (Object item : changeSetEntries) {
                final ChangeLogSet.Entry changeSetEntry = (ChangeLogSet.Entry) item
                script.debug("changeSetEntry: ${changeSetEntry}")

                if (changeSetEntry instanceof GitChangeSet) {
                    def comment = ((GitChangeSet) changeSetEntry).getComment()
                    script.debug("Comment: ${comment}")
                    def issueKeys = new IssueKeyStringExtractor().extractIssueKeys(comment)
                    script.debug("IssueKeys in comment: ${issueKeys}")
                    allIssueKeys.addAll(issueKeys)
                }
                def issueKeys = new IssueKeyStringExtractor().extractIssueKeys(changeSetEntry.getMsg())
                script.debug("IssueKeys in msg: ${issueKeys}")
                allIssueKeys.addAll(issueKeys)

                if (allIssueKeys.size() >= ISSUE_KEY_MAX_LIMIT) {
                    break
                }
            }
        }
        def stringIssueKeys = allIssueKeys.collect({it.getValue()}).unique(false)
        script.debug("String issue Keys: ${stringIssueKeys}")
        return stringIssueKeys
    }

    private boolean isBuildSuccessful(final build) {
        return Result.SUCCESS.toString() == build.getResult().toString()
    }
}
