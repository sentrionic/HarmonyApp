package xyz.sentrionic.harmony.persistence

import androidx.room.Database
import androidx.room.RoomDatabase
import xyz.sentrionic.harmony.models.AccountProperties
import xyz.sentrionic.harmony.models.AuthToken
import xyz.sentrionic.harmony.models.Profile
import xyz.sentrionic.harmony.models.StoryPost

@Database(entities = [AuthToken::class, AccountProperties::class, StoryPost::class, Profile::class], version = 1)
abstract class AppDatabase: RoomDatabase() {

    abstract fun getAuthTokenDao(): AuthTokenDao

    abstract fun getAccountPropertiesDao(): AccountPropertiesDao

    abstract fun getStoryPostDao() : StoryPostDao

    abstract fun getProfileDao() : ProfileDao

    companion object {
        val DATABASE_NAME: String = "app_db"
    }
}