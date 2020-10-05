package com.apwide.jenkins.util.auth

class AuthenticationContext {
    final String credentialsId
    final credentialsHeader

    AuthenticationContext(String credentialsId, credentialsHeader) {
        this.credentialsId = credentialsId
        this.credentialsHeader = credentialsHeader ?: []
    }
}
