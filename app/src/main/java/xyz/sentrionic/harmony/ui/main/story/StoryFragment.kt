package xyz.sentrionic.harmony.ui.main.story


import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import kotlinx.android.synthetic.main.fragment_story.*
import kotlinx.android.synthetic.main.layout_story_list_item.view.*
import xyz.sentrionic.harmony.R
import xyz.sentrionic.harmony.models.StoryPost
import xyz.sentrionic.harmony.ui.DataState
import xyz.sentrionic.harmony.ui.main.story.state.StoryStateEvent.LikeStoryPostEvent
import xyz.sentrionic.harmony.ui.main.story.state.StoryViewState
import xyz.sentrionic.harmony.ui.main.story.viewmodel.*
import xyz.sentrionic.harmony.util.ErrorHandling
import xyz.sentrionic.harmony.util.Heart
import xyz.sentrionic.harmony.util.TopSpacingItemDecoration

class StoryFragment : BaseStoryFragment(), StoryListAdapter.Interaction, SwipeRefreshLayout.OnRefreshListener {

    private lateinit var recyclerAdapter: StoryListAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_story, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
        swipe_refresh.setOnRefreshListener(this)

        initRecyclerView()
        subscribeObservers()

        if (savedInstanceState == null) viewModel.loadFirstPage()
    }


    private fun subscribeObservers() {
        viewModel.dataState.observe(viewLifecycleOwner, Observer { dataState ->
            if (dataState != null) {
                handlePagination(dataState)
                stateChangeListener.onDataStateChange(dataState)
            }
        })

        viewModel.viewState.observe(viewLifecycleOwner, Observer { viewState ->
            Log.d(TAG, "StoryFragment, ViewState: ${viewState}")
            if (viewState != null) {
                recyclerAdapter.apply {
                    preloadGlideImages(
                        requestManager = requestManager,
                        list = viewState.storyFields.storyList
                    )
                    submitList(
                        storyList = viewState.storyFields.storyList,
                        isQueryExhausted = viewState.storyFields.isQueryExhausted
                    )
                }
            }
        })
    }

    private fun handlePagination(dataState: DataState<StoryViewState>) {

        // Handle incoming data from DataState
        dataState.data?.let {
            it.data?.let{
                it.getContentIfNotHandled()?.let {
                    viewModel.handleIncomingStoryListData(it)
                }
            }
        }

        // Check for pagination end (no more results)
        // must do this b/c server will return an ApiErrorResponse if page is not valid,
        // -> meaning there is no more data.
        dataState.error?.let { event ->
            event.peekContent().response.message?.let {
                if (ErrorHandling.isPaginationDone(it)) {

                    // handle the error message event so it doesn't display in UI
                    event.getContentIfNotHandled()

                    // set query exhausted to update RecyclerView with
                    // "No more results..." list item
                    viewModel.setQueryExhausted(true)
                }
            }
        }
    }

    private fun initRecyclerView() {

        story_post_recyclerview.apply {
            layoutManager = LinearLayoutManager(this@StoryFragment.context)
            val topSpacingDecorator = TopSpacingItemDecoration(10)
            removeItemDecoration(topSpacingDecorator) // does nothing if not applied already
            addItemDecoration(topSpacingDecorator)

            recyclerAdapter = StoryListAdapter(requestManager,  this@StoryFragment)
            addOnScrollListener(object: RecyclerView.OnScrollListener() {

                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)
                    val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                    val lastPosition = layoutManager.findLastVisibleItemPosition()
                    if (lastPosition == recyclerAdapter.itemCount.minus(1)) {
                        Log.d(TAG, "StoryFragment: attempting to load next page...")
                        viewModel.nextPage()
                    }
                }
            })
            adapter = recyclerAdapter
        }

    }

    override fun onItemSelected(position: Int, item: StoryPost) {
        viewModel.setStoryPost(item)
        findNavController().navigate(R.id.action_storyFragment_to_viewStoryFragment)
    }

    override fun onItemLiked(position: Int, item: StoryPost, heart: Heart, itemView: View) {
        viewModel.setStoryPost(item)
        viewModel.setStateEvent(LikeStoryPostEvent())
        val likes = item.likes + heart.toggleLike()
        itemView.story_image_likes.text = resources.getQuantityString(R.plurals.likes, likes, likes)
    }

    override fun onProfileSelected(position: Int, item: StoryPost) {
        viewModel.setUsername(item.username)
        findNavController().navigate(R.id.action_storyFragment_to_profileFragment2)
    }

    override fun onCommentSelected(position: Int, item: StoryPost) {
        viewModel.setStoryPost(item)
        findNavController().navigate(R.id.action_storyFragment_to_viewCommentFragment)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // clear references (can leak memory)
        story_post_recyclerview.adapter = null
    }

    override fun onRefresh() {
        onStorySearch()
        swipe_refresh.isRefreshing = false
    }

    private fun onStorySearch() {
        viewModel.loadFirstPage().let {
            resetUI()
        }
    }

    private  fun resetUI() {
        story_post_recyclerview.smoothScrollToPosition(0)
        stateChangeListener.hideSoftKeyboard()
        focusable_view.requestFocus()
    }
}
