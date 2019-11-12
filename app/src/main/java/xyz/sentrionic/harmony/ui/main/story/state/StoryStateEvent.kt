package xyz.sentrionic.harmony.ui.main.story.state

sealed class StoryStateEvent {

    class StorySearchEvent : StoryStateEvent()

    class ProfileSearchEvent : StoryStateEvent()

    class CheckAuthorOfStoryPost : StoryStateEvent()

    class DeleteStoryPostEvent : StoryStateEvent()

    class LikeStoryPostEvent : StoryStateEvent()

    class None : StoryStateEvent()

}