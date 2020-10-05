package com.apwide.jenkins.util.auth

interface Authenticator {

    Object authenticate(Closure task)
}