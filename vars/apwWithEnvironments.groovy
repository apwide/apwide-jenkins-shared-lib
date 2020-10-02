import com.apwide.jenkins.golive.Environments
import com.apwide.jenkins.util.Parameters
import com.apwide.jenkins.util.ScriptWrapper

import static com.apwide.jenkins.util.Utilities.executeStep

def call(Map criteria, Closure task) {
    //use different here to be able to define the closure in last parameter
    executeStep(this, [:]) { ScriptWrapper script, Parameters parameters ->
        new Environments(script, parameters).withEnvironments(criteria, task)
    }
}