package xyz.sentrionic.harmony.api.main.responses

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class CommentResponse(
    @SerializedName("pk")
    @Expose
    var pk: Int,

    @SerializedName("comment")
    @Expose
    var comment: String,

    @SerializedName("image")
    @Expose
    var image: String,

    @SerializedName("date_published")
    @Expose
    var date_published: String,

    @SerializedName("username")
    @Expose
    var username: String,

    @SerializedName("likes")
    @Expose
    var likes: Int
)
