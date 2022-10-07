package com.apwide.jenkins.util

class MockPipelineScript {
    private final httpRequestPlugin
    private final readJsonPlugin

    class Build{
        def changeSets
        def result
        Build previousBuild

        Build(Build previousBuild){
            this.previousBuild = previousBuild
        }
    }

    MockPipelineScript(httpRequestPlugin, readJsonPlugin) {
        this.httpRequestPlugin = httpRequestPlugin
        this.readJsonPlugin = readJsonPlugin
    }

    def httpRequest = httpRequestPlugin.&httpRequest
    def readJSON = readJsonPlugin.&readJSON
    def echo = System.out.&println
    def env = [:]
    Build previousBuild = new Build(null)
    Build currentBuild = new Build(previousBuild)

}
