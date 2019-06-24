package com.apwide.jenkins.util


import groovyx.net.http.HTTPBuilder
import groovyx.net.http.Method

import static com.apwide.jenkins.util.JsonMarshaller.toJsonText
import static groovyx.net.http.ContentType.ANY
import static groovyx.net.http.ContentType.JSON

class HttpClient {

    private final http = new HTTPBuilder()

    def request(Map params) {
        def httpMode = params.httpMode
        def requestBody = params.requestBody
        def url = params.url
        def contentType = contentType(params)

        def method = Method.valueOf(httpMode)
        def credentials = 'admin:admin'.bytes.encodeBase64().toString()

        def response = http.request(url, method, contentType) { request ->
            headers.Authorization = "Basic ${credentials}"
            body = requestBody
        }

        if (contentType.equals(JSON)) {
            return [content: toJsonText(response)]
        } else {
            return [content: response]
        }
    }

    def contentType(Map params) {
        def jenkinsContentType = params?.contentType
        if ("APPLICATION_JSON".equals(jenkinsContentType)) {
            return JSON
        } else {
            return ANY
        }
    }
}
