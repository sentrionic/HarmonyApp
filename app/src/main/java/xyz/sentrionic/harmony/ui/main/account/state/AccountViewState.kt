package xyz.sentrionic.harmony.ui.main.account.state

import android.net.Uri
import xyz.sentrionic.harmony.models.AccountProperties
import xyz.sentrionic.harmony.models.StoryPost

class AccountViewState(
    var accountProperties: AccountProperties? = null,
    var newProfileField: NewProfileField? = null,
    var accountStories: AccountStories? = null
) {

    data class NewProfileField(
        var newImageUri: Uri? = null
    )

    data class AccountStories(
        var storyList: List<StoryPost>? = null,
        var page: Int = 1,
        var isQueryInProgress: Boolean = false,
        var isQueryExhausted: Boolean = false
    )
}