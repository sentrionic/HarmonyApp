package xyz.sentrionic.harmony.repository.main

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.switchMap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.withContext
import xyz.sentrionic.harmony.api.GenericResponse
import xyz.sentrionic.harmony.api.main.HarmonyMainService
import xyz.sentrionic.harmony.api.main.responses.ProfileListSearchResponse
import xyz.sentrionic.harmony.models.AuthToken
import xyz.sentrionic.harmony.models.Profile
import xyz.sentrionic.harmony.persistence.ProfileDao
import xyz.sentrionic.harmony.repository.JobManager
import xyz.sentrionic.harmony.repository.NetworkBoundResource
import xyz.sentrionic.harmony.session.SessionManager
import xyz.sentrionic.harmony.ui.DataState
import xyz.sentrionic.harmony.ui.Response
import xyz.sentrionic.harmony.ui.ResponseType
import xyz.sentrionic.harmony.ui.main.story.state.StoryViewState
import xyz.sentrionic.harmony.util.AbsentLiveData
import xyz.sentrionic.harmony.util.ApiSuccessResponse
import xyz.sentrionic.harmony.util.Constants.Companion.PAGINATION_PAGE_SIZE
import xyz.sentrionic.harmony.util.ErrorHandling.Companion.ERROR_UNKNOWN
import xyz.sentrionic.harmony.util.GenericApiResponse
import xyz.sentrionic.harmony.util.SuccessHandling
import javax.inject.Inject

class SearchRepository
@Inject
constructor(
    val harmonyMainService: HarmonyMainService,
    val profileDao: ProfileDao,
    val sessionManager: SessionManager
) : JobManager("SearchRepository") {

    private val TAG: String = "AppDebug"

    fun searchUserProfiles(
        authToken: AuthToken,
        query: String,
        filterAndOrder: String,
        page: Int
    ): LiveData<DataState<StoryViewState>> {
        return object :
            NetworkBoundResource<ProfileListSearchResponse, List<Profile>, StoryViewState>(
                sessionManager.isConnectedToTheInternet(),
                true,
                false,
                true
            ) {
            // if network is down, view cache only and return
            override suspend fun createCacheRequestAndReturn() {
                withContext(Dispatchers.Main) {

                    // finishing by viewing db cache
                    result.addSource(loadFromCache()) { viewState ->
                        viewState.userFields.isQueryInProgress = false

                        if (page * PAGINATION_PAGE_SIZE > viewState.userFields.userList.size) {
                            viewState.userFields.isQueryExhausted = true
                        }

                        onCompleteJob(DataState.data(viewState, null))
                    }
                }
            }

            override suspend fun handleApiSuccessResponse(
                response: ApiSuccessResponse<ProfileListSearchResponse>
            ) {

                val profileList: ArrayList<Profile> = ArrayList()
                for (profileResponse in response.body.results) {
                    profileList.add(
                        Profile(
                            pk = profileResponse.pk,
                            username = profileResponse.username,
                            display_name = profileResponse.display_name,
                            description = profileResponse.description,
                            website = profileResponse.website,
                            image = profileResponse.image,
                            posts = profileResponse.posts,
                            followers = profileResponse.followers,
                            following = profileResponse.following,
                            isFollowing = profileResponse.isFollowing
                        )
                    )
                }

                updateLocalDb(profileList)
                createCacheRequestAndReturn()
            }

            override fun createCall(): LiveData<GenericApiResponse<ProfileListSearchResponse>> {
                return harmonyMainService.searchProfiles(
                    "Token ${authToken.token!!}",
                    query = query,
                    ordering = filterAndOrder,
                    page = page
                )
            }

            override fun loadFromCache(): LiveData<StoryViewState> {
                return profileDao.getProfiles(query, page)
                    .switchMap {
                        object : LiveData<StoryViewState>() {
                            override fun onActive() {
                                super.onActive()
                                value = StoryViewState(
                                    StoryViewState.StoryFields(),
                                    StoryViewState.ViewStoryFields(),
                                    StoryViewState.UserFields(
                                        userList = it,
                                        isQueryInProgress = true
                                    )
                                )
                            }
                        }
                    }
            }

            override suspend fun updateLocalDb(cacheObject: List<Profile>?) {
                // loop through list and update the local db
                if (cacheObject != null) {
                    withContext(IO) {
                        for (profile in cacheObject) {
                            try {
                                // Launch each insert as a separate job to be executed in parallel
                                Log.d(TAG, "updateLocalDb: inserting story: ${profile}")
                                profileDao.insert(profile)

                            } catch (e: Exception) {
                                Log.e(
                                    TAG,
                                    "updateLocalDb: error updating cache data on blog post with slug: ${profile.username}. " +
                                            "${e.message}"
                                )
                                // Could send an error report here or something but I don't think you should throw an error to the UI
                                // Since there could be many blog posts being inserted/updated.
                            }
                        }
                    }
                } else {
                    Log.d(TAG, "updateLocalDb: story post list is null")
                }
            }

            override fun setJob(job: Job) {
                addJob("searchUserProfiles", job)
            }

        }.asLiveData()
    }

    fun toggleFollow(
        authToken: AuthToken,
        profile: Profile
    ) : LiveData<DataState<StoryViewState>> {
        return object : NetworkBoundResource<GenericResponse, Profile, StoryViewState>(
            sessionManager.isConnectedToTheInternet(),
            true,
            true,
            false
        ) {
            override suspend fun createCacheRequestAndReturn() {}

            override suspend fun handleApiSuccessResponse(response: ApiSuccessResponse<GenericResponse>) {
                when {
                    response.body.response == SuccessHandling.FOLLOWED -> {
                        profile.isFollowing = true
                        profile.followers += 1
                        updateLocalDb(profile)
                    }
                    response.body.response == SuccessHandling.UNFOLLOWED -> {
                        profile.isFollowing = false
                        profile.followers -= 1
                        updateLocalDb(profile)
                    }
                    else -> onCompleteJob(
                        DataState.error(
                            Response(
                                ERROR_UNKNOWN,
                                ResponseType.Dialog()
                            )
                        )
                    )
                }

                withContext(Dispatchers.Main){
                    // finish with success response
                    onCompleteJob(
                        DataState.data(
                            data = null,
                            response = null
                        )
                    )
                }
            }

            override fun createCall(): LiveData<GenericApiResponse<GenericResponse>> {
                return harmonyMainService.toggleFollow(
                    "Token ${authToken.token!!}",
                    profile.username
                )
            }

            override fun loadFromCache(): LiveData<StoryViewState> {
                return AbsentLiveData.create()
            }

            override suspend fun updateLocalDb(cacheObject: Profile?) {
                cacheObject?.let { profile ->
                    profileDao.updateProfile(
                        profile.pk,
                        profile.followers,
                        profile.isFollowing
                    )
                }
            }

            override fun setJob(job: Job) {
                addJob("toggleLike", job)
            }

        }.asLiveData()
    }

    fun getProfile(authToken: AuthToken, username: String): LiveData<DataState<StoryViewState>> {
        return object :
            NetworkBoundResource<Profile, Profile, StoryViewState>(
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

            override suspend fun handleApiSuccessResponse(response: ApiSuccessResponse<Profile>) {
                updateLocalDb(response.body)

                createCacheRequestAndReturn()
            }

            override fun loadFromCache(): LiveData<StoryViewState> {
                return profileDao.getProfile(username)
                    .switchMap {
                        object : LiveData<StoryViewState>() {
                            override fun onActive() {
                                super.onActive()
                                value = StoryViewState(
                                    StoryViewState.StoryFields(),
                                    StoryViewState.ViewStoryFields(),
                                    StoryViewState.UserFields(),
                                    StoryViewState.ViewProfileField(it, it.isFollowing, it.username)
                                )
                            }
                        }
                    }
            }

            override suspend fun updateLocalDb(cacheObject: Profile?) {
                cacheObject?.let {
                    profileDao.insert(cacheObject)
                }
            }

            override fun createCall(): LiveData<GenericApiResponse<Profile>> {
                return harmonyMainService
                    .getProfile(
                        "Token ${authToken.token!!}",
                        username
                    )
            }


            override fun setJob(job: Job) {
                addJob("getProfile", job)
            }


        }.asLiveData()
    }
}