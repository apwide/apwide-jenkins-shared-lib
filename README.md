# Jenkins shared library for Jira and Apwide Golive

This project is a shared library to get and update information between a Jenkins pipeline
and a Jira instance ([Apwide Golive plugin](https://www.apwide.com)).

## Prerequisites to use Jenkins Jira shared library
* [Pipeline Utility Steps Plugin](https://wiki.jenkins.io/display/JENKINS/Pipeline+Utility+Steps+Plugin) must be installed
* [Http Request Plugin](https://wiki.jenkins.io/display/JENKINS/HTTP+Request+Plugin) must be installed

## Available Environment Variables
To avoid duplication in your pipelines, some Jira and Apwide environment variables can be configured at different level depending
on the granularity:
* [Jenkins Global Environment Variables](https://wiki.jenkins.io/display/JENKINS/Global+Variable+String+Parameter+Plugin)
* [In pipeline environment directive at pipeline or stage level](https://jenkins.io/doc/book/pipeline/syntax/#environment)
* [Using Pipeline Basic Step withEnv on local portion](https://jenkins.io/doc/pipeline/steps/workflow-basic-steps/#withenv-set-environment-variables)

Here are the available environment variables:
* **JIRA_BASE_URL** : Jira base url. (e.g. http://localhost:8080 or if you use a context http://localhost:2990/jira). Replace **baseUrl** parameter.
* **JIRA_CREDENTIALS_ID** : Id of the Jenkins credentials use to to call Jira Rest API. Replace **credentialsId** parameter.
* **APW_APPLICATION** : Environment application name used in Apwide Golive (e.g. 'eCommerce'). Replace **application** parameter.
* **APW_CATEGORY** : Environment category name used in Apwide Golive (e.g. 'Dev', 'Demo', 'Staging'...). Replace **category** parameter
* **APW_UNAVAILABLE_STATUS** : Status name when environment is detected as not available during check environment status. Replace **unavailableStatus** parameter
* **APW_AVAILABLE_STATUS** : Status name when environment is detcted as available during check environment status. Replace **status** parameter
* **APW_ENVIRONMENT_ID** : Id of the Apwide Golive Environment (used when updating environment details, attributes). Replace **environmentId** parameter

Most of the steps provided by the shared library are using these variables, but for each of the step, you can define inline property such as:
With inline property
```groovy
def project = jira(
    baseUrl: 'http://localhost:2990/jira',
    credentialsId: 'localhost-jira-admin',
    httpMode: 'GET',
    path: '/rest/api/2/project/10000'
)
```
With global variable
```groovy
def project = jira httpMode: 'GET', path: '/rest/api/2/project/10000'
```

To know name of parameters, please consult [Parameters](./src/com/apwide/jenkins/util/Parameters.groovy) Global Variable Reference on the pipeline where you've imported the shared lib
after [having successfully ran the job once](https://stackoverflow.com/questions/41162177/jenkins-pipeline-how-to-add-help-for-global-shared-library).

## Use cases

You can find pipeline examples in [examples](./examples) folder.
Here are some examples global functions contained in the shared library. Most of the examples assume that environment variables have been defined at an upper level.

### Get project information
Must be wrapped with a **script** directive because return a parameter
```groovy
script {
    def project = jiraGetProject id:'10000'
}
```

### Monitor environment status with Apwide Golive
with parameters:
```groovy
apwCheckEnvironmentStatus (
    application:'eCommerce',
    category:'Dev',
    unavailableStatus:'Down',
    availableStatus:'Up',
    check: {
        sh 'timeout 5 wget --retry-connrefused --tries=5 --waitretry=1 -q http://192.168.0.6:8180 -O /dev/null'
    }
)
```

with environment variables:
```groovy
apwCheckEnvironmentStatus check: {
    sh 'timeout 5 wget --retry-connrefused --tries=5 --waitretry=1 -q http://192.168.0.6:8180 -O /dev/null'
}
```

## References
* [How to setup a Jenkins shared library](https://jenkins.io/doc/book/pipeline/shared-libraries/)
* [How to configure credentials](https://jenkins.io/doc/book/using/using-credentials/)
* [Jenkins Shared Libraries](https://jenkins.io/doc/book/pipeline/shared-libraries/)
