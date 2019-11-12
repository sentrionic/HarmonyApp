package xyz.sentrionic.harmony.ui.main.story.viewmodel

import xyz.sentrionic.harmony.models.Profile
import xyz.sentrionic.harmony.models.StoryPost
import xyz.sentrionic.harmony.ui.main.story.StoryViewModel

fun StoryViewModel.getSearchQuery(): String {
    getCurrentViewStateOrNew().let{
        return it.storyFields.searchQuery
    }
}

fun StoryViewModel.getPage(): Int{
    getCurrentViewStateOrNew().let{
        return it.storyFields.page
    }
}

fun StoryViewModel.getIsQueryExhausted(): Boolean {
    getCurrentViewStateOrNew().let {
        return it.storyFields.isQueryExhausted
    }
}

fun StoryViewModel.getIsQueryInProgress(): Boolean {
    getCurrentViewStateOrNew().let {
        return it.storyFields.isQueryInProgress
    }
}

fun StoryViewModel.getFilter(): String {
    getCurrentViewStateOrNew().let {
        return it.storyFields.filter
    }
}

fun StoryViewModel.getOrder(): String {
    getCurrentViewStateOrNew().let {
        return it.storyFields.order
    }
}

fun StoryViewModel.getSlug(): String {
    getCurrentViewStateOrNew().let {
        it.viewStoryFields.storyPost?.let {
            return it.slug
        }
    }
    return ""
}

fun StoryViewModel.isAuthorOfStoryPost(): Boolean {
    getCurrentViewStateOrNew().let {
        return it.viewStoryFields.isAuthorOfStoryPost
    }
}

fun StoryViewModel.getStoryPost(): StoryPost {
    getCurrentViewStateOrNew().let {
        return it.viewStoryFields.storyPost?.let {
            return it
        }?: getDummyStoryPost()
    }
}

fun StoryViewModel.getProfile(): Profile {
    getCurrentViewStateOrNew().let {
        return it.viewProfileFields.profile?.let {
            return it
        }?: getDummyProfile()
    }
}

fun StoryViewModel.getUsername() : String {
    getCurrentViewStateOrNew().let {
        return it.viewProfileFields.username?.let {
            return it
        }?: ""
    }
}

fun StoryViewModel.getDummyStoryPost(): StoryPost {
    return StoryPost(-1, "" , "", "", 1, "", "", 0, false, "")
}

fun StoryViewModel.getDummyProfile() : Profile {
    return Profile(-1, "", "", "", "", "", 0, 0, 0, false)
}

