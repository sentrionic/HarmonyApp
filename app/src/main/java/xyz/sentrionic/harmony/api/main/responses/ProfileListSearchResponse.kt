package xyz.sentrionic.harmony.api.main.responses

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import xyz.sentrionic.harmony.models.Profile

class ProfileListSearchResponse(

    @SerializedName("results")
    @Expose
    var results: List<Profile>,

    @SerializedName("detail")
    @Expose
    var detail: String
) {

    override fun toString(): String {
        return "ProfileListSearchResponse(results=$results, detail='$detail')"
    }
}