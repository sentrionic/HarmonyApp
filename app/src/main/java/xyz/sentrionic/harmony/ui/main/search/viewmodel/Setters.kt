package xyz.sentrionic.harmony.ui.main.search.viewmodel

import xyz.sentrionic.harmony.models.Profile
import xyz.sentrionic.harmony.ui.main.search.SearchViewModel
import xyz.sentrionic.harmony.ui.main.story.StoryViewModel

fun StoryViewModel.setUserQuery(query: String ) {
    val update = getCurrentViewStateOrNew()
    update.userFields.searchQuery = query
    setViewState(update)
}

fun StoryViewModel.setProfileListData(profiles: List<Profile>) {
    val update = getCurrentViewStateOrNew()
    update.userFields.userList = profiles
    setViewState(update)
}

fun StoryViewModel.setProfile(profile: Profile) {
    val update = getCurrentViewStateOrNew()
    update.viewProfileFields.profile = profile
    setViewState(update)
}

fun StoryViewModel.setUserQueryExhausted(isExhausted: Boolean) {
    val update = getCurrentViewStateOrNew()
    update.userFields.isQueryExhausted = isExhausted
    setViewState(update)
}

fun StoryViewModel.setUserQueryInProgress(isInProgress: Boolean) {
    val update = getCurrentViewStateOrNew()
    update.userFields.isQueryInProgress = isInProgress
    setViewState(update)
}


// Filter can be "date_updated" or "username"
fun SearchViewModel.setStoryFilter(filter: String?) {
    filter?.let {
        val update = getCurrentViewStateOrNew()
        update.storyFields.filter = filter
        setViewState(update)
    }
}

// Order can be "-" or ""
// Note: "-" = DESC, "" = ASC
fun SearchViewModel.setStoryOrder(order: String) {
    val update = getCurrentViewStateOrNew()
    update.storyFields.order = order
    setViewState(update)
}