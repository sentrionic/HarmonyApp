package xyz.sentrionic.harmony.ui.main.search.state

sealed class SearchStoryStateEvent {

    class StorySearchEvent : SearchStoryStateEvent()

    class CheckAuthorOfStoryPost : SearchStoryStateEvent()

    class None : SearchStoryStateEvent()

}