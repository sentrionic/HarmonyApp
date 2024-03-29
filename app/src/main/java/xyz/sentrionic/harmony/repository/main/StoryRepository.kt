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
import xyz.sentrionic.harmony.api.main.responses.StoryListSearchResponse
import xyz.sentrionic.harmony.models.AuthToken
import xyz.sentrionic.harmony.models.StoryPost
import xyz.sentrionic.harmony.persistence.StoryPostDao
import xyz.sentrionic.harmony.persistence.returnOrderedStoryQuery
import xyz.sentrionic.harmony.repository.JobManager
import xyz.sentrionic.harmony.repository.NetworkBoundResource
import xyz.sentrionic.harmony.session.SessionManager
import xyz.sentrionic.harmony.ui.DataState
import xyz.sentrionic.harmony.ui.Response
import xyz.sentrionic.harmony.ui.ResponseType
import xyz.sentrionic.harmony.ui.main.story.state.StoryViewState
import xyz.sentrionic.harmony.ui.main.story.state.StoryViewState.StoryFields
import xyz.sentrionic.harmony.ui.main.story.state.StoryViewState.ViewStoryFields
import xyz.sentrionic.harmony.util.*
import xyz.sentrionic.harmony.util.Constants.Companion.PAGINATION_PAGE_SIZE
import xyz.sentrionic.harmony.util.ErrorHandling.Companion.ERROR_UNKNOWN
import xyz.sentrionic.harmony.util.SuccessHandling.Companion.RESPONSE_HAS_PERMISSION_TO_EDIT
import xyz.sentrionic.harmony.util.SuccessHandling.Companion.RESPONSE_NO_PERMISSION_TO_EDIT
import xyz.sentrionic.harmony.util.SuccessHandling.Companion.SUCCESS_STORY_DELETED
import javax.inject.Inject

class StoryRepository
@Inject
constructor(
    val harmonyMainService: HarmonyMainService,
    val storyPostDao: StoryPostDao,
    val sessionManager: SessionManager
) : JobManager("StoryRepository") {

    private val TAG: String = "AppDebug"

    fun searchStoryPosts(
        authToken: AuthToken,
        query: String,
        filterAndOrder: String,
        page: Int
    ): LiveData<DataState<StoryViewState>> {
        return object :
            NetworkBoundResource<StoryListSearchResponse, List<StoryPost>, StoryViewState>(
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
                        viewState.storyFields.isQueryInProgress = false

                        if (page * PAGINATION_PAGE_SIZE > viewState.storyFields.storyList.size) {
                            viewState.storyFields.isQueryExhausted = true
                        }

                        onCompleteJob(DataState.data(viewState, null))
                    }
                }
            }

            override suspend fun handleApiSuccessResponse(
                response: ApiSuccessResponse<StoryListSearchResponse>
            ) {

                val storyPostList: ArrayList<StoryPost> = ArrayList()
                for (storyPostResponse in response.body.results) {
                    storyPostList.add(
                        StoryPost(
                            pk = storyPostResponse.pk,
                            slug = storyPostResponse.slug,
                            caption = storyPostResponse.caption,
                            image = storyPostResponse.image,
                            date_published = DateUtils.convertServerStringDateToLong(
                                storyPostResponse.date_published
                            ),
                            username = storyPostResponse.username,
                            tags = storyPostResponse.tags,
                            likes = storyPostResponse.likes,
                            liked = storyPostResponse.liked,
                            profile_image = storyPostResponse.profile_image
                        )
                    )
                }
                updateLocalDb(storyPostList)

                createCacheRequestAndReturn()
            }

            override fun createCall(): LiveData<GenericApiResponse<StoryListSearchResponse>> {
                return harmonyMainService.searchListStoryPosts(
                    "Token ${authToken.token!!}",
                    query = query,
                    ordering = filterAndOrder,
                    page = page
                )
            }

            override fun loadFromCache(): LiveData<StoryViewState> {
                return storyPostDao.returnOrderedStoryQuery(query, filterAndOrder, page)
                    .switchMap {
                        object : LiveData<StoryViewState>() {
                            override fun onActive() {
                                super.onActive()
                                value = StoryViewState(
                                    StoryFields(
                                        storyList = it,
                                        isQueryInProgress = true
                                    )
                                )
                            }
                        }
                    }
            }

            override suspend fun updateLocalDb(cacheObject: List<StoryPost>?) {
                // loop through list and update the local db
                if (cacheObject != null) {
                    withContext(IO) {
                        for (storyPost in cacheObject) {
                            try {
                                // Launch each insert as a separate job to be executed in parallel
                                Log.d(TAG, "updateLocalDb: inserting story: ${storyPost}")
                                storyPostDao.insert(storyPost)

                            } catch (e: Exception) {
                                Log.e(
                                    TAG,
                                    "updateLocalDb: error updating cache data on blog post with slug: ${storyPost.slug}. " +
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
                addJob("searchStoryPost", job)
            }

        }.asLiveData()
    }

    fun isAuthorOfStoryPost(
        authToken: AuthToken,
        slug: String
    ): LiveData<DataState<StoryViewState>> {
        return object: NetworkBoundResource<GenericResponse, Any, StoryViewState>(
            sessionManager.isConnectedToTheInternet(),
            true,
            true,
            false
        ){

            // not applicable
            override suspend fun createCacheRequestAndReturn() {}

            override suspend fun handleApiSuccessResponse(response: ApiSuccessResponse<GenericResponse>) {
                withContext(Dispatchers.Main){

                    Log.d(TAG, "handleApiSuccessResponse: ${response.body.response}")
                    when {
                        response.body.response == RESPONSE_NO_PERMISSION_TO_EDIT -> onCompleteJob(
                            DataState.data(
                                data = StoryViewState(
                                    viewStoryFields = ViewStoryFields(
                                        isAuthorOfStoryPost = false
                                    )
                                ),
                                response = null
                            )
                        )
                        response.body.response == RESPONSE_HAS_PERMISSION_TO_EDIT -> onCompleteJob(
                            DataState.data(
                                data = StoryViewState(
                                    viewStoryFields = ViewStoryFields(
                                        isAuthorOfStoryPost = true
                                    )
                                ),
                                response = null
                            )
                        )
                        else -> onErrorReturn(ERROR_UNKNOWN, shouldUseDialog = false, shouldUseToast = false)
                    }
                }
            }

            // not applicable
            override fun loadFromCache(): LiveData<StoryViewState> {
                return AbsentLiveData.create()
            }

            // Make an update and change nothing.
            // If they are not the author it will return: "You don't have permission to edit that."
            override fun createCall(): LiveData<GenericApiResponse<GenericResponse>> {
                return harmonyMainService.isAuthorOfStoryPost(
                    "Token ${authToken.token!!}",
                    slug
                )
            }

            // not applicable
            override suspend fun updateLocalDb(cacheObject: Any?) {}

            override fun setJob(job: Job) {
                addJob("isAuthorOfStoryPost", job)
            }


        }.asLiveData()
    }

    fun deleteStoryPost(
        authToken: AuthToken,
        storyPost: StoryPost
    ): LiveData<DataState<StoryViewState>>{
        return object: NetworkBoundResource<GenericResponse, StoryPost, StoryViewState>(
            sessionManager.isConnectedToTheInternet(),
            true,
            true,
            false
        ){

            // not applicable
            override suspend fun createCacheRequestAndReturn() {}

            override suspend fun handleApiSuccessResponse(response: ApiSuccessResponse<GenericResponse>) {


                if (response.body.response == SUCCESS_STORY_DELETED) {
                    updateLocalDb(storyPost)
                }
                else {
                    onCompleteJob(
                        DataState.error(
                            Response(
                                ERROR_UNKNOWN,
                                ResponseType.Dialog()
                            )
                        )
                    )
                }
            }

            // not applicable
            override fun loadFromCache(): LiveData<StoryViewState> {
                return AbsentLiveData.create()
            }

            override fun createCall(): LiveData<GenericApiResponse<GenericResponse>> {
                return harmonyMainService.deleteStoryPost(
                    "Token ${authToken.token!!}",
                    storyPost.slug
                )
            }

            override suspend fun updateLocalDb(cacheObject: StoryPost?) {
                cacheObject?.let{storyPost ->
                    storyPostDao.deleteStoryPost(storyPost)
                    onCompleteJob(
                        DataState.data(
                            null,
                            Response(SUCCESS_STORY_DELETED, ResponseType.Toast())
                        )
                    )
                }
            }

            override fun setJob(job: Job) {
                addJob("deleteStoryPost", job)
            }

        }.asLiveData()
    }

    fun toggleLike(
        authToken: AuthToken,
        storyPost: StoryPost
    ) : LiveData<DataState<StoryViewState>> {
        return object : NetworkBoundResource<GenericResponse, StoryPost, StoryViewState>(
            sessionManager.isConnectedToTheInternet(),
            true,
            true,
            false
        ) {
            override suspend fun createCacheRequestAndReturn() {}

            override suspend fun handleApiSuccessResponse(response: ApiSuccessResponse<GenericResponse>) {
                when {
                    response.body.response == SuccessHandling.LIKED_POST -> {
                        storyPost.liked = true
                        storyPost.likes += 1
                        updateLocalDb(storyPost)
                    }
                    response.body.response == SuccessHandling.UNLIKED_POST -> {
                        storyPost.liked = false
                        storyPost.likes -= 1
                        updateLocalDb(storyPost)
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
                return harmonyMainService.toggleLike(
                    "Token ${authToken.token!!}",
                    storyPost.slug
                )
            }

            override fun loadFromCache(): LiveData<StoryViewState> {
                return AbsentLiveData.create()
            }

            override suspend fun updateLocalDb(cacheObject: StoryPost?) {
                cacheObject?.let { storyPost ->
                    storyPostDao.updateStoryPostLikes(
                        storyPost.pk,
                        storyPost.likes,
                        storyPost.liked
                    )
                }
            }

            override fun setJob(job: Job) {
                addJob("toggleLike", job)
            }

        }.asLiveData()
    }

    fun getProfileStories(
        authToken: AuthToken,
        username: String,
        filterAndOrder: String,
        page: Int
    ): LiveData<DataState<StoryViewState>> {
        return object :
            NetworkBoundResource<StoryListSearchResponse, List<StoryPost>, StoryViewState>(
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
                        onCompleteJob(DataState.data(viewState, null))
                    }
                }
            }

            override suspend fun handleApiSuccessResponse(
                response: ApiSuccessResponse<StoryListSearchResponse>
            ) {

                val storyPostList: ArrayList<StoryPost> = ArrayList()
                for (storyPostResponse in response.body.results) {
                    storyPostList.add(
                        StoryPost(
                            pk = storyPostResponse.pk,
                            slug = storyPostResponse.slug,
                            caption = storyPostResponse.caption,
                            image = storyPostResponse.image,
                            date_published = DateUtils.convertServerStringDateToLong(
                                storyPostResponse.date_published
                            ),
                            username = storyPostResponse.username,
                            tags = storyPostResponse.tags,
                            likes = storyPostResponse.likes,
                            liked = storyPostResponse.liked,
                            profile_image = storyPostResponse.profile_image
                        )
                    )
                }
                updateLocalDb(storyPostList)

                createCacheRequestAndReturn()
            }

            override fun createCall(): LiveData<GenericApiResponse<StoryListSearchResponse>> {
                return harmonyMainService.searchListStoryPosts(
                    "Token ${authToken.token!!}",
                    query = username,
                    ordering = filterAndOrder,
                    page = page
                )
            }

            override fun loadFromCache(): LiveData<StoryViewState> {
                return storyPostDao.returnOrderedStoryQuery(username, filterAndOrder, page)
                    .switchMap {
                        object : LiveData<StoryViewState>() {
                            override fun onActive() {
                                super.onActive()
                                value = StoryViewState(
                                    viewProfileFields = StoryViewState.ViewProfileField(
                                        profileStories = it
                                    )
                                )
                            }
                        }
                    }
            }

            override suspend fun updateLocalDb(cacheObject: List<StoryPost>?) {
                // loop through list and update the local db
                if (cacheObject != null) {
                    withContext(IO) {
                        for (storyPost in cacheObject) {
                            try {
                                // Launch each insert as a separate job to be executed in parallel
                                Log.d(TAG, "updateLocalDb: inserting story: ${storyPost}")
                                storyPostDao.insert(storyPost)

                            } catch (e: Exception) {
                                Log.e(
                                    TAG,
                                    "updateLocalDb: error updating cache data on blog post with slug: ${storyPost.slug}. " +
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
                addJob("searchStoryPost", job)
            }

        }.asLiveData()
    }

}