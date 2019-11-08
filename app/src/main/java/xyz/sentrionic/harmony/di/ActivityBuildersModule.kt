package xyz.sentrionic.harmony.di

import dagger.Module
import dagger.android.ContributesAndroidInjector
import xyz.sentrionic.harmony.di.auth.AuthFragmentBuildersModule
import xyz.sentrionic.harmony.di.auth.AuthModule
import xyz.sentrionic.harmony.di.auth.AuthScope
import xyz.sentrionic.harmony.di.auth.AuthViewModelModule
import xyz.sentrionic.harmony.di.main.MainFragmentBuildersModule
import xyz.sentrionic.harmony.di.main.MainModule
import xyz.sentrionic.harmony.di.main.MainScope
import xyz.sentrionic.harmony.di.main.MainViewModelModule
import xyz.sentrionic.harmony.ui.auth.AuthActivity
import xyz.sentrionic.harmony.ui.main.MainActivity

/**
 * Provides the subcomponents as dependencies (e.g. AuthComponent, MainComponent
 */
@Module
abstract class ActivityBuildersModule {

    @AuthScope
    @ContributesAndroidInjector(
        modules = [AuthModule::class, AuthFragmentBuildersModule::class, AuthViewModelModule::class]
    )
    abstract fun contributeAuthActivity(): AuthActivity

    @MainScope
    @ContributesAndroidInjector(
        modules = [MainModule::class, MainFragmentBuildersModule::class, MainViewModelModule::class]
    )
    abstract fun contributeMainActivity(): MainActivity
}