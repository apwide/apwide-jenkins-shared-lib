import com.apwide.jenkins.golive.Categories
import com.apwide.jenkins.util.Parameters

import static com.apwide.jenkins.util.Utilities.executeStep

def call(Map config = null) {
    executeStep(this, config) { script, Parameters parameters ->
        return new Categories(this, parameters.config).update(
                parameters.category,
                parameters.body
        )
    }
}