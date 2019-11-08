package xyz.sentrionic.harmony.di.auth

import androidx.lifecycle.ViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import xyz.sentrionic.harmony.di.ViewModelKey
import xyz.sentrionic.harmony.ui.auth.AuthViewModel

/**
 * Provides the AuthViewModel
 */
@Module
abstract class AuthViewModelModule {

    @Binds
    @IntoMap
    @ViewModelKey(AuthViewModel::class)
    abstract fun bindAuthViewModel(authViewModel: AuthViewModel): ViewModel

}