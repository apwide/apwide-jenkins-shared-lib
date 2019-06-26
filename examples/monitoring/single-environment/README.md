## Single environment

You work on a single application and you want to monitor your single eCommerce production environment.

This pipeline adds a scheduled job, executed every 1 minute, which query Apwide Golive to:
* Retrieve environment details
* Use environment information to do a default check status (simple HTTP call)
* Update the status in Apwide Golive based on the result of the check status