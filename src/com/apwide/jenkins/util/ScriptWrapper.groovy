package com.apwide.jenkins.util

import static com.apwide.jenkins.util.ScriptWrapper.LogLevel.DEBUG

class ScriptWrapper {
    final script
    final Parameters params

    ScriptWrapper(script, Parameters params) {
        this.script = script
        this.params = params
    }

    def getCurrentBuildResult() {
        return this.script.currentBuild.result
    }

    def httpRequest(Map params) {
        def requestParams = params << [consoleLogResponseBody: isLogEnabled()]
        return script.httpRequest(requestParams)
    }

    def readJSON(Map params = null) {
        return script.readJSON(params)
    }

    def setCurrentBuildResult(result) {
        this.script.currentBuild.result = result
    }

    def debug(msg) {
        if (isLogEnabled()) {
            this.script.echo(msg)
        }
    }

    def string(params) {
        return this.script.string(params)
    }

    def withCredentials(params, Closure task) {
        script.withCredentials(params) {
            task()
        }
    }

    def env(key) {
        this.script.env[key]
    }

    private boolean isLogEnabled() {
        return LogLevel.levelFrom(this.params.logLevel()) == DEBUG
    }

    enum LogLevel {
        OFF, DEBUG;

        static levelFrom(String logLevel) {
            if (logLevel != null && logLevel.equalsIgnoreCase("debug")) {
                return DEBUG;
            }
            return OFF;
        }
    }
}
