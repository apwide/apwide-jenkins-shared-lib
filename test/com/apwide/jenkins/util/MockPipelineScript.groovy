package com.apwide.jenkins.util

class MockPipelineScript {
    private final httpRequestPlugin
    private final readJsonPlugin

    MockPipelineScript(httpRequestPlugin, readJsonPlugin) {
        this.httpRequestPlugin = httpRequestPlugin
        this.readJsonPlugin = readJsonPlugin
    }

    def httpRequest = httpRequestPlugin.&httpRequest
    def readJSON = readJsonPlugin.&readJSON
    def echo = System.out.&println
    def env = [:]
    def currentBuild = [:]
}
