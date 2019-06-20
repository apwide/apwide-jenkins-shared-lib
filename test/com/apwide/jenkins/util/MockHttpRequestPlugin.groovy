package com.apwide.jenkins.util

class MockHttpRequestPlugin {

    private final HttpClient http = new HttpClient()

    def httpRequest(Map params) {
        http.request(params)
    }

}
