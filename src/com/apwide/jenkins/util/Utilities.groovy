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

    static boolean areDependenciesAvailable(script, buildFailOnError) {
        return isMethodAvailable(script, 'Http Request', buildFailOnError) { script.httpRequest() } &&
                isMethodAvailable(script, 'Pipeline Utility Steps', buildFailOnError) { script.readJSON() }
    }

    static private boolean isMethodAvailable(script, pluginName, jiraBuildFailOnError, Closure method) {
//        script.echo "Check availability of plugin '${pluginName}'"
        try {
            method()
        } catch (NoSuchMethodError ex) {
            script.echo "Plugin '${pluginName}' not available"
            if (jiraBuildFailOnError) {
                script.currentBuild.result = Result.FAILURE
            }
            return false
        } catch (err) {
            // do nothing
        }
//        script.echo "Plugin '${pluginName}' available"
        return true
    }

    static executeStep(script, Map config, Closure action) {
        Parameters parameters = new Parameters(script, config ?: [:])

        if (!areDependenciesAvailable(script, parameters.buildFailOnError)) {
            return
        }

        try {
            return action(this, parameters)
        } catch(err) {
            if (parameters.buildFailOnError) {
                throw err
            }
        }
        return null
    }

    static executeStep(script, Closure body = null, Closure action) {
        // we should fine a way to generically bind script context to body closure
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

        if (!areDependenciesAvailable(script, parameters.buildFailOnError)) {
            return
        }

        try {
            return action(this, parameters)
        } catch(err) {
            if (parameters.buildFailOnError) {
                throw err
            }
        }
        return null
    }

//    static String render(String text, Map parameters) {
//        return new SimpleTemplateEngine().createTemplate(text).make(parameters).toString()
//    }
}
