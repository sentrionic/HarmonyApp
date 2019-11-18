package xyz.sentrionic.harmony.ui.main.account.viewmodel

import xyz.sentrionic.harmony.ui.main.account.AccountViewModel

fun AccountViewModel.getPage(): Int{
    getCurrentViewStateOrNew().let {
        return it.accountStories?.page?.let { page ->
            return page
        }?: return 1
    }
}

fun AccountViewModel.getIsQueryExhausted(): Boolean {
    getCurrentViewStateOrNew().let {
        return it.accountStories!!.isQueryExhausted
    }
}

fun AccountViewModel.getIsQueryInProgress(): Boolean {
    getCurrentViewStateOrNew().let {
        return it.accountStories!!.isQueryInProgress
    }
}