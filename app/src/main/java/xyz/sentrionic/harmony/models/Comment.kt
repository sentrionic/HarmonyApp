package xyz.sentrionic.harmony.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

@Entity(tableName = "comment")
data class Comment(

    @SerializedName("pk")
    @Expose
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "pk")
    var pk: Int,

    @SerializedName("comment")
    @Expose
    @ColumnInfo(name = "comment")
    var comment: String,

    @SerializedName("image")
    @Expose
    @ColumnInfo(name = "image")
    var image: String,

    @SerializedName("date_published")
    @Expose
    @ColumnInfo(name = "date_published")
    var date_published: Long,

    @SerializedName("username")
    @Expose
    @ColumnInfo(name = "username")
    var username: String,

    @SerializedName("likes")
    @Expose
    @ColumnInfo(name = "likes")
    var likes: Int
) {
    override fun toString(): String {
        return "Comment(pk=$pk, comment='$comment', image='$image', date_published=$date_published, username='$username', likes=$likes)"
    }
}