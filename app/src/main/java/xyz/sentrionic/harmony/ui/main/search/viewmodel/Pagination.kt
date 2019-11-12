package xyz.sentrionic.harmony.ui.main.search.viewmodel

import android.util.Log
import xyz.sentrionic.harmony.ui.main.story.StoryViewModel
import xyz.sentrionic.harmony.ui.main.story.state.StoryStateEvent.ProfileSearchEvent
import xyz.sentrionic.harmony.ui.main.story.state.StoryViewState

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
