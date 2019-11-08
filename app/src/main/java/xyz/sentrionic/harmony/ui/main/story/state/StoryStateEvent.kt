package xyz.sentrionic.harmony.ui.main.story.state

sealed class StoryStateEvent {

    class StorySearchEvent : StoryStateEvent()

    class CheckAuthorOfStoryPost : StoryStateEvent()

    class DeleteStoryPostEvent : StoryStateEvent()

    class None : StoryStateEvent()

}