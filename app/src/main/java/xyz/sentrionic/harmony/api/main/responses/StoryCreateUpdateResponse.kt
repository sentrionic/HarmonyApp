package xyz.sentrionic.harmony.api.main.responses

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class StoryCreateUpdateResponse(

    @SerializedName("response")
    @Expose
    var response: String,

    @SerializedName("pk")
    @Expose
    var pk: Int,

    @SerializedName("caption")
    @Expose
    var caption: String,

    @SerializedName("tags")
    @Expose
    var tags: String,

    @SerializedName("slug")
    @Expose
    var slug: String,

    @SerializedName("date_updated")
    @Expose
    var date_updated: String,

    @SerializedName("image")
    @Expose
    var image: String,

    @SerializedName("username")
    @Expose
    var username: String,

    @SerializedName("profile_image")
    @Expose
    var profile_image: String

)