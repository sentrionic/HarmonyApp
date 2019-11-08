package xyz.sentrionic.harmony.ui.main.create_story

import android.net.Uri
import androidx.lifecycle.LiveData
import okhttp3.MediaType
import okhttp3.RequestBody
import xyz.sentrionic.harmony.repository.main.CreateStoryRepository
import xyz.sentrionic.harmony.session.SessionManager
import xyz.sentrionic.harmony.ui.BaseViewModel
import xyz.sentrionic.harmony.ui.DataState
import xyz.sentrionic.harmony.ui.Loading
import xyz.sentrionic.harmony.ui.main.create_story.state.CreateStoryStateEvent
import xyz.sentrionic.harmony.ui.main.create_story.state.CreateStoryStateEvent.*
import xyz.sentrionic.harmony.ui.main.create_story.state.CreateStoryViewState
import xyz.sentrionic.harmony.ui.main.create_story.state.CreateStoryViewState.*
import xyz.sentrionic.harmony.util.AbsentLiveData
import javax.inject.Inject

class CreateStoryViewModel
@Inject
constructor(
    val createStoryRepository: CreateStoryRepository,
    val sessionManager: SessionManager
): BaseViewModel<CreateStoryStateEvent, CreateStoryViewState>() {

    override fun handleStateEvent(
        stateEvent: CreateStoryStateEvent
    ): LiveData<DataState<CreateStoryViewState>> {

        when (stateEvent) {

            is CreateNewStoryEvent -> {
                return sessionManager.cachedToken.value?.let { authToken ->

                    val caption = RequestBody.create(MediaType.parse("text/plain"), stateEvent.caption)

                    createStoryRepository.createNewStoryPost(
                        authToken,
                        caption,
                        stateEvent.image
                    )
                }?: AbsentLiveData.create()
            }

            is None -> {
                return object: LiveData<DataState<CreateStoryViewState>>() {
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

    override fun initNewViewState(): CreateStoryViewState {
        return CreateStoryViewState()
    }

    fun clearNewStoryFields() {
        val update = getCurrentViewStateOrNew()
        update.storyFields = NewStoryFields()
        setViewState(update)
    }

    fun cancelActiveJobs() {
        createStoryRepository.cancelActiveJobs()
        handlePendingData()
    }

    private fun handlePendingData() {
        setStateEvent(None())
    }

    override fun onCleared() {
        super.onCleared()
        cancelActiveJobs()
    }

    fun setNewStoryFields(caption: String?, uri: Uri?) {
        val update = getCurrentViewStateOrNew()
        val newStoryFields = update.storyFields
        caption?.let { newStoryFields.newStoryCaption = it }
        uri?.let{ newStoryFields.newImageUri = it }
        update.storyFields = newStoryFields
        _viewState.value = update
    }

}