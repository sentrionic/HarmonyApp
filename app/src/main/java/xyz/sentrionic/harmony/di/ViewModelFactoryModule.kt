package xyz.sentrionic.harmony.di

import androidx.lifecycle.ViewModelProvider
import dagger.Binds
import dagger.Module
import xyz.sentrionic.harmony.viewmodels.ViewModelProviderFactory

/**
 * Generates the viewmodels
 */
@Module
abstract class ViewModelFactoryModule {

    @Binds
    abstract fun bindViewModelFactory(factory: ViewModelProviderFactory): ViewModelProvider.Factory
}