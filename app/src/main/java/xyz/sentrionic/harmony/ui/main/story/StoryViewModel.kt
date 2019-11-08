package xyz.sentrionic.harmony.ui.main.story

import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import xyz.sentrionic.harmony.persistence.StoryQueryUtils
import xyz.sentrionic.harmony.repository.main.StoryRepository
import xyz.sentrionic.harmony.session.SessionManager
import xyz.sentrionic.harmony.ui.BaseViewModel
import xyz.sentrionic.harmony.ui.DataState
import xyz.sentrionic.harmony.ui.Loading
import xyz.sentrionic.harmony.ui.main.story.state.StoryStateEvent
import xyz.sentrionic.harmony.ui.main.story.state.StoryStateEvent.*
import xyz.sentrionic.harmony.ui.main.story.state.StoryViewState
import xyz.sentrionic.harmony.ui.main.story.viewmodel.*
import xyz.sentrionic.harmony.util.AbsentLiveData
import xyz.sentrionic.harmony.util.PreferenceKeys.Companion.STORY_FILTER
import xyz.sentrionic.harmony.util.PreferenceKeys.Companion.STORY_ORDER
import javax.inject.Inject

class StoryViewModel
@Inject
constructor(
    private val sessionManager: SessionManager,
    private val storyRepository: StoryRepository,
    private val sharedPreferences: SharedPreferences,
    private val editor: SharedPreferences.Editor
): BaseViewModel<StoryStateEvent, StoryViewState>() {

    init {
        setStoryFilter(
            sharedPreferences.getString(
                STORY_FILTER,
                StoryQueryUtils.STORY_FILTER_DATE_PUBLISHED
            )
        )
        sharedPreferences.getString(
            STORY_ORDER,
            StoryQueryUtils.STORY_ORDER_DESC
        )?.let {
            setStoryOrder(
                it
            )
        }
    }

    override fun handleStateEvent(stateEvent: StoryStateEvent): LiveData<DataState<StoryViewState>> {

        when (stateEvent) {

            is StorySearchEvent -> {
                return sessionManager.cachedToken.value?.let { authToken ->
                    storyRepository.searchStoryPosts(
                        authToken = authToken,
                        query = getSearchQuery(),
                        filterAndOrder = getOrder() + getFilter(),
                        page = getPage()
                    )
                }?: AbsentLiveData.create()
            }

            is CheckAuthorOfStoryPost -> {
                return sessionManager.cachedToken.value?.let { authToken ->
                    storyRepository.isAuthorOfStoryPost(
                        authToken = authToken,
                        slug = getSlug()
                    )
                }?: AbsentLiveData.create()
            }

            is DeleteStoryPostEvent -> {
                return sessionManager.cachedToken.value?.let { authToken ->
                    storyRepository.deleteStoryPost(
                        authToken = authToken,
                        storyPost = getStoryPost()
                    )
                }?: AbsentLiveData.create()
            }

            is None -> {
                return object : LiveData<DataState<StoryViewState>>() {
                    override fun onActive() {
                        super.onActive()
                        value = DataState(
                            null,
                            Loading(false),
                                    null
                        )
                    }
                }
            }
        }
    }

    fun saveFilterOptions(filter: String, order: String) {
        editor.putString(STORY_FILTER, filter)
        editor.apply()

        editor.putString(STORY_ORDER, order)
        editor.apply()
    }

    override fun initNewViewState(): StoryViewState {
        return StoryViewState()
    }

    fun cancelActiveJobs() {
        storyRepository.cancelActiveJobs() // cancel active jobs
        handlePendingData() // hide progress bar
    }

    private fun handlePendingData() {
        setStateEvent(None())
    }

    override fun onCleared() {
        super.onCleared()
        cancelActiveJobs()
    }

}