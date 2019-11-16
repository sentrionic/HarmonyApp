package xyz.sentrionic.harmony.ui.main.story.viewmodel

import xyz.sentrionic.harmony.models.Comment
import xyz.sentrionic.harmony.models.Profile
import xyz.sentrionic.harmony.models.StoryPost
import xyz.sentrionic.harmony.ui.main.story.StoryViewModel

/**
 * Story Setters
 */
fun StoryViewModel.setQuery(query: String ) {
    val update = getCurrentViewStateOrNew()
    update.storyFields.searchQuery = query
    setViewState(update)
}

fun StoryViewModel.setStoryListData(storyList: List<StoryPost>) {
    val update = getCurrentViewStateOrNew()
    update.storyFields.storyList = storyList
    setViewState(update)
}

fun StoryViewModel.setStoryPost(storyPost: StoryPost) {
    val update = getCurrentViewStateOrNew()
    update.viewStoryFields.storyPost = storyPost
    setViewState(update)
}

fun StoryViewModel.setQueryExhausted(isExhausted: Boolean) {
    val update = getCurrentViewStateOrNew()
    update.storyFields.isQueryExhausted = isExhausted
    setViewState(update)
}

fun StoryViewModel.setQueryInProgress(isInProgress: Boolean) {
    val update = getCurrentViewStateOrNew()
    update.storyFields.isQueryInProgress = isInProgress
    setViewState(update)
}

fun StoryViewModel.setIsAuthorOfStoryPost(isAuthorOfStoryPost: Boolean) {
    val update = getCurrentViewStateOrNew()
    update.viewStoryFields.isAuthorOfStoryPost = isAuthorOfStoryPost
    setViewState(update)
}

// Filter can be "date_updated" or "username"
fun StoryViewModel.setStoryFilter(filter: String?) {
    filter?.let {
        val update = getCurrentViewStateOrNew()
        update.storyFields.filter = filter
        setViewState(update)
    }
}

// Order can be "-" or ""
// Note: "-" = DESC, "" = ASC
fun StoryViewModel.setStoryOrder(order: String) {
    val update = getCurrentViewStateOrNew()
    update.storyFields.order = order
    setViewState(update)
}

fun StoryViewModel.removeDeletedStoryPost() {
    val update = getCurrentViewStateOrNew()
    val list = update.storyFields.storyList.toMutableList()
    for (i in 0 until list.size) {
        if (list[i] == getStoryPost()) {
            list.remove(getStoryPost())
            break
        }
    }
    setStoryListData(list)
}

/**
 * Profile Setters
 */
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

fun StoryViewModel.setUsername(username: String) {
    val update = getCurrentViewStateOrNew()
    update.viewProfileFields.username = username
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

/**
 * Comment Setters
 */

fun StoryViewModel.setCommentListData(comments: List<Comment>) {
    val update = getCurrentViewStateOrNew()
    update.viewCommentsFields.commentList = comments
    setViewState(update)
}

fun StoryViewModel.setCommentQueryExhausted(isExhausted: Boolean) {
    val update = getCurrentViewStateOrNew()
    update.viewCommentsFields.isQueryExhausted = isExhausted
    setViewState(update)
}

fun StoryViewModel.setCommentQueryInProgress(isInProgress: Boolean) {
    val update = getCurrentViewStateOrNew()
    update.viewCommentsFields.isQueryInProgress = isInProgress
    setViewState(update)
}