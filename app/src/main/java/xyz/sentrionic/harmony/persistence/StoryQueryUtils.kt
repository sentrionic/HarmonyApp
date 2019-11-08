package xyz.sentrionic.harmony.persistence

import androidx.lifecycle.LiveData
import xyz.sentrionic.harmony.models.StoryPost
import xyz.sentrionic.harmony.persistence.StoryQueryUtils.Companion.ORDER_BY_ASC_DATE_UPDATED
import xyz.sentrionic.harmony.persistence.StoryQueryUtils.Companion.ORDER_BY_ASC_USERNAME
import xyz.sentrionic.harmony.persistence.StoryQueryUtils.Companion.ORDER_BY_DESC_DATE_UPDATED
import xyz.sentrionic.harmony.persistence.StoryQueryUtils.Companion.ORDER_BY_DESC_USERNAME

class StoryQueryUtils {


    companion object{
        private val TAG: String = "AppDebug"

        // values
        const val STORY_ORDER_ASC: String = ""
        const val STORY_ORDER_DESC: String = "-"
        const val STORY_FILTER_USERNAME = "username"
        const val STORY_FILTER_DATE_PUBLISHED = "date_published"

        val ORDER_BY_ASC_DATE_UPDATED = STORY_ORDER_ASC + STORY_FILTER_DATE_PUBLISHED
        val ORDER_BY_DESC_DATE_UPDATED = STORY_ORDER_DESC + STORY_FILTER_DATE_PUBLISHED
        val ORDER_BY_ASC_USERNAME = STORY_ORDER_ASC + STORY_FILTER_USERNAME
        val ORDER_BY_DESC_USERNAME = STORY_ORDER_DESC + STORY_FILTER_USERNAME
    }
}


fun StoryPostDao.returnOrderedStoryQuery(
    query: String,
    filterAndOrder: String,
    page: Int
): LiveData<List<StoryPost>> {

    when{

        filterAndOrder.contains(ORDER_BY_DESC_DATE_UPDATED) ->{
            return searchStoryPostsOrderByDateDESC(
                query = query,
                page = page)
        }

        filterAndOrder.contains(ORDER_BY_ASC_DATE_UPDATED) ->{
            return searchStoryPostsOrderByDateASC(
                query = query,
                page = page)
        }

        filterAndOrder.contains(ORDER_BY_DESC_USERNAME) ->{
            return searchStoryPostsOrderByAuthorDESC(
                query = query,
                page = page)
        }

        filterAndOrder.contains(ORDER_BY_ASC_USERNAME) ->{
            return searchStoryPostsOrderByAuthorASC(
                query = query,
                page = page)
        }
        else ->
            return searchStoryPostsOrderByDateASC(
                query = query,
                page = page
            )
    }
}