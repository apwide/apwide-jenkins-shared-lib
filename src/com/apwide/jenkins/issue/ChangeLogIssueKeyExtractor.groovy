package com.apwide.jenkins.issue;

import hudson.plugins.git.GitChangeSet;
import hudson.scm.ChangeLogSet;


import com.apwide.jenkins.util.ScriptWrapper;

/**
 * Parses the change log from the current build and extracts the issue keys from the commit
 * messages. It also tries to extract from squashed commits.
 */
class ChangeLogIssueKeyExtractor implements IssueKeyExtractor {

    Collection<String> extractIssueKeys(final ScriptWrapper script) {

        final Collection<IssueKey> allIssueKeys = new ArrayList<IssueKey>();
        final List<ChangeLogSet<? extends ChangeLogSet.Entry>> changeSets =
                new ArrayList<>(script.getChangeSets());

        script.debug("ChangeLogSet: ${changeSets}")

        /*ScriptWrapper previous = workflowRun.getPreviousBuild();
        while (Objects.nonNull(previous) && !isBuildSuccessful(previous)) {
            changeSets.addAll(previous.getChangeSets());
            previous = previous.getPreviousBuild();
        }*/

        for (ChangeLogSet<? extends ChangeLogSet.Entry> changeSet : changeSets) {
            final Object[] changeSetEntries = changeSet.getItems();
            script.debug("changeSetEntries: ${changeSetEntries}")

            for (Object item : changeSetEntries) {
                final ChangeLogSet.Entry changeSetEntry = (ChangeLogSet.Entry) item;
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
                allIssueKeys.addAll(issueKeys);

                if (allIssueKeys.size() >= ISSUE_KEY_MAX_LIMIT) {
                    break;
                }
            }
        }

        return allIssueKeys.collect({it.getValue()})
    }

    private boolean isBuildSuccessful(final ScriptWrapper workflowRun) {
        return true
        /*return Optional.ofNullable(workflowRun)
                .collect({ it.getCurrentBuildResult })
                .collect({ it.toString() })
                .find({Result.SUCCESS.toString().equals(it)})
                .isEmpty(); */
    }
}
