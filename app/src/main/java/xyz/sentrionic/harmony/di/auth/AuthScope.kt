package xyz.sentrionic.harmony.di.auth

import javax.inject.Scope

/**
 * AuthScope is strictly for login and registration
 */
@Scope
@kotlin.annotation.Retention(AnnotationRetention.RUNTIME)
annotation class AuthScope