This pipeline shows how you can automatically release and push the released version including its related tickets to Golive and Jira

It uses the apwSendReleaseInfo step that:
* create / update the version in Jira
* finds the Jira tickets that are in commit comments and add them as fixed in the Jira version
* set the Jira Version to released status


It uses the apwSendDeploymentInfo step that:
* sets deployed version in Golive
* parses the history of commits pushed since the last successful build of the branch
* finds the Jira tickets that are in commit comments and add it to the deployment description in Golive/Jira
* adds the Jira tickets that are fixed in the deployed Jira version (using a JQL query)


