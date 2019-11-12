package xyz.sentrionic.harmony.ui.main.story.state

import xyz.sentrionic.harmony.models.Profile
import xyz.sentrionic.harmony.models.StoryPost
import xyz.sentrionic.harmony.persistence.StoryQueryUtils.Companion.ORDER_BY_ASC_DATE_UPDATED
import xyz.sentrionic.harmony.persistence.StoryQueryUtils.Companion.STORY_ORDER_ASC

data class StoryViewState (

    // StoryFragment vars
    var storyFields: StoryFields = StoryFields(),

    // ViewStoryFragment vars
    var viewStoryFields: ViewStoryFields = ViewStoryFields(),

    var userFields: UserFields = UserFields(),

    var viewProfileFields: ViewProfileField = ViewProfileField()
)
{
    data class StoryFields(
        var storyList: List<StoryPost> = ArrayList(),
        var searchQuery: String = "",
        var page: Int = 1,
        var isQueryInProgress: Boolean = false,
        var isQueryExhausted: Boolean = false,
        var filter: String = ORDER_BY_ASC_DATE_UPDATED,
        var order: String = STORY_ORDER_ASC
    )

    data class ViewStoryFields(
        var storyPost: StoryPost? = null,
        var isAuthorOfStoryPost: Boolean = false
    )

    data class UserFields(
        var userList: List<Profile> = ArrayList(),
        var searchQuery: String = "",
        var page: Int = 1,
        var isQueryInProgress: Boolean = false,
        var isQueryExhausted: Boolean = false
    )

    data class ViewProfileField(
        var profile: Profile? = null,
        var isFollowing: Boolean = false
    )

}