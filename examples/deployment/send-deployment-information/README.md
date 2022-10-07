This pipeline shows how you can automatically push deployment information (including the list of deployed Jira tickets!) to Golive and Jira in a classical Continuous Integration (CI) scenario

It uses the apwSendDeploymentInfo step that:
* sets deployed version and the build number in Golive
* parses the history of commits pushed since the last successful build of the branch
* finds the Jira tickets that are in commit comments and add it to the deployment description in Golive/Jira
---
**Why is this useful?**

The same version is automatically redeployed to the CI environment each time a new commit is pushed to the branch. 
As a consequence, **the version number alone is not enough to track what has been changed between 2 deployments!**

---
**How does it work behind the scene?**

Jenkins tracks changes by providing the list of commits pushed since the last successful build of a branch. 
Thanks to that, the apwSendDeploymentInfo step can:
* automatically parse this list of commits to find the list of Jira tickets that have been added since the last successful build
* format and push the list of deployed Jira tickets back to Golive and Jira

