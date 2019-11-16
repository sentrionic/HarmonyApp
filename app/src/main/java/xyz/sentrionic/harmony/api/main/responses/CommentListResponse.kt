package xyz.sentrionic.harmony.api.main.responses

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class CommentListResponse(

    @SerializedName("results")
    @Expose
    var results: List<CommentResponse>,

    @SerializedName("detail")
    @Expose
    var detail: String
) {

    override fun toString(): String {
        return "CommentListResponse(results=$results, detail='$detail')"
    }
}