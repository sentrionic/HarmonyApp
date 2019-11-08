package xyz.sentrionic.harmony.persistence

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import xyz.sentrionic.harmony.models.AccountProperties

@Dao
interface AccountPropertiesDao {

    @Query("SELECT * FROM account_properties WHERE email = :email")
    suspend fun searchByEmail(email: String): AccountProperties?

    @Query("SELECT * FROM account_properties WHERE pk = :pk")
    fun searchByPk(pk: Int): LiveData<AccountProperties>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAndReplace(accountProperties: AccountProperties): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertOrIgnore(accountProperties: AccountProperties): Long

    @Query("""UPDATE account_properties SET 
        email = :email, 
        username = :username,
        display_name = :display_name,
        description = :description,
        website = :website,
        posts = (CASE WHEN :posts IS NULL THEN posts ELSE :posts END),
        followers = (CASE WHEN :followers IS NULL THEN followers ELSE :followers END),
        following = (CASE WHEN :following IS NULL THEN following ELSE :following END),
        image = (CASE WHEN :image IS NULL THEN image ELSE :image END)
        WHERE pk = :pk""")
    fun updateAccountProperties(pk: Int, email: String, username: String, display_name: String,
                                description: String, image: String?, website: String, posts: Int?,
                                followers: Int?, following: Int?)

    @Query("""UPDATE account_properties SET 
        email = :email, 
        username = :username,
        display_name = :display_name,
        description = :description,
        website = :website
        WHERE pk = :pk""")
    fun updateAccountPropertiesTemp(pk: Int, email: String, username: String, display_name: String,
                                description: String, website: String)
}