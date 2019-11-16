package xyz.sentrionic.harmony.ui.main.story.viewmodel

import android.util.Log
import xyz.sentrionic.harmony.ui.main.story.StoryViewModel
import xyz.sentrionic.harmony.ui.main.story.state.StoryStateEvent.*
import xyz.sentrionic.harmony.ui.main.story.state.StoryViewState


/**
 * Story Pagination
 */
fun StoryViewModel.resetPage(){
    val update = getCurrentViewStateOrNew()
    update.storyFields.page = 1
    setViewState(update)
}

fun StoryViewModel.loadFirstPage() {
    setQueryInProgress(true)
    setQueryExhausted(false)
    resetPage()
    setStateEvent(StorySearchEvent())
    Log.e(TAG, "StoryViewModel: loadFirstPage: ${getSearchQuery()}")
}

private fun StoryViewModel.incrementPageNumber() {
    val update = getCurrentViewStateOrNew()
    val page = update.copy().storyFields.page // get current page
    update.storyFields.page = page + 1
    setViewState(update)
}

fun StoryViewModel.nextPage() {
    if (!getIsQueryInProgress() && !getIsQueryExhausted()) {
        Log.d(TAG, "StoryViewModel: Attempting to load next page...")
        incrementPageNumber()
        setQueryInProgress(true)
        setStateEvent(StorySearchEvent())
    }
}

fun StoryViewModel.handleIncomingStoryListData(viewState: StoryViewState) {
    Log.d(TAG, "StoryViewState, DataState: ${viewState}")
    Log.d(TAG, "StoryViewState, DataState: isQueryInProgress?: " +
            "${viewState.storyFields.isQueryInProgress}")
    Log.d(TAG, "BlogViewModel, DataState: isQueryExhausted?: " +
            "${viewState.storyFields.isQueryExhausted}")
    setQueryInProgress(viewState.storyFields.isQueryInProgress)
    setQueryExhausted(viewState.storyFields.isQueryExhausted)
    setStoryListData(viewState.storyFields.storyList)
}

/**
 * Profile Pagination
 */
fun StoryViewModel.resetUserPage() {
    val update = getCurrentViewStateOrNew()
    update.userFields.page = 1
    setViewState(update)
}

fun StoryViewModel.loadFirstUserPage() {
    setUserQueryInProgress(true)
    setUserQueryExhausted(false)
    resetUserPage()
    setStateEvent(ProfileSearchEvent())
    Log.e(TAG, "StoryViewModel: loadFirstUserPage: ${getUserSearchQuery()}")
}

private fun StoryViewModel.incrementUserPageNumber() {
    val update = getCurrentViewStateOrNew()
    val page = update.copy().userFields.page // get current page
    update.userFields.page = page + 1
    setViewState(update)
}

fun StoryViewModel.nextUserPage() {
    if (!getIsUserQueryInProgress() && !getIsUserQueryExhausted()) {
        Log.d(TAG, "StoryViewModel: Attempting to load next page...")
        incrementUserPageNumber()
        setUserQueryInProgress(true)
        setStateEvent(ProfileSearchEvent())
    }
}

fun StoryViewModel.handleIncomingProfileListData(viewState: StoryViewState) {
    Log.d(TAG, "StoryViewState, UserQuery, DataState: ${viewState}")
    Log.d(TAG, "StoryViewState, UserQuery, DataState: isQueryInProgress?: " +
            "${viewState.userFields.isQueryInProgress}")
    Log.d(TAG, "StoryViewModel, DataState: isQueryExhausted?: " +
            "${viewState.userFields.isQueryExhausted}")
    setUserQueryInProgress(viewState.userFields.isQueryInProgress)
    setUserQueryExhausted(viewState.userFields.isQueryExhausted)
    setProfileListData(viewState.userFields.userList)
}

/**
 * Comment Pagination
 */
fun StoryViewModel.resetCommentPage(){
    val update = getCurrentViewStateOrNew()
    update.viewCommentsFields.page = 1
    setViewState(update)
}

fun StoryViewModel.resetCommentList(){
    val update = getCurrentViewStateOrNew()
    update.viewCommentsFields.commentList = emptyList()
    setViewState(update)
}

fun StoryViewModel.loadFirstCommentPage() {
    setCommentQueryInProgress(true)
    setCommentQueryExhausted(false)
    resetCommentPage()
    resetCommentList()
    setStateEvent(GetStoryPostComments())
}

private fun StoryViewModel.incrementCommentPageNumber() {
    val update = getCurrentViewStateOrNew()
    val page = update.copy().viewCommentsFields.page // get current page
    update.viewCommentsFields.page = page + 1
    setViewState(update)
}

fun StoryViewModel.nextCommentPage() {
    if (!getIsCommentQueryInProgress() && !getIsCommentQueryExhausted()) {
        Log.d(TAG, "StoryViewModel: Attempting to load next page...")
        incrementCommentPageNumber()
        setCommentQueryInProgress(true)
        setStateEvent(GetStoryPostComments())
    }
}

fun StoryViewModel.handleIncomingCommentListData(viewState: StoryViewState) {
    Log.d(TAG, "StoryViewState, DataState: ${viewState}")
    Log.d(TAG, "StoryViewState, DataState: isQueryInProgress?: " +
            "${viewState.viewCommentsFields.isQueryInProgress}")
    Log.d(TAG, "BlogViewModel, DataState: isQueryExhausted?: " +
            "${viewState.viewCommentsFields.isQueryExhausted}")
    setCommentQueryInProgress(viewState.viewCommentsFields.isQueryInProgress)
    setCommentQueryExhausted(viewState.viewCommentsFields.isQueryExhausted)
    setCommentListData(viewState.viewCommentsFields.commentList)
}

