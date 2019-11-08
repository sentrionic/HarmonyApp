package xyz.sentrionic.harmony.di.auth

import android.content.SharedPreferences
import dagger.Module
import dagger.Provides
import retrofit2.Retrofit
import xyz.sentrionic.harmony.api.auth.HarmonyApiAuthService
import xyz.sentrionic.harmony.persistence.AccountPropertiesDao
import xyz.sentrionic.harmony.persistence.AuthTokenDao
import xyz.sentrionic.harmony.repository.auth.AuthRepository
import xyz.sentrionic.harmony.session.SessionManager

/**
 * Dependencies for authentication
 */
@Module
class AuthModule {

    @AuthScope
    @Provides
    fun provideHarmonyApiAuthService(retrofitBuilder: Retrofit.Builder): HarmonyApiAuthService {
        return retrofitBuilder
            .build()
            .create(HarmonyApiAuthService::class.java)
    }

    @AuthScope
    @Provides
    fun provideAuthRepository(
        sessionManager: SessionManager,
        authTokenDao: AuthTokenDao,
        accountPropertiesDao: AccountPropertiesDao,
        openApiAuthService: HarmonyApiAuthService,
        preferences: SharedPreferences,
        editor: SharedPreferences.Editor
    ): AuthRepository {
        return AuthRepository(
            authTokenDao,
            accountPropertiesDao,
            openApiAuthService,
            sessionManager,
            preferences,
            editor
        )
    }

}