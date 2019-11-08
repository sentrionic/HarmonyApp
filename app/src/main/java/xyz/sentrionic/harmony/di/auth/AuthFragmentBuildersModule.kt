package xyz.sentrionic.harmony.di.auth

import dagger.Module
import dagger.android.ContributesAndroidInjector
import xyz.sentrionic.harmony.ui.auth.ForgotPasswordFragment
import xyz.sentrionic.harmony.ui.auth.LauncherFragment
import xyz.sentrionic.harmony.ui.auth.RegisterFragment

/**
 * Provides the auth fragments for dependency injection
 */
@Module
abstract class AuthFragmentBuildersModule {

    @ContributesAndroidInjector()
    abstract fun contributeLauncherFragment(): LauncherFragment

    @ContributesAndroidInjector()
    abstract fun contributeRegisterFragment(): RegisterFragment

    @ContributesAndroidInjector()
    abstract fun contributeForgotPasswordFragment(): ForgotPasswordFragment

}