package xyz.sentrionic.harmony.persistence

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import xyz.sentrionic.harmony.models.Profile
import xyz.sentrionic.harmony.util.Constants

@Dao
interface ProfileDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(profile: Profile): Long

    @Query("SELECT * FROM profiles WHERE username LIKE '%' || :query || '%' OR display_name LIKE '%' || :query || '%' LIMIT (:page * :pageSize)")
    fun getProfiles(query: String, page: Int, pageSize: Int = Constants.PAGINATION_PAGE_SIZE): LiveData<List<Profile>>
}