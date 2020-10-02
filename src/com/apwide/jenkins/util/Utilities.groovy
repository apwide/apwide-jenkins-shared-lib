package com.apwide.jenkins.util

import hudson.model.Result

import static groovy.lang.Closure.DELEGATE_FIRST

class Utilities {

    static String urlEncode(value) {
        if (value instanceof String) {
            return java.net.URLEncoder.encode(value, 'UTF-8')
        }
        return value
    }

    static boolean areDependenciesAvailable(ScriptWrapper script, buildFailOnError) {
        return isMethodAvailable(script, 'Http Request', buildFailOnError) { script.httpRequest() } &&
                isMethodAvailable(script, 'Pipeline Utility Steps', buildFailOnError) { script.readJSON() }
    }

    static private boolean isMethodAvailable(ScriptWrapper script, pluginName, jiraBuildFailOnError, Closure method) {
        try {
            method()
        } catch (NoSuchMethodError ex) {
            script.debug("Plugin '${pluginName}' not available")
            if (jiraBuildFailOnError) {
                script.setCurrentBuildResult(Result.FAILURE)
            }
            return false
        } catch (err) {
            // do nothing
        }
        return true
    }

    static executeStep(script, Map config, Closure action) {
        Parameters parameters = new Parameters(script, config ?: [:])
        ScriptWrapper wrappedScript = new ScriptWrapper(script, parameters)

        if (!areDependenciesAvailable(wrappedScript, parameters.buildFailOnError)) {
            return
        }

        try {
            return action(wrappedScript, parameters)
        } catch(err) {
            if (parameters.buildFailOnError) {
                throw err
            }
        }
        return null
    }

    static executeStep(script, Closure body = null, Closure action) {
        // we should find a way to generically bind script context to body closure
        Map params = [
                currentBuild: script.currentBuild,
                env: script.env
        ]

        if (body != null) {
            body.resolveStrategy = DELEGATE_FIRST
            body.delegate = params
            body()
        }

        Parameters parameters = new Parameters(script, params)
        ScriptWrapper wrappedScript = new ScriptWrapper(script, parameters)

        if (!areDependenciesAvailable(wrappedScript, parameters.buildFailOnError)) {
            return
        }

        try {
            return action(wrappedScript, parameters)
        } catch(err) {
            if (parameters.buildFailOnError) {
                throw err
            }
        }
        return null
    }
}
