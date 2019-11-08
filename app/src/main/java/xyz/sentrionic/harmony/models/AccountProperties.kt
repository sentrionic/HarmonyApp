package xyz.sentrionic.harmony.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

/**
 * Add @Expose to fields that should be read in the response
 */

@Entity(tableName = "account_properties")
data class AccountProperties(

    @SerializedName("pk")
    @Expose
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "pk") var pk: Int,

    @SerializedName("email")
    @Expose
    @ColumnInfo(name = "email") var email: String,

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
    @ColumnInfo(name = "following") var following: Int?
)
{

    override fun equals(other: Any?): Boolean {
        if (javaClass != other?.javaClass) return false

        other as AccountProperties

        if (pk != other.pk) return false
        if (email != other.email) return false
        if (username != other.username) return false
        if (display_name != other.display_name) return false
        if (description != other.description) return false
        if (website != other.website) return false
        if (image != other.image) return false
        if (followers != other.followers) return false
        if (following != other.following) return false
        if (posts != other.posts) return false

        return true
    }

}