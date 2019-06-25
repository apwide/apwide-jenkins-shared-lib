import com.apwide.jenkins.golive.Environments
import com.apwide.jenkins.util.Parameters

import static com.apwide.jenkins.util.Utilities.executeStep

def call(Map criteria, Closure task) {
    //use different here to be able to define the closure in last parameter
    executeStep(this, [:]) { script, Parameters parameters ->
        new Environments(this, parameters.config).withEnvironments(criteria, task)
    }
}