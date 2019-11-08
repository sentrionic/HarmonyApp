package xyz.sentrionic.harmony.persistence

import androidx.lifecycle.LiveData
import androidx.room.*
import xyz.sentrionic.harmony.models.StoryPost
import xyz.sentrionic.harmony.util.Constants.Companion.PAGINATION_PAGE_SIZE

@Dao
interface StoryPostDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(storyPost: StoryPost): Long

    @Query("""
        SELECT * FROM story_post 
        WHERE tags LIKE '%' || :query || '%' 
        OR caption LIKE '%' || :query || '%' 
        OR username LIKE '%' || :query || '%' 
        LIMIT (:page * :pageSize)
        """)
    fun getAllStoryPosts(
        query: String,
        page: Int,
        pageSize: Int = PAGINATION_PAGE_SIZE
    ): LiveData<List<StoryPost>>

    @Query("SELECT * FROM story_post WHERE tags LIKE '%' || :query || '%' OR caption LIKE '%' || :query || '%' OR username LIKE '%' || :query || '%' ORDER BY date_published DESC LIMIT (:page * :pageSize)")
    fun searchStoryPostsOrderByDateDESC(query: String, page: Int, pageSize: Int = PAGINATION_PAGE_SIZE): LiveData<List<StoryPost>>

    @Query("SELECT * FROM story_post WHERE tags LIKE '%' || :query || '%' OR caption LIKE '%' || :query || '%' OR username LIKE '%' || :query || '%' ORDER BY date_published  ASC LIMIT (:page * :pageSize)")
    fun searchStoryPostsOrderByDateASC(query: String, page: Int, pageSize: Int = PAGINATION_PAGE_SIZE): LiveData<List<StoryPost>>

    @Query("SELECT * FROM story_post WHERE tags LIKE '%' || :query || '%' OR caption LIKE '%' || :query || '%' OR username LIKE '%' || :query || '%' ORDER BY username DESC LIMIT (:page * :pageSize)")
    fun searchStoryPostsOrderByAuthorDESC(query: String, page: Int, pageSize: Int = PAGINATION_PAGE_SIZE): LiveData<List<StoryPost>>

    @Query("SELECT * FROM story_post WHERE tags LIKE '%' || :query || '%' OR caption LIKE '%' || :query || '%' OR username LIKE '%' || :query || '%' ORDER BY username  ASC LIMIT (:page * :pageSize)")
    fun searchStoryPostsOrderByAuthorASC(query: String, page: Int, pageSize: Int = PAGINATION_PAGE_SIZE): LiveData<List<StoryPost>>

    @Delete
    suspend fun deleteStoryPost(storyPost: StoryPost)

    @Query("""
        UPDATE story_post SET caption = :caption, tags = :tags, image = :image 
        WHERE pk = :pk
        """)

    fun updateStoryPost(pk: Int, caption: String, tags: String, image: String)
}