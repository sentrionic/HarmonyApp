package xyz.sentrionic.harmony.ui.main.account.viewmodel

import android.util.Log
import xyz.sentrionic.harmony.ui.main.account.AccountViewModel
import xyz.sentrionic.harmony.ui.main.account.state.AccountStateEvent
import xyz.sentrionic.harmony.ui.main.account.state.AccountViewState

fun AccountViewModel.resetPage() {
    val update = getCurrentViewStateOrNew()
    update.accountStories?.page = 1
    setViewState(update)
}

private fun AccountViewModel.incrementPageNumber() {
    val update = getCurrentViewStateOrNew()
    val page = update.accountStories?.page // get current page
    update.accountStories!!.page = page!! + 1
    setViewState(update)
}

fun AccountViewModel.nextPage() {
    if (!getIsQueryInProgress() && !getIsQueryExhausted()) {
        Log.d(TAG, "AccountViewState: Attempting to load next page...")
        incrementPageNumber()
        setQueryInProgress(true)
        setStateEvent(AccountStateEvent.GetAccountStoriesEvent("sentrionic"))
    }
}

fun AccountViewModel.handleIncomingStoryListData(accountStories: AccountViewState.AccountStories) {
    Log.d(TAG, "AccountViewState, DataState: ${viewState}")
    Log.d(TAG, "AccountViewState, DataState: isQueryInProgress?: " +
            "${accountStories.isQueryInProgress}")
    Log.d(TAG, "AccountViewState, DataState: isQueryExhausted?: " +
            "${accountStories.isQueryExhausted}")
    setQueryInProgress(accountStories.isQueryInProgress)
    setQueryExhausted(accountStories.isQueryExhausted)
    setAccountStoryListData(accountStories)
}

fun AccountViewModel.setQueryExhausted(isExhausted: Boolean) {
    val update = getCurrentViewStateOrNew()
    update.accountStories?.isQueryExhausted = isExhausted
    setViewState(update)
}

fun AccountViewModel.setQueryInProgress(isInProgress: Boolean) {
    val update = getCurrentViewStateOrNew()
    update.accountStories?.isQueryInProgress = isInProgress
    setViewState(update)
}