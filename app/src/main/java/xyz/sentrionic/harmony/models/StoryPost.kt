package xyz.sentrionic.harmony.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "story_post")
data class StoryPost(

    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "pk")
    var pk: Int,

    @ColumnInfo(name = "slug")
    var slug: String,

    @ColumnInfo(name = "caption")
    var caption: String,

    @ColumnInfo(name = "image")
    var image: String,

    @ColumnInfo(name = "date_published")
    var date_published: Long,

    @ColumnInfo(name = "username")
    var username: String,

    @ColumnInfo(name = "tags")
    var tags: String,

    @ColumnInfo(name = "likes")
    var likes: Int,

    @ColumnInfo(name = "liked")
    var liked: Boolean,

    @ColumnInfo(name = "profile_image")
    var profile_image: String

) {

    override fun toString(): String {
        return "StoryPost(pk=$pk, slug='$slug', caption='$caption', image='$image', date_published=$date_published, username='$username', tags='$tags', likes=$likes, liked=$liked, profile_image='$profile_image')"
    }
}