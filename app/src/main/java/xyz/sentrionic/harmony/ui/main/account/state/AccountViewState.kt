package xyz.sentrionic.harmony.ui.main.account.state

import android.net.Uri
import xyz.sentrionic.harmony.models.AccountProperties

class AccountViewState(
    var accountProperties: AccountProperties? = null,
    var newProfileField: NewProfileField? = null
) {

    data class NewProfileField(
        var newImageUri: Uri? = null
    )
}