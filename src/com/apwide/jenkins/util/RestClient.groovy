package com.apwide.jenkins.util

import com.apwide.jenkins.util.auth.AuthenticationContext
import com.apwide.jenkins.util.auth.Authenticator

import static com.apwide.jenkins.util.JsonMarshaller.toJsonText

class RestClient implements Serializable {
    private final ScriptWrapper script
    private final Map config
    private final String resourceUrl
    private final Authenticator authenticator;

    RestClient(script, Map config, Authenticator authenticator, String resourceUrl) {
        this.script = script
        this.config = config
        this.resourceUrl = resourceUrl
        this.authenticator = authenticator
    }

    private def request(httpMode = 'GET', path = '', body = null, validResponseCodes = '200:304') {
        script.debug("trigger request")
        authenticator.authenticate { AuthenticationContext authContext ->
            return executeRequest(httpMode, path, body, validResponseCodes, authContext)
        }
    }

    private def executeRequest(httpMode = 'GET', path = '', body = null, validResponseCodes = '200:304', AuthenticationContext authContext) {
        script.debug("execute request")
        script.debug("credentials id ${authContext.getCredentialsId()}")
        script.debug("customHeaders ${authContext.getCredentialsHeader()}")
        def previousResult = script.getCurrentBuildResult()
        def url = "${resourceUrl}${path}"

        def requestOptions = [
            authentication: authContext.getCredentialsId(),
            customHeaders: authContext.getCredentialsHeader(),
            consoleLogResponseBody: true,
            timeout: 5,
            httpMode: httpMode,
            requestBody: toJsonText(body),
            contentType: 'APPLICATION_JSON',
            url: url,
            quiet: !script.isLogEnabled(),
            validResponseCodes: validResponseCodes
        ] << (config.httpRequestOptions ?: [:])

        script.debug("request Options")
        script.debug("$requestOptions")

        try {
            def response = script.httpRequest(requestOptions)
            return response.content ? script.readJSON(text: response.content) : null
        } catch (err) {
            script.debug("Error during Rest call: ${err}")
            script.debug("Url: ${httpMode} ${url}")
            script.debug("Body: ${body}")
            if (config.buildFailOnError) {
                script.debug("Build marked to fail")
                throw err
            }
            script.debug("Build marked to not fail")
            script.debug("Previous build result ${previousResult}")
            script.setCurrentBuildResult(previousResult)
            return null
        }
    }

    def put(path = '', body = null) {
        return request('PUT', path, body)
    }

    def post(path = '', body = null) {
        return request('POST', path, body)
    }

    def get(path = '', validResponseCodes  = '200:304') {
        return request('GET', path, null, validResponseCodes)
    }

    def delete(path = '') {
        return request('DELETE', path)
    }

    static def checkUrl(Map params, ScriptWrapper script) {
        int tries = 0
        int nbRetry = params.nbRetry ?: 1

        while (tries <= nbRetry) {
            try {
                script.httpRequest(params)
                return
            } catch (err) {
                if (tries >= nbRetry) {
                    throw err
                }
            }
            tries++
        }

    }
}
