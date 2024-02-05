This pipeline shows how to track environment information during deployment
---
This example reuse the pipeline from [send-deployment-information](../../deployment/send-deployment-information/README.md) but instead of
using the apwSendDeploymentInfo, it uses a different step, apwSendEnvironmentInfo.

This step is able:
* to create application, category and/or environment if configured to.
* update environment URL/attributes
* push deployment information
* extract issue keys from commit messages to link them on deployment
* update status of environment
