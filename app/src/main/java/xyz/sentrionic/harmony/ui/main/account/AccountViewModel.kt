package xyz.sentrionic.harmony.ui.main.account

import android.net.Uri
import androidx.lifecycle.LiveData
import xyz.sentrionic.harmony.models.AccountProperties
import xyz.sentrionic.harmony.repository.main.AccountRepository
import xyz.sentrionic.harmony.session.SessionManager
import xyz.sentrionic.harmony.ui.BaseViewModel
import xyz.sentrionic.harmony.ui.DataState
import xyz.sentrionic.harmony.ui.Loading
import xyz.sentrionic.harmony.ui.main.account.state.AccountStateEvent
import xyz.sentrionic.harmony.ui.main.account.state.AccountStateEvent.*
import xyz.sentrionic.harmony.ui.main.account.state.AccountViewState
import xyz.sentrionic.harmony.ui.main.account.state.AccountViewState.*
import xyz.sentrionic.harmony.util.AbsentLiveData
import javax.inject.Inject

class AccountViewModel
@Inject
constructor(
    val sessionManager: SessionManager,
    val accountRepository: AccountRepository
) : BaseViewModel<AccountStateEvent, AccountViewState>() {

    override fun handleStateEvent(stateEvent: AccountStateEvent): LiveData<DataState<AccountViewState>> {
        when (stateEvent) {

            is GetAccountPropertiesEvent -> {
                return sessionManager.cachedToken.value?.let { authToken ->
                    accountRepository.getAccountProperties(authToken)
                }?: AbsentLiveData.create()
            }

            is UpdateAccountPropertiesEvent -> {
                val image = stateEvent.image
                return sessionManager.cachedToken.value?.let { authToken ->
                    authToken.account_pk?.let { pk ->

                        val newAccountProperties = AccountProperties(
                            pk,
                            stateEvent.email,
                            stateEvent.username,
                            stateEvent.display_name,
                            stateEvent.description,
                            stateEvent.website,
                            null,
                            null,
                            null,
                            null
                        )

                        accountRepository.saveAccountProperties(
                            authToken,
                            newAccountProperties,
                            image
                        )
                    }
                }?: AbsentLiveData.create()
            }

            is ChangePasswordEvent -> {
                return sessionManager.cachedToken.value?.let { authToken ->
                    accountRepository.updatePassword(
                        authToken,
                        stateEvent.currentPassword,
                        stateEvent.newPassword,
                        stateEvent.confirmNewPassword
                    )
                }?: AbsentLiveData.create()
            }

            is None -> {
                return object: LiveData<DataState<AccountViewState>>() {
                    override fun onActive() {
                        super.onActive()
                        value = DataState(null, Loading(false), null)
                    }
                }
            }
        }
    }

    fun setAccountPropertiesData(accountProperties: AccountProperties) {
        val update = getCurrentViewStateOrNew()
        if (update.accountProperties == accountProperties) {
            return
        }
        update.accountProperties = accountProperties
        _viewState.value = update
    }

    override fun initNewViewState(): AccountViewState {
        return AccountViewState()
    }

    fun logout() {
        sessionManager.logout()
    }

    fun cancelActiveJobs() {
        accountRepository.cancelActiveJobs() // cancel active jobs
        handlePendingData() // hide progress bar
    }

    fun handlePendingData() {
        setStateEvent(None())
    }

    fun getUpdatedProfilePictureUri(): Uri? {
        getCurrentViewStateOrNew().let {
            it.newProfileField?.newImageUri?.let {
                return it
            }
        }
        return null
    }

    fun setNewProfilePicture(uri: Uri?) {
        val update = getCurrentViewStateOrNew()
        val newProfileField = NewProfileField()
        uri?.let { newProfileField.newImageUri = it }
        update.newProfileField = newProfileField
        setViewState(update)
    }

    override fun onCleared() {
        super.onCleared()
        cancelActiveJobs()
    }

}