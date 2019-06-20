package com.apwide.jenkins.util

import static com.apwide.jenkins.util.JsonMarshaller.toJsonText

class RestClient implements Serializable {
    private final script
    private final Map config
    private final String resourceUrl

    RestClient(script, Map config, String path = '') {
        this.script = script
        this.config = config
        this.resourceUrl = "${config.baseUrl}${path}"
    }

    private def request(httpMode = 'GET', path = '', body = null) {
        def url = "${resourceUrl}${path}"
        try {
            def response = script.httpRequest(
                    outputFile: 'response.json',
                    authentication: config.credentialsId,
                    consoleLogResponseBody: false,
                    timeout: 5,
                    httpMode: httpMode,
                    requestBody: toJsonText(body),
                    contentType: 'APPLICATION_JSON',
                    url: url,
                    validResponseCodes: '200:304')
            return script.readJSON(text: response.content)
        } catch (err) {
            if (config.fail)
            script.echo "Error during Rest call: ${err}"
            script.echo "Url: ${httpMode} ${url}"
            script.echo "Body: ${body}"
            return ''
        }
    }

    def put(path = '', body = null) {
        return request('PUT', path, body)
    }

    def post(path = '', body = null) {
        return request('POST', path, body)
    }

    def get(path = '') {
        return request('GET', path)
    }

    def delete(path = '') {
        return request('DELETE', path)
    }
}
