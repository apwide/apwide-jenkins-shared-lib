This pipeline shows how to update Apwide Golive on application deployment.
---
It's based on an eCommerce application having 2 environments, Dev and Demo.
Pipeline contains a build stage and 2 conditional deployment stages to deploy on Dev or Demo environment. Build is parametrized
to select on which environment the build should be promoted. By default, when having **options** parameters, the first option
is the default value, so, on each successful build, a deployment on Dev will happen.
---
Deployment procedure is simulated with some **sleep**, but the other Apwide Golive steps can be reused and will:
* Set the status to 'Deploy' when deployment started
* Update the deployed version when deployment is finished
* Update environment information such as url or attributes
* Set the status back to 'Up' when everything is fine
