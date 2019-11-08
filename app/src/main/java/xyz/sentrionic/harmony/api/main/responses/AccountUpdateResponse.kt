package xyz.sentrionic.harmony.api.main.responses

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class AccountUpdateResponse(

    @SerializedName("response")
    @Expose
    var response: String,

    @SerializedName("pk")
    @Expose
    var pk: Int,

    @SerializedName("username")
    @Expose
    var username: String,

    @SerializedName("email")
    @Expose
    var email: String,

    @SerializedName("display_name")
    @Expose
    var display_name: String,

    @SerializedName("description")
    @Expose
    var description: String,

    @SerializedName("image")
    @Expose
    var image: String,

    @SerializedName("website")
    @Expose
    var website: String,

    @SerializedName("posts")
    @Expose
    var posts: Int,

    @SerializedName("followers")
    @Expose
    var followers: Int,

    @SerializedName("following")
    @Expose
    var following: Int

)