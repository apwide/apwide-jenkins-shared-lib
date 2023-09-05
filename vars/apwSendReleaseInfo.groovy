import com.apwide.jenkins.jira.Release
import com.apwide.jenkins.util.Parameters
import com.apwide.jenkins.util.ScriptWrapper

import static com.apwide.jenkins.util.Utilities.executeStep

def call(Map config = null) {
    executeStep(this, config) { ScriptWrapper script, Parameters parameters ->
        return new Release(script, parameters).sendReleaseInfo(
                parameters.params.versionName,
                parameters.params.versionDescription,
                parameters.params.projectIdOrKey,
                parameters.params.startDate,
                parameters.params.issueKeys as Collection<String>,
                parameters.params.released,
                parameters.params.releaseDate
        )
    }
}
