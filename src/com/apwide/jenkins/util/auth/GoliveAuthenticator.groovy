package com.apwide.jenkins.util.auth

import com.apwide.jenkins.util.Parameters
import com.apwide.jenkins.util.ScriptWrapper

class GoliveAuthenticator implements Authenticator {

    final ScriptWrapper script
    final Parameters parameters

    GoliveAuthenticator(ScriptWrapper script, Parameters parameters) {
        this.script = script
        this.parameters = parameters
    }

    @Override
    def authenticate(Closure request) {
        if (parameters.isCloud()) {
            script.debug("Golive cloud authentication is used")
            script.withCredentials([script.string(credentialsId: parameters.getGoliveCloudCredentialsId(), variable: 'APW_INTERNAL_GOLIVE_CLOUD_CREDENTIALS_ID')]) {
                def token = this.script.env("APW_INTERNAL_GOLIVE_CLOUD_CREDENTIALS_ID")
                def authenticationHeaders = [[name: 'api-key', value: token]]
                def authContext = new AuthenticationContext(null, authenticationHeaders)
                script.debug("call closure")
                return request(authContext)
            }
        } else {
            script.debug("Golive server authentication is used")
            def authContext = new AuthenticationContext(parameters.getJiraCredentialsId(), null)
            return request(authContext)
        }
    }
}
