package xyz.sentrionic.harmony.ui.main.story

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.*
import com.bumptech.glide.RequestManager
import kotlinx.android.synthetic.main.item_comment.view.*
import xyz.sentrionic.harmony.R
import xyz.sentrionic.harmony.models.Comment
import xyz.sentrionic.harmony.util.DateUtils

class CommentListAdapter(private val requestManager: RequestManager) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val COMMENT_ITEM = 0

    val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Comment>() {

        override fun areItemsTheSame(oldItem: Comment, newItem: Comment): Boolean {
            return oldItem.pk == newItem.pk
        }

        override fun areContentsTheSame(oldItem: Comment, newItem: Comment): Boolean {
            return oldItem == newItem
        }

    }

    private val differ =
        AsyncListDiffer(
            CommentRecyclerChangeCallback(this),
            AsyncDifferConfig.Builder(DIFF_CALLBACK).build()
        )


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        when (viewType) {

            COMMENT_ITEM ->{
                return CommentViewHolder(
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.item_comment, parent, false),
                    requestManager = requestManager
                )
            }
            else -> {
                return CommentViewHolder(
                    LayoutInflater.from(parent.context).inflate(R.layout.item_comment, parent, false),
                    requestManager = requestManager
                )
            }
        }
    }

    internal inner class CommentRecyclerChangeCallback(
        private val adapter: CommentListAdapter
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
            is CommentViewHolder -> {
                holder.bind(differ.currentList[position])
            }
        }
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun getItemViewType(position: Int): Int {
        if (differ.currentList[position].pk > -1) {
            return COMMENT_ITEM
        }
        return differ.currentList[position].pk
    }

    // Prepare the images that will be displayed in the RecyclerView.
    // This also ensures if the network connection is lost, they will be in the cache
    fun preloadGlideImages(
        requestManager: RequestManager,
        list: List<Comment>
    ) {
        for (comment in list){
            requestManager
                .load(comment.image)
                .preload()
        }
    }

    fun submitList(commentList: List<Comment>?) {
        val newList = commentList?.toMutableList()
        differ.submitList(newList)
    }

    class CommentViewHolder
    constructor(
        itemView: View,
        val requestManager: RequestManager
    ) : RecyclerView.ViewHolder(itemView) {

        fun bind(item: Comment) = with(itemView) {

            requestManager
                .load(item.image)
                .into(itemView.comment_profile_image)

            itemView.comment_username.text = item.username
            itemView.comment.text = item.comment
            itemView.comment_likes.text = resources.getQuantityString(R.plurals.likes, item.likes, item.likes)
            itemView.comment_time_posted.text = DateUtils.convertLongToStringDate(item.date_published)
        }
    }

}