package com.apwide.jenkins.util

import groovyx.net.http.HTTPBuilder
import groovyx.net.http.Method

import static com.apwide.jenkins.util.JsonMarshaller.toJsonText
import static groovyx.net.http.ContentType.JSON

class HttpClient {

    private final http = new HTTPBuilder()

    def request(Map params) {
        def httpMode = params.httpMode
        def requestBody = params.requestBody
        def url = params.url

        def method = Method.valueOf(httpMode)
        def credentials = 'admin:admin'.bytes.encodeBase64().toString()

        def response = http.request(url, method, JSON) { request ->
            headers.Authorization = "Basic ${credentials}"
            headers.Accept = "application/json"
            body = requestBody
        }

        return [content: toJsonText(response)]
    }
}
