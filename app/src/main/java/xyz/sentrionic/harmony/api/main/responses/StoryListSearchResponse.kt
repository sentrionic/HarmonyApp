package xyz.sentrionic.harmony.api.main.responses

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class StoryListSearchResponse(

    @SerializedName("results")
    @Expose
    var results: List<StorySearchResponse>,

    @SerializedName("detail")
    @Expose
    var detail: String
) {

    override fun toString(): String {
        return "StoryListSearchResponse(results=$results, detail='$detail')"
    }
}