## Custom check status logic

Simple HTTP call is not enough to check the status of your environment, you need more advanced logic.

This pipeline will show you how to use environment information provided by Apwide Golive to implement custom check status logic. You just have to provide
a custom closure that will receive environment information stored in Golive to implement its check status. If the check raise an exception, the status
will be automatically updated to the unavailable status provided, otherwise, environment will be updated to available.