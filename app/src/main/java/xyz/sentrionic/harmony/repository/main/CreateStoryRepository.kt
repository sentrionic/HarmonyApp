package xyz.sentrionic.harmony.repository.main

import androidx.lifecycle.LiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.withContext
import okhttp3.MultipartBody
import okhttp3.RequestBody
import xyz.sentrionic.harmony.api.main.HarmonyMainService
import xyz.sentrionic.harmony.api.main.responses.StoryCreateUpdateResponse
import xyz.sentrionic.harmony.models.AuthToken
import xyz.sentrionic.harmony.models.StoryPost
import xyz.sentrionic.harmony.persistence.StoryPostDao
import xyz.sentrionic.harmony.repository.JobManager
import xyz.sentrionic.harmony.repository.NetworkBoundResource
import xyz.sentrionic.harmony.session.SessionManager
import xyz.sentrionic.harmony.ui.DataState
import xyz.sentrionic.harmony.ui.Response
import xyz.sentrionic.harmony.ui.ResponseType
import xyz.sentrionic.harmony.ui.main.create_story.state.CreateStoryViewState
import xyz.sentrionic.harmony.util.AbsentLiveData
import xyz.sentrionic.harmony.util.ApiSuccessResponse
import xyz.sentrionic.harmony.util.DateUtils
import xyz.sentrionic.harmony.util.GenericApiResponse
import javax.inject.Inject

class CreateStoryRepository
@Inject
constructor(
    val harmonyMainService: HarmonyMainService,
    val storyPostDao: StoryPostDao,
    val sessionManager: SessionManager
): JobManager("CreateStoryRepository") {

    private val TAG: String = "AppDebug"

    fun createNewStoryPost(
        authToken: AuthToken,
        caption: RequestBody,
        image: MultipartBody.Part?
    ): LiveData<DataState<CreateStoryViewState>> {
        return object :
            NetworkBoundResource<StoryCreateUpdateResponse, StoryPost, CreateStoryViewState>(
                sessionManager.isConnectedToTheInternet(),
                true,
                true,
                false
            ) {

            // not applicable
            override suspend fun createCacheRequestAndReturn() {

            }

            override suspend fun handleApiSuccessResponse(response: ApiSuccessResponse<StoryCreateUpdateResponse>) {

                val updatedStoryPost = StoryPost(
                        response.body.pk,
                        response.body.slug,
                        response.body.caption,
                        response.body.image,
                        DateUtils.convertServerStringDateToLong(response.body.date_updated),
                        response.body.username,
                        response.body.tags,
                    0,
                    false,
                        response.body.profile_image
                    )

                updateLocalDb(updatedStoryPost)

                withContext(Dispatchers.Main) {
                    // finish with success response
                    onCompleteJob(
                        DataState.data(
                            null,
                            Response(response.body.response, ResponseType.Dialog())
                        )
                    )
                }
            }

            override fun createCall(): LiveData<GenericApiResponse<StoryCreateUpdateResponse>> {
                return harmonyMainService.createStory(
                    "Token ${authToken.token!!}",
                    caption,
                    image
                )
            }

            // not applicable
            override fun loadFromCache(): LiveData<CreateStoryViewState> {
                return AbsentLiveData.create()
            }

            override suspend fun updateLocalDb(cacheObject: StoryPost?) {
                cacheObject?.let {
                    storyPostDao.insert(it)
                }
            }

            override fun setJob(job: Job) {
                addJob("createNewStoryPost", job)
            }

        }.asLiveData()
    }

}