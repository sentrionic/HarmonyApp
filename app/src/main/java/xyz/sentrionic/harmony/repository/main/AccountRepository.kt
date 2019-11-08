package xyz.sentrionic.harmony.repository.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.switchMap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.withContext
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import xyz.sentrionic.harmony.api.GenericResponse
import xyz.sentrionic.harmony.api.main.HarmonyMainService
import xyz.sentrionic.harmony.api.main.responses.AccountUpdateResponse
import xyz.sentrionic.harmony.models.AccountProperties
import xyz.sentrionic.harmony.models.AuthToken
import xyz.sentrionic.harmony.persistence.AccountPropertiesDao
import xyz.sentrionic.harmony.repository.JobManager
import xyz.sentrionic.harmony.repository.NetworkBoundResource
import xyz.sentrionic.harmony.session.SessionManager
import xyz.sentrionic.harmony.ui.DataState
import xyz.sentrionic.harmony.ui.Response
import xyz.sentrionic.harmony.ui.ResponseType
import xyz.sentrionic.harmony.ui.main.account.state.AccountViewState
import xyz.sentrionic.harmony.util.AbsentLiveData
import xyz.sentrionic.harmony.util.ApiSuccessResponse
import xyz.sentrionic.harmony.util.GenericApiResponse
import javax.inject.Inject

class AccountRepository
@Inject
constructor(
    val harmonyMainService: HarmonyMainService,
    val accountPropertiesDao: AccountPropertiesDao,
    val sessionManager: SessionManager
) : JobManager("AccountRepository") {

    private val TAG: String = "AppDebug"

    fun getAccountProperties(authToken: AuthToken): LiveData<DataState<AccountViewState>> {
        return object :
            NetworkBoundResource<AccountProperties, AccountProperties, AccountViewState>(
                sessionManager.isConnectedToTheInternet(),
                true,
                false,
                true
            ) {

            // if network is down, view the cache and return
            override suspend fun createCacheRequestAndReturn() {
                withContext(Dispatchers.Main) {

                    // finishing by viewing db cache
                    result.addSource(loadFromCache()) { viewState ->
                        onCompleteJob(DataState.data(viewState, null))
                    }
                }
            }

            override suspend fun handleApiSuccessResponse(response: ApiSuccessResponse<AccountProperties>) {
                updateLocalDb(response.body)

                createCacheRequestAndReturn()
            }

            override fun loadFromCache(): LiveData<AccountViewState> {
                return accountPropertiesDao.searchByPk(authToken.account_pk!!)
                    .switchMap {
                        object : LiveData<AccountViewState>() {
                            override fun onActive() {
                                super.onActive()
                                value = AccountViewState(it)
                            }
                        }
                    }
            }

            override suspend fun updateLocalDb(cacheObject: AccountProperties?) {
                cacheObject?.let {
                    accountPropertiesDao.updateAccountProperties(
                        cacheObject.pk,
                        cacheObject.email,
                        cacheObject.username,
                        cacheObject.display_name,
                        cacheObject.description,
                        cacheObject.image!!,
                        cacheObject.website,
                        cacheObject.posts,
                        cacheObject.followers,
                        cacheObject.following
                    )
                }
            }

            override fun createCall(): LiveData<GenericApiResponse<AccountProperties>> {
                return harmonyMainService
                    .getAccountProperties(
                        "Token ${authToken.token!!}"
                    )
            }


            override fun setJob(job: Job) {
                addJob("getAccountProperties", job)
            }


        }.asLiveData()
    }

    fun saveAccountProperties(
        authToken: AuthToken,
        accountProperties: AccountProperties,
        image: MultipartBody.Part?
    ): LiveData<DataState<AccountViewState>> {
        return object : NetworkBoundResource<AccountUpdateResponse, AccountProperties, AccountViewState>(
            sessionManager.isConnectedToTheInternet(),
            true,
            true,
            false
        ) {

            // not applicable
            override suspend fun createCacheRequestAndReturn() {}

            override suspend fun handleApiSuccessResponse(response: ApiSuccessResponse<AccountUpdateResponse>) {

                val updatedAccountProperties = AccountProperties(
                    response.body.pk,
                    response.body.email,
                    response.body.username,
                    response.body.display_name,
                    response.body.description,
                    response.body.image,
                    response.body.website,
                    response.body.posts,
                    response.body.followers,
                    response.body.following
                )

                updateLocalDb(updatedAccountProperties) // The update does not return a CacheObject

                withContext(Dispatchers.Main) {
                    // finish with success response
                    onCompleteJob(
                        DataState.data(
                            data = null,
                            response = Response(response.body.response, ResponseType.Toast())
                        )
                    )
                }
            }

            // not used in this case
            override fun loadFromCache(): LiveData<AccountViewState> {
                return AbsentLiveData.create()
            }

            override fun createCall(): LiveData<GenericApiResponse<AccountUpdateResponse>> {
                return harmonyMainService.saveAccountProperties(
                    "Token ${authToken.token!!}",
                    RequestBody.create(MediaType.parse("text/plain"), accountProperties.email),
                    RequestBody.create(MediaType.parse("text/plain"), accountProperties.username),
                    RequestBody.create(MediaType.parse("text/plain"), accountProperties.display_name),
                    RequestBody.create(MediaType.parse("text/plain"), accountProperties.description),
                    RequestBody.create(MediaType.parse("text/plain"), accountProperties.website),
                    image
                )
            }

            override suspend fun updateLocalDb(cacheObject: AccountProperties?) {
                return accountPropertiesDao.updateAccountProperties(
                    accountProperties.pk,
                    accountProperties.email,
                    accountProperties.username,
                    accountProperties.display_name,
                    accountProperties.description,
                    accountProperties.image,
                    accountProperties.website,
                    accountProperties.posts,
                    accountProperties.followers,
                    accountProperties.following
                )
            }

            override fun setJob(job: Job) {
                addJob("saveAccountProperties", job)
            }

        }.asLiveData()
    }

    fun updatePassword(authToken: AuthToken, currentPassword: String, newPassword: String, confirmNewPassword: String): LiveData<DataState<AccountViewState>> {
        return object: NetworkBoundResource<GenericResponse, Any, AccountViewState>(
            sessionManager.isConnectedToTheInternet(),
            true,
            true,
            false
        ){

            // not applicable
            override suspend fun createCacheRequestAndReturn() {}

            override suspend fun handleApiSuccessResponse(response: ApiSuccessResponse<GenericResponse>) {
                withContext(Dispatchers.Main){
                    // finish with success response
                    onCompleteJob(
                        DataState.data(null,
                            Response(response.body.response, ResponseType.Toast())
                        ))
                }
            }

            // not used in this case
            override fun loadFromCache(): LiveData<AccountViewState> {
                return AbsentLiveData.create()
            }

            override fun createCall(): LiveData<GenericApiResponse<GenericResponse>> {
                return harmonyMainService.updatePassword(
                    "Token ${authToken.token!!}",
                    currentPassword,
                    newPassword,
                    confirmNewPassword
                )
            }

            // not used in this case
            override suspend fun updateLocalDb(cacheObject: Any?) {}

            override fun setJob(job: Job) {
                addJob("updatePassword", job)
            }

        }.asLiveData()
    }
}