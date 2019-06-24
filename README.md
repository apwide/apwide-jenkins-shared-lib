# Jenkins shared library for Jira and Apwide Golive

This project is a shared library to get and update information between a Jenkins pipeline
and a Jira instance ([Apwide Golive plugin](https://www.apwide.com)).

## Getting Started

Just [import the library]((https://stackoverflow.com/questions/41162177/jenkins-pipeline-how-to-add-help-for-global-shared-library)) in your jenkins
and use the predefined steps.

For example, set the current deployed version of an eCommerce dev environment in Apwide Golive.

###### Set Environment Deployed Version
```groovy
steps {
    apwSetDeployedVersion(
        baseUrl: 'http://jira-mycompany.com',
        credentialsId: 'jira-credentials',
        application: 'eCommerce',
        category: 'Dev',
        version: '0.0.1-SNAPSHOT'
    )
}
```
So, we've specified the **baseUrl** to reach our jira instance, the **credentialsId** referencing the credentials stored in the jenkins repository and we've
set the **version** 0.0.1-SNAPSHOT of the environment **category** name Dev used for the **application** name eCommerce.

Let's try now to set the current status of this environment.

###### Set Environment Status
```groovy
steps {
    apwSetEnvironmentStatus(
        baseUrl: 'http://jira-mycompany.com',
        credentialsId: 'jira-credentials',
        application: 'eCommerce',
        category: 'Dev',
        status: 'Up'
    )
}
```

Now, imagine we want to do both in the steps, set the deployed version and change the status. We start having a lot of duplication. So, that's why the shared
lib looks for predefined environment variables.
```groovy
environment {
    JIRA_BASE_URL = 'http://jira-mycompany.com'
    JIRA_CREDENTIALS_ID = 'jira-credentials'
    APW_APPLICATION = 'eCommerce'
    APW_CATEGORY = 'Dev'
}
steps {
    apwSetDeployedVersion version: '0.0.1-SNAPSHOT'
    apwSetEnvironmentStatus status: 'Up'
}
```

Much more concise, isn't it ? What is good with environment variable, is they can be defined at different level in Jenkins:
* [Jenkins Global Environment Variables](https://wiki.jenkins.io/display/JENKINS/Global+Variable+String+Parameter+Plugin)
* [In pipeline environment directive at pipeline or stage level](https://jenkins.io/doc/book/pipeline/syntax/#environment)
* [Using Pipeline Basic Step withEnv on local portion](https://jenkins.io/doc/pipeline/steps/workflow-basic-steps/#withenv-set-environment-variables)

So, depending on how transverse is your concept/categorization, you can decide where to configure it.
Imagine we want to monitor our environments and update their status in Apwide Golive based on an HTTP check. However, in Apwide Golive,
You can configure the list of status name an environment can have.

So, your organization:
* has single jira instance
* has several development teams working each on a specific application, including one on the eCommerce application
* use 3 level of environments: 'Dev', 'Demo', 'Production'
* wants to monitoring environment per development team/application
* use the same status cross application for available and unavailable environments

You could configure at [jenkins global level](https://wiki.jenkins.io/display/JENKINS/Global+Variable+String+Parameter+Plugin) the:
* JIRA_BASE_URL : base url to reach JIRA
* JIRA_CREDENTIALS_ID : id referencing [jenkins credentials](https://jenkins.io/doc/book/using/using-credentials/)
* APW_UNAVAILABLE_STATUS : let's put it 'Dead' because it's what has been configured in Apwide Golive (D) (default value is 'Down')
* APW_AVAILABLE_STATUS : here your company use 'Alive' for example. (default value is 'Up')

So, now you could define your pipeline this way
```groovy
pipeline {
    environment {
        APW_APPLICATION = 'eCommerce'
    }
    stages {
        stage('Check eCommerce Dev') {
            steps {
                apwCheckEnvironmentStatus (
                    category: 'Dev',
                    check: { sh 'timeout 5 wget --retry-connrefused --tries=5 --waitretry=1 -q http://192.168.0.6:8180 -O /dev/null' }
                )
            }
        }
        stage('Check eCommerce Demo') {
            steps {
                apwCheckEnvironmentStatus (
                    category: 'Demo',
                    check: { sh 'timeout 5 wget --retry-connrefused --tries=5 --waitretry=1 -q http://192.168.0.6:8080 -O /dev/null' }
                )
            }
        }
        stage('Check eCommerce Production') {
            steps {
                apwCheckEnvironmentStatus (
                    category: 'Production',
                    check: { sh 'timeout 5 wget --retry-connrefused --tries=5 --waitretry=1 -q http://192.168.0.6:9000 -O /dev/null' }
                )
            }
        }
    }
}
```

So, we defined the common **APW_APPLICATION** at the pipeline level and the rest is completely hidden in your jenkins global configuration because
there is nothing specific to your pipeline.

Imagine now you have several steps to do for each environment. Here, we're going to print a message only. You could also define env variable at stage level:
```groovy
pipeline {
    environment {
     APW_APPLICATION = 'eCommerce'
    }
    stages {
        stage('Check eCommerce Dev') {
            environment {
                APW_CATEGORY = 'Dev'
            }
            steps {
                echo "checking status of ${APW_CATEGORY}"
                apwCheckEnvironmentStatus check: { sh 'timeout 5 wget --retry-connrefused --tries=5 --waitretry=1 -q http://192.168.0.6:8180 -O /dev/null' }
            }
        }
        stage('Check eCommerce Demo') {
            steps {
                echo "checking status of ${APW_CATEGORY}"
                apwCheckEnvironmentStatus check: { sh 'timeout 5 wget --retry-connrefused --tries=5 --waitretry=1 -q http://192.168.0.6:8080 -O /dev/null' }
            }
        }
        stage('Check eCommerce Production') {
            steps {
                echo "checking status of ${APW_CATEGORY}"
                apwCheckEnvironmentStatus check: { sh 'timeout 5 wget --retry-connrefused --tries=5 --waitretry=1 -q http://192.168.0.6:9000 -O /dev/null' }
            }
        }
    }
}
 ```
If you want to have more examples, let's check our [examples folder](./examples)

You're not satisfied by exposed global variables, do your call to jira by yourself:
```groovy
steps {
    jira httpMode: 'GET', path: '/rest/api/2/project/10000'
    jira httpMode: 'POST', path: '/rest/api/2/versions', body [:]
}
```

You want to propose an new abstraction, feel free to create a PR !

## Prerequisites to use Jenkins Jira shared library
* [Pipeline Utility Steps Plugin](https://wiki.jenkins.io/display/JENKINS/Pipeline+Utility+Steps+Plugin) must be installed
* [Http Request Plugin](https://wiki.jenkins.io/display/JENKINS/HTTP+Request+Plugin) must be installed

## Available Environment Variables
To avoid duplication in your pipelines, some Jira and Apwide environment variables can be configured at different level depending
on the granularity:

Here are the available environment variables:
* **JIRA_BASE_URL** : Jira base url. (e.g. http://localhost:8080 or if you use a context http://localhost:2990/jira). Replace **baseUrl** parameter.
* **JIRA_CREDENTIALS_ID** : Id of the Jenkins credentials use to to call Jira Rest API. Replace **credentialsId** parameter. If not provided the shared library
will look for the credentials id 'jira-credentials'
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

## References
* [How to setup a Jenkins shared library](https://jenkins.io/doc/book/pipeline/shared-libraries/)
* [How to configure credentials](https://jenkins.io/doc/book/using/using-credentials/)
* [Jenkins Shared Libraries](https://jenkins.io/doc/book/pipeline/shared-libraries/)
