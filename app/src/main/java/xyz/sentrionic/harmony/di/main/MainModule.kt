package xyz.sentrionic.harmony.di.main

import dagger.Module
import dagger.Provides
import retrofit2.Retrofit
import xyz.sentrionic.harmony.api.main.HarmonyMainService
import xyz.sentrionic.harmony.persistence.AccountPropertiesDao
import xyz.sentrionic.harmony.persistence.AppDatabase
import xyz.sentrionic.harmony.persistence.StoryPostDao
import xyz.sentrionic.harmony.repository.main.AccountRepository
import xyz.sentrionic.harmony.repository.main.CreateStoryRepository
import xyz.sentrionic.harmony.repository.main.StoryRepository
import xyz.sentrionic.harmony.session.SessionManager

@Module
class MainModule {

    @MainScope
    @Provides
    fun provideOpenApiMainService(retrofitBuilder: Retrofit.Builder): HarmonyMainService {
        return retrofitBuilder
            .build()
            .create(HarmonyMainService::class.java)
    }

    @MainScope
    @Provides
    fun provideAccountRepository(
        harmonyMainService: HarmonyMainService,
        accountPropertiesDao: AccountPropertiesDao,
        sessionManager: SessionManager
    ): AccountRepository {
        return AccountRepository(
            harmonyMainService,
            accountPropertiesDao,
            sessionManager
        )
    }

    @MainScope
    @Provides
    fun provideStoryPostDao(db: AppDatabase): StoryPostDao {
        return db.getStoryPostDao()
    }

    @MainScope
    @Provides
    fun provideStoryRepository(
        harmonyMainService: HarmonyMainService,
        storyPostDao: StoryPostDao,
        sessionManager: SessionManager
    ): StoryRepository {
        return StoryRepository(harmonyMainService, storyPostDao, sessionManager)
    }

    @MainScope
    @Provides
    fun provideCreateStoryRepository(
        harmonyMainService: HarmonyMainService,
        storyPostDao: StoryPostDao,
        sessionManager: SessionManager
    ): CreateStoryRepository {
        return CreateStoryRepository(harmonyMainService, storyPostDao, sessionManager)
    }

}