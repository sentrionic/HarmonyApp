package xyz.sentrionic.harmony.repository.main

import androidx.lifecycle.LiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.withContext
import xyz.sentrionic.harmony.api.main.HarmonyMainService
import xyz.sentrionic.harmony.api.main.responses.CommentListResponse
import xyz.sentrionic.harmony.api.main.responses.CommentResponse
import xyz.sentrionic.harmony.models.AuthToken
import xyz.sentrionic.harmony.models.Comment
import xyz.sentrionic.harmony.repository.JobManager
import xyz.sentrionic.harmony.repository.NetworkBoundResource
import xyz.sentrionic.harmony.session.SessionManager
import xyz.sentrionic.harmony.ui.DataState
import xyz.sentrionic.harmony.ui.main.story.state.StoryViewState
import xyz.sentrionic.harmony.ui.main.story.state.StoryViewState.ViewCommentsFields
import xyz.sentrionic.harmony.util.AbsentLiveData
import xyz.sentrionic.harmony.util.ApiSuccessResponse
import xyz.sentrionic.harmony.util.Constants.Companion.PAGINATION_PAGE_SIZE
import xyz.sentrionic.harmony.util.DateUtils
import xyz.sentrionic.harmony.util.GenericApiResponse
import javax.inject.Inject

class CommentRepository
@Inject
constructor(
    val harmonyMainService: HarmonyMainService,
    val sessionManager: SessionManager
) : JobManager("CommentRepository") {

    private val TAG: String = "AppDebug"

    fun searchComments(
        authToken: AuthToken,
        query: String,
        page: Int
    ): LiveData<DataState<StoryViewState>> {
        return object :
            NetworkBoundResource<CommentListResponse, List<CommentResponse>, StoryViewState>(
                sessionManager.isConnectedToTheInternet(),
                true,
                true,
                false
            ) {
            // if network is down, view cache only and return
            override suspend fun createCacheRequestAndReturn() {
            }

            override suspend fun handleApiSuccessResponse(
                response: ApiSuccessResponse<CommentListResponse>
            ) {

                val commentList: ArrayList<Comment> = ArrayList()
                for (commentResponse in response.body.results) {
                    commentList.add(
                        Comment(
                            pk = commentResponse.pk,
                            comment = commentResponse.comment,
                            image = commentResponse.image,
                            date_published = DateUtils.convertServerStringDateToLong(commentResponse.date_published),
                            username = commentResponse.username,
                            likes = commentResponse.likes
                        )
                    )
                }

                withContext(Dispatchers.Main) {
                    // finish with success response
                    onCompleteJob(
                        DataState.data(
                            StoryViewState(
                                viewCommentsFields = ViewCommentsFields(
                                    commentList = commentList,
                                    isQueryInProgress = false,
                                    isQueryExhausted = page * PAGINATION_PAGE_SIZE > commentList.size
                                )
                            )
                        ))
                }
            }

            override fun createCall(): LiveData<GenericApiResponse<CommentListResponse>> {
                return harmonyMainService.getStoryPostComments(
                    "Token ${authToken.token!!}",
                    query = query,
                    page = page
                )
            }

            override fun loadFromCache(): LiveData<StoryViewState> {
                return AbsentLiveData.create()
            }

            override suspend fun updateLocalDb(cacheObject: List<CommentResponse>?) {}

            override fun setJob(job: Job) {
                addJob("searchComments", job)
            }

        }.asLiveData()
    }
}