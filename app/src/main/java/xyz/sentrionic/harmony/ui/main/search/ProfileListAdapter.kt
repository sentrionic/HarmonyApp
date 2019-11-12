package xyz.sentrionic.harmony.ui.main.search

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.*
import com.bumptech.glide.RequestManager
import kotlinx.android.synthetic.main.item_search_list.view.*
import xyz.sentrionic.harmony.R
import xyz.sentrionic.harmony.models.Profile

class ProfileListAdapter(private val requestManager: RequestManager,
                       private val interaction: Interaction? = null) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val TAG: String = "AppDebug"
    private val PROFILE_ITEM = 0

    val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Profile>() {

        override fun areItemsTheSame(oldItem: Profile, newItem: Profile): Boolean {
            return oldItem.pk == newItem.pk
        }

        override fun areContentsTheSame(oldItem: Profile, newItem: Profile): Boolean {
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

            PROFILE_ITEM ->{
                return ProfileViewHolder(
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.item_search_list, parent, false),
                    interaction = interaction,
                    requestManager = requestManager
                )
            }
            else -> {
                return ProfileViewHolder(
                    LayoutInflater.from(parent.context).inflate(R.layout.item_search_list, parent, false),
                    interaction = interaction,
                    requestManager = requestManager
                )
            }
        }
    }

    internal inner class StoryRecyclerChangeCallback(
        private val adapter: ProfileListAdapter
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
            is ProfileViewHolder -> {
                holder.bind(differ.currentList[position])
            }
        }
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun getItemViewType(position: Int): Int {
        if (differ.currentList[position].pk > -1) {
            return PROFILE_ITEM
        }
        return differ.currentList[position].pk
    }

    // Prepare the images that will be displayed in the RecyclerView.
    // This also ensures if the network connection is lost, they will be in the cache
    fun preloadGlideImages(
        requestManager: RequestManager,
        list: List<Profile>
    ) {
        for (profile in list){
            requestManager
                .load(profile.image)
                .preload()
        }
    }

    fun submitList(storyList: List<Profile>?) {
        val newList = storyList?.toMutableList()
        differ.submitList(newList)
    }

    class ProfileViewHolder
    constructor(
        itemView: View,
        val requestManager: RequestManager,
        private val interaction: Interaction?
    ) : RecyclerView.ViewHolder(itemView) {

        fun bind(item: Profile) = with(itemView) {
            itemView.setOnClickListener {
                interaction?.onProfileSelected(adapterPosition, item)
            }

            requestManager
                .load(item.image)
                .into(itemView.user_image)

            itemView.user_name.text = item.username
            itemView.display_name.text = item.display_name
        }
    }

    interface Interaction {
        fun onProfileSelected(position: Int, item: Profile)
    }
}
