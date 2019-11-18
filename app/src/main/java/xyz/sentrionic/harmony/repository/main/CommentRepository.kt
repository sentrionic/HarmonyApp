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
        page: Int,
        oldCommentList: List<Comment>
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

                val list = oldCommentList + commentList

                withContext(Dispatchers.Main) {
                    // finish with success response
                    onCompleteJob(
                        DataState.data(
                            StoryViewState(
                                viewCommentsFields = ViewCommentsFields(
                                    commentList = list,
                                    isQueryInProgress = false,
                                    isQueryExhausted = page * PAGINATION_PAGE_SIZE > list.size
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

    fun postComment(
        authToken: AuthToken,
        slug: String,
        comment: String,
        commentList: List<Comment>) : LiveData<DataState<StoryViewState>> {

        return object : NetworkBoundResource<CommentResponse, Comment, StoryViewState>(
            sessionManager.isConnectedToTheInternet(),
            true,
            true,
            false
        ) {
            override suspend fun createCacheRequestAndReturn() {}

            override suspend fun handleApiSuccessResponse(response: ApiSuccessResponse<CommentResponse>) {

                val newComment = Comment(
                    pk = response.body.pk,
                    comment = response.body.comment,
                    image = response.body.image,
                    date_published = DateUtils.convertServerStringDateToLong(response.body.date_published),
                    username = response.body.username,
                    likes = response.body.likes
                )

                val newCommentList = commentList + newComment

                withContext(Dispatchers.Main) {
                    // finish with success response
                    onCompleteJob(
                        DataState.data(
                            StoryViewState(
                                viewCommentsFields = ViewCommentsFields(
                                    commentList = newCommentList
                                )
                            )
                        )
                    )
                }
            }

            override fun createCall(): LiveData<GenericApiResponse<CommentResponse>> {
                return harmonyMainService.postComment(
                    authorization = "Token ${authToken.token!!}",
                    slug = slug,
                    comment = comment
                )
            }

            override fun loadFromCache(): LiveData<StoryViewState> {
                return AbsentLiveData.create()
            }

            override suspend fun updateLocalDb(cacheObject: Comment?) {}

            override fun setJob(job: Job) {
                addJob("postComment", job)
            }

        }.asLiveData()
    }
}