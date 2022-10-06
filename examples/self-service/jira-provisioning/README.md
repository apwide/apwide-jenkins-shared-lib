This pipeline provision Jira environment on demand from Apwide Golive triggers. It uses webhooks to send events from Jira to jenkins and to
trigger the execution of the job.

You must have configured [jira webhook](https://confluence.atlassian.com/adminjiraserver/managing-webhooks-938846912.html) first and
then, use [Jenkins generic webhook trigger plugin](https://wiki.jenkins.io/display/JENKINS/Generic+Webhook+Trigger+Plugin) to select
which trigger you want to put in place.

If you want to be able to trigger a specific build on specific trigger, you will have to define a **token** that needs to be configured
at job trigger level and on the webhook url query parameter. Consult the jenkins plugin documentation to see how to proceed.

Apwide golive provides these events:
* environment:created
* environment:updated
* environment:deleted
* environment:version_deployed
* environment:status_updated
* environment:env_customfield_value_added
* environment:env_customfield_value_removed

In this example, pipeline listens to 3 types of events:
* **environment:created** : when triggered, the pipeline will
    * change the status of the environment to *In Deployment*
    * update environment information with the containerURL (url format is inferred from environment information such as category to define a listening port), attributes.
    * set the deployed version to the default jira version defined with an environment variable *DEFAULT_JIRA_VERSION*
* **environment:version_deployed** : when this event is sent, job will
    * change the status of the environment to *In Deployment*
    * stop and remove existing containers for this environment
    * populate the deployed version in Apwide Golive
    * create a new container with the version requested and using a backup present on the agent to initalize the data.
* **environment:deleted** :
    * stop and remove the container

This pipeline provisions the environment using a backup zip that must be available on the jenkins agent. It also populate an attribute named *Data Set* used
to provide a restore feature from Apwide Golive in Jira.
