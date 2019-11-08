package xyz.sentrionic.harmony.api.main.responses

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class StorySearchResponse(

    @SerializedName("pk")
    @Expose
    var pk: Int,

    @SerializedName("caption")
    @Expose
    var caption: String,

    @SerializedName("slug")
    @Expose
    var slug: String,

    @SerializedName("tags")
    @Expose
    var tags: String,

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
    var likes: Int,

    @SerializedName("liked")
    @Expose
    var liked: Boolean,

    @SerializedName("profile_image")
    @Expose
    var profile_image: String
) {

    override fun toString(): String {
        return "StorySearchResponse(pk=$pk, caption='$caption', slug='$slug', tags='$tags', image='$image', date_published='$date_published', username='$username', likes=$likes, liked=$liked, profile_image='$profile_image')"
    }
}
