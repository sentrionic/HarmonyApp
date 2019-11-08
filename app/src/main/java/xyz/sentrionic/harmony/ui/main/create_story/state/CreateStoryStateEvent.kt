package xyz.sentrionic.harmony.ui.main.create_story.state

import okhttp3.MultipartBody


sealed class CreateStoryStateEvent {

    data class CreateNewStoryEvent(
        val caption: String,
        val image: MultipartBody.Part
    ): CreateStoryStateEvent()

    class None: CreateStoryStateEvent()
}