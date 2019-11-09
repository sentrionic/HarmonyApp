package xyz.sentrionic.harmony.ui.main.story

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.*
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade
import kotlinx.android.synthetic.main.layout_story_list_item.view.image_time_posted
import kotlinx.android.synthetic.main.layout_story_list_item.view.story_caption_username
import kotlinx.android.synthetic.main.layout_story_list_item.view.story_image
import kotlinx.android.synthetic.main.layout_story_list_item.view.story_image_caption
import kotlinx.android.synthetic.main.layout_story_list_item.view.story_image_heart
import kotlinx.android.synthetic.main.layout_story_list_item.view.story_image_heart_red
import kotlinx.android.synthetic.main.layout_story_list_item.view.story_image_likes
import kotlinx.android.synthetic.main.layout_story_list_item.view.story_profile_photo
import kotlinx.android.synthetic.main.layout_story_list_item.view.story_username
import xyz.sentrionic.harmony.R
import xyz.sentrionic.harmony.models.StoryPost
import xyz.sentrionic.harmony.util.DateUtils
import xyz.sentrionic.harmony.util.GenericViewHolder
import xyz.sentrionic.harmony.util.Heart

class StoryListAdapter(private val requestManager: RequestManager,
                       private val interaction: Interaction? = null) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val TAG: String = "AppDebug"
    private val NO_MORE_RESULTS = -1
    private val STORY_ITEM = 0
    private val NO_MORE_RESULTS_BLOG_MARKER = StoryPost(
        NO_MORE_RESULTS,
        "" ,
        "",
        "",
        0,
        "",
        "",
        0,
        false,
        ""
    )

    val DIFF_CALLBACK = object : DiffUtil.ItemCallback<StoryPost>() {

        override fun areItemsTheSame(oldItem: StoryPost, newItem: StoryPost): Boolean {
            return oldItem.pk == newItem.pk
        }

        override fun areContentsTheSame(oldItem: StoryPost, newItem: StoryPost): Boolean {
            return oldItem == newItem
        }

    }

    private val differ =
        AsyncListDiffer(
            StoryRecyclerChangeCallback(this),
            AsyncDifferConfig.Builder(DIFF_CALLBACK).build()
        )


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        when (viewType) {

            NO_MORE_RESULTS ->{
                Log.e(TAG, "onCreateViewHolder: No more results...")
                return GenericViewHolder(
                    LayoutInflater.from(parent.context).inflate(
                        R.layout.layout_no_more_results,
                        parent,
                        false
                    )
                )
            }

            STORY_ITEM ->{
                return StoryViewHolder(
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.layout_story_list_item, parent, false),
                    interaction = interaction,
                    requestManager = requestManager
                )
            }
            else -> {
                return StoryViewHolder(
                    LayoutInflater.from(parent.context).inflate(R.layout.layout_story_list_item, parent, false),
                    interaction = interaction,
                    requestManager = requestManager
                )
            }
        }
    }

    internal inner class StoryRecyclerChangeCallback(
        private val adapter: StoryListAdapter
    ) : ListUpdateCallback {

        override fun onChanged(position: Int, count: Int, payload: Any?) {
            adapter.notifyItemRangeChanged(position, count, payload)
        }

        override fun onInserted(position: Int, count: Int) {
            adapter.notifyItemRangeChanged(position, count)
        }

        override fun onMoved(fromPosition: Int, toPosition: Int) {
            adapter.notifyDataSetChanged()
        }

        override fun onRemoved(position: Int, count: Int) {
            adapter.notifyDataSetChanged()
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is StoryViewHolder -> {
                holder.bind(differ.currentList[position])
            }
        }
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun getItemViewType(position: Int): Int {
        if (differ.currentList[position].pk > -1) {
            return STORY_ITEM
        }
        return differ.currentList[position].pk
    }

    // Prepare the images that will be displayed in the RecyclerView.
    // This also ensures if the network connection is lost, they will be in the cache
    fun preloadGlideImages(
        requestManager: RequestManager,
        list: List<StoryPost>
    ) {
        for (storyPost in list){
            requestManager
                .load(storyPost.image)
                .preload()
        }
    }

    fun submitList(storyList: List<StoryPost>?, isQueryExhausted: Boolean) {
        val newList = storyList?.toMutableList()
        if (isQueryExhausted)
            newList?.add(NO_MORE_RESULTS_BLOG_MARKER)
        differ.submitList(newList)
    }

    class StoryViewHolder
    constructor(
        itemView: View,
        val requestManager: RequestManager,
        private val interaction: Interaction?
    ) : RecyclerView.ViewHolder(itemView) {

        fun bind(item: StoryPost) = with(itemView) {
            itemView.story_image.setOnClickListener {
                interaction?.onItemSelected(adapterPosition, item)
            }

            itemView.story_image_heart.setOnClickListener {
                interaction?.onItemLiked(adapterPosition, item, Heart(heartWhite = story_image_heart, heartRed = story_image_heart_red), itemView)
            }

            itemView.story_image_heart_red.setOnClickListener {
                interaction?.onItemLiked(adapterPosition, item, Heart(heartWhite = story_image_heart, heartRed = story_image_heart_red), itemView)
            }

            requestManager
                .load(item.image)
                .transition(withCrossFade())
                .into(itemView.story_image)

            requestManager
                .load(item.profile_image)
                .into(itemView.story_profile_photo)

            itemView.story_username.text = item.username
            itemView.story_caption_username.text = item.username
            itemView.story_image_caption.text = item.caption
            itemView.story_image_likes.text = resources.getQuantityString(R.plurals.likes, item.likes, item.likes)

            if (item.liked) {
                itemView.story_image_heart_red.visibility = View.VISIBLE
                itemView.story_image_heart.visibility = View.GONE
            }
            else {
                itemView.story_image_heart_red.visibility = View.GONE
                itemView.story_image_heart.visibility = View.VISIBLE
            }

            itemView.image_time_posted.text = DateUtils.convertLongToStringDate(item.date_published)
        }
    }

    interface Interaction {
        fun onItemSelected(position: Int, item: StoryPost)
        fun onItemLiked(position: Int, item: StoryPost, heart: Heart, itemView: View)
    }
}