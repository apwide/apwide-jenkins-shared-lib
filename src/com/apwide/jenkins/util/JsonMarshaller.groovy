package com.apwide.jenkins.util

import groovy.json.JsonOutput
import groovy.json.JsonSlurper

class JsonMarshaller {
    static def toJsonMap(String text) {
        text?.trim() ? new JsonSlurper().parseText(text) : null
    }

    static def toJsonText(object) {
        object ? JsonOutput.toJson(object) : null
    }
}
