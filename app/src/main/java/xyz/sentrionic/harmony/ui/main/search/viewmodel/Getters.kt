package xyz.sentrionic.harmony.ui.main.search.viewmodel

import xyz.sentrionic.harmony.models.StoryPost
import xyz.sentrionic.harmony.ui.main.search.SearchViewModel

fun SearchViewModel.getSearchQuery(): String {
    getCurrentViewStateOrNew().let{
        return it.storyFields.searchQuery
    }
}

fun SearchViewModel.getPage(): Int{
    getCurrentViewStateOrNew().let{
        return it.storyFields.page
    }
}

fun SearchViewModel.getIsQueryExhausted(): Boolean {
    getCurrentViewStateOrNew().let {
        return it.storyFields.isQueryExhausted
    }
}

fun SearchViewModel.getIsQueryInProgress(): Boolean {
    getCurrentViewStateOrNew().let {
        return it.storyFields.isQueryInProgress
    }
}

fun SearchViewModel.getFilter(): String {
    getCurrentViewStateOrNew().let {
        return it.storyFields.filter
    }
}

fun SearchViewModel.getOrder(): String {
    getCurrentViewStateOrNew().let {
        return it.storyFields.order
    }
}

fun SearchViewModel.getSlug(): String {
    getCurrentViewStateOrNew().let {
        it.viewStoryFields.storyPost?.let {
            return it.slug
        }
    }
    return ""
}

fun SearchViewModel.getStoryPost(): StoryPost {
    getCurrentViewStateOrNew().let {
        return it.viewStoryFields.storyPost?.let {
            return it
        }?: getDummyStoryPost()
    }
}

fun SearchViewModel.getDummyStoryPost(): StoryPost {
    return StoryPost(-1, "" , "", "", 1, "", "", 0, false, "")
}