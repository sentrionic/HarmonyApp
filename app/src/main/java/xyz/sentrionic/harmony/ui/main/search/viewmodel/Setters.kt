package xyz.sentrionic.harmony.ui.main.search.viewmodel

import xyz.sentrionic.harmony.models.StoryPost
import xyz.sentrionic.harmony.ui.main.search.SearchViewModel

fun SearchViewModel.setQuery(query: String ) {
    val update = getCurrentViewStateOrNew()
    update.storyFields.searchQuery = query
    setViewState(update)
}

fun SearchViewModel.setStoryListData(storyList: List<StoryPost>) {
    val update = getCurrentViewStateOrNew()
    update.storyFields.storyList = storyList
    setViewState(update)
}

fun SearchViewModel.setStoryPost(storyPost: StoryPost) {
    val update = getCurrentViewStateOrNew()
    update.viewStoryFields.storyPost = storyPost
    setViewState(update)
}

fun SearchViewModel.setQueryExhausted(isExhausted: Boolean) {
    val update = getCurrentViewStateOrNew()
    update.storyFields.isQueryExhausted = isExhausted
    setViewState(update)
}

fun SearchViewModel.setQueryInProgress(isInProgress: Boolean) {
    val update = getCurrentViewStateOrNew()
    update.storyFields.isQueryInProgress = isInProgress
    setViewState(update)
}

fun SearchViewModel.setIsAuthorOfStoryPost(isAuthorOfStoryPost: Boolean) {
    val update = getCurrentViewStateOrNew()
    update.viewStoryFields.isAuthorOfStoryPost = isAuthorOfStoryPost
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