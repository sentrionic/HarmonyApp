package xyz.sentrionic.harmony

import dagger.android.AndroidInjector
import dagger.android.support.DaggerApplication
import xyz.sentrionic.harmony.di.DaggerAppComponent

/**
 * Base for Dagger Setup
 */
class BaseApplication : DaggerApplication() {

    override fun applicationInjector(): AndroidInjector<out DaggerApplication> {
        return DaggerAppComponent.builder().application(this).build()
    }

}