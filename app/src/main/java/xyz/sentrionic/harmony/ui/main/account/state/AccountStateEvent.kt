package xyz.sentrionic.harmony.ui.main.account.state

import okhttp3.MultipartBody

sealed class AccountStateEvent{

    class GetAccountPropertiesEvent: AccountStateEvent()

    data class GetAccountStoriesEvent(val username: String): AccountStateEvent()

    data class UpdateAccountPropertiesEvent(
        val email: String,
        val username: String,
        val description: String,
        val website: String,
        val display_name: String,
        val image: MultipartBody.Part?
    ): AccountStateEvent()

    data class ChangePasswordEvent(
        val currentPassword: String,
        val newPassword: String,
        val confirmNewPassword: String
    ) : AccountStateEvent()

    class None: AccountStateEvent()
}