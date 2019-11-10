package xyz.sentrionic.harmony.util

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.*
import com.bumptech.glide.RequestManager
import kotlinx.android.synthetic.main.layout_grid_imageview.view.*
import xyz.sentrionic.harmony.R
import xyz.sentrionic.harmony.models.StoryPost

class GridListAdapter(private val requestManager: RequestManager,
                      private val interaction: Interaction? = null) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val STORY_ITEM = 0

    private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<StoryPost>() {

        override fun areItemsTheSame(oldItem: StoryPost, newItem: StoryPost): Boolean {
            return oldItem.pk == newItem.pk
        }

        override fun areContentsTheSame(oldItem: StoryPost, newItem: StoryPost): Boolean {
            return oldItem == newItem
        }

    }

    private val differ =
        AsyncListDiffer(
            GridRecyclerChangeCallback(this),
            AsyncDifferConfig.Builder(DIFF_CALLBACK).build()
        )


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        when (viewType) {

            STORY_ITEM ->{
                return GridViewHolder(
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.layout_grid_imageview, parent, false),
                    interaction = interaction,
                    requestManager = requestManager
                )
            }
            else -> {
                return GridViewHolder(
                    LayoutInflater.from(parent.context).inflate(
                        R.layout.layout_grid_imageview,
                        parent,
                        false
                    ),
                    interaction = interaction,
                    requestManager = requestManager
                )
            }
        }
    }

    internal inner class GridRecyclerChangeCallback(
        private val adapter: GridListAdapter
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
            is GridViewHolder -> {
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

    fun submitList(storyList: List<StoryPost>?) {
        val newList = storyList?.toMutableList()
        differ.submitList(newList)
    }

    class GridViewHolder
    constructor(
        itemView: View,
        val requestManager: RequestManager,
        private val interaction: Interaction?
    ) : RecyclerView.ViewHolder(itemView) {

        fun bind(item: StoryPost) = with(itemView) {
            itemView.gridImageView.setOnClickListener {
                interaction?.onItemSelected(adapterPosition, item)
            }

            requestManager
                .load(item.image)
                .dontAnimate()
                .into(itemView.gridImageView)
        }
    }

    interface Interaction {
        fun onItemSelected(position: Int, item: StoryPost)
    }
}