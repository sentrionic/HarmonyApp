package xyz.sentrionic.harmony.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

@Entity(tableName = "profiles")
data class Profile(

    @SerializedName("pk")
    @Expose
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "pk") var pk: Int,

    @SerializedName("username")
    @Expose
    @ColumnInfo(name = "username") var username: String,

    @SerializedName("display_name")
    @Expose
    @ColumnInfo(name = "display_name") var display_name: String,

    @SerializedName("description")
    @Expose
    @ColumnInfo(name = "description") var description: String,

    @SerializedName("website")
    @Expose
    @ColumnInfo(name = "website") var website: String,

    @SerializedName("image")
    @Expose
    @ColumnInfo(name = "image") var image: String?,

    @SerializedName("posts")
    @Expose
    @ColumnInfo(name = "posts") var posts: Int?,

    @SerializedName("followers")
    @Expose
    @ColumnInfo(name = "followers") var followers: Int?,

    @SerializedName("following")
    @Expose
    @ColumnInfo(name = "following") var following: Int?,

    @SerializedName("follow")
    @Expose
    @ColumnInfo(name = "follow") var isFollowing: Boolean
)
{

    override fun toString(): String {
        return "Profile(pk=$pk, username='$username', display_name='$display_name', description='$description', website='$website', image=$image, posts=$posts, followers=$followers, following=$following, isFollowing=$isFollowing)"
    }

}