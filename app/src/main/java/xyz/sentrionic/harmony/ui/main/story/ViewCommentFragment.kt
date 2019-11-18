package xyz.sentrionic.harmony.ui.main.story


import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import kotlinx.android.synthetic.main.fragment_view_comment.*
import kotlinx.android.synthetic.main.snippet_comment_top.*
import xyz.sentrionic.harmony.R
import xyz.sentrionic.harmony.ui.DataState
import xyz.sentrionic.harmony.ui.main.story.state.StoryStateEvent
import xyz.sentrionic.harmony.ui.main.story.state.StoryViewState
import xyz.sentrionic.harmony.ui.main.story.viewmodel.*
import xyz.sentrionic.harmony.util.DateUtils
import xyz.sentrionic.harmony.util.ErrorHandling
import xyz.sentrionic.harmony.util.TopSpacingItemDecoration


class ViewCommentFragment : BaseStoryFragment(), SwipeRefreshLayout.OnRefreshListener {

    private lateinit var recyclerAdapter: CommentListAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_view_comment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
        swipe_refresh.setOnRefreshListener(this)

        setStoryPostDescription()
        initRecyclerView()
        subscribeObservers()
        initCommentField()

        if (savedInstanceState == null) viewModel.loadFirstCommentPage()
    }

    private fun subscribeObservers() {
        viewModel.dataState.observe(viewLifecycleOwner, Observer { dataState ->
            if (dataState != null) {
                handlePagination(dataState)
                stateChangeListener.onDataStateChange(dataState)
            }
        })

        viewModel.viewState.observe(viewLifecycleOwner, Observer { viewState ->
            Log.d(TAG, "ViewCommentFragment, ViewState: ${viewState.viewCommentsFields}")
            if (viewState != null) {
                recyclerAdapter.apply {
                    preloadGlideImages(
                        requestManager = requestManager,
                        list = viewState.viewCommentsFields.commentList
                    )
                    submitList(
                        commentList = viewState.viewCommentsFields.commentList
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
                    viewModel.handleIncomingCommentListData(it)
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
                    viewModel.setCommentQueryExhausted(true)
                }
            }
        }
    }

    private fun initRecyclerView() {

        comment_recyclerview.apply {
            layoutManager = LinearLayoutManager(this@ViewCommentFragment.context)
            val topSpacingDecorator = TopSpacingItemDecoration(5)
            removeItemDecoration(topSpacingDecorator) // does nothing if not applied already
            addItemDecoration(topSpacingDecorator)

            recyclerAdapter = CommentListAdapter(requestManager)
            addOnScrollListener(object: RecyclerView.OnScrollListener() {

                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)
                    val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                    val lastPosition = layoutManager.findLastVisibleItemPosition()
                    if (lastPosition == recyclerAdapter.itemCount.minus(1)) {
                        Log.d(TAG, "StoryFragment: attempting to load next page...")
                        viewModel.nextCommentPage()
                    }
                }
            })
            adapter = recyclerAdapter
        }
    }

    private fun setStoryPostDescription() {
        val storyPost = viewModel.getStoryPost()

        requestManager
            .load(storyPost.profile_image)
            .into(description_profile_image)

        description_username.text = storyPost.username
        description.text = storyPost.caption
        description_time_posted.text = DateUtils.convertLongToStringDate(storyPost.date_published)

    }

    private fun initCommentField() {

        comment_send.isEnabled = false

        comment_edittext.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                comment_send.isEnabled = s?.isNotBlank()!!
            }

        })

        comment_send.setOnClickListener {
            viewModel.setComment(comment_edittext.text.toString())
            viewModel.setStateEvent(StoryStateEvent.PostCommentEvent())
            clearUI()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // clear references (can leak memory)
        comment_recyclerview.adapter = null
    }

    override fun onRefresh() {
        onCommentsLoaded()
        swipe_refresh.isRefreshing = false
    }

    private fun onCommentsLoaded() {
        viewModel.loadFirstCommentPage().let {
            resetUI()
        }
    }

    private fun resetUI() {
        comment_recyclerview.smoothScrollToPosition(0)
        stateChangeListener.hideSoftKeyboard()
        focusable_view.requestFocus()
    }

    private fun clearUI() {
        comment_recyclerview.smoothScrollToPosition(recyclerAdapter.itemCount)
        stateChangeListener.hideSoftKeyboard()
        focusable_view.requestFocus()
        comment_edittext.setText("")
        viewModel.setComment("")
    }
}
