package com.apwide.jenkins.util

import static com.apwide.jenkins.util.JsonMarshaller.toJsonMap

class MockReadJsonPlugin {
    def readJSON(Map params) {
        toJsonMap(params.text as String)
    }
}
