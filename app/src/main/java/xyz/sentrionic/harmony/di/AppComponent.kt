package xyz.sentrionic.harmony.di

import android.app.Application
import dagger.BindsInstance
import dagger.Component
import dagger.android.AndroidInjectionModule
import dagger.android.AndroidInjector
import xyz.sentrionic.harmony.BaseApplication
import xyz.sentrionic.harmony.session.SessionManager
import javax.inject.Singleton

/**
 * Top-Level Dagger File that specifies application wide dependencies
 */
@Singleton
@Component(
    modules = [
        AndroidInjectionModule::class,
        AppModule::class,
        ActivityBuildersModule::class,
        ViewModelFactoryModule::class
    ]
)
interface AppComponent : AndroidInjector<BaseApplication> {

    // Special/unique dependencies that are needed everywhere
    val sessionManager: SessionManager // must add here b/c injecting into abstract class

    @Component.Builder
    interface Builder {

        @BindsInstance
        fun application(application: Application): Builder

        fun build(): AppComponent
    }
}
