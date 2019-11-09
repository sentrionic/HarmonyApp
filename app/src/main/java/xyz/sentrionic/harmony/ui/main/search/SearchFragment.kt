package xyz.sentrionic.harmony.ui.main.search


import android.app.SearchManager
import android.content.Context.SEARCH_SERVICE
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.customview.getCustomView
import kotlinx.android.synthetic.main.fragment_search.*

import xyz.sentrionic.harmony.R
import xyz.sentrionic.harmony.models.StoryPost
import xyz.sentrionic.harmony.persistence.StoryQueryUtils.Companion.STORY_FILTER_DATE_PUBLISHED
import xyz.sentrionic.harmony.persistence.StoryQueryUtils.Companion.STORY_FILTER_USERNAME
import xyz.sentrionic.harmony.persistence.StoryQueryUtils.Companion.STORY_ORDER_ASC
import xyz.sentrionic.harmony.ui.DataState
import xyz.sentrionic.harmony.ui.main.story.StoryListAdapter
import xyz.sentrionic.harmony.ui.main.story.state.StoryViewState
import xyz.sentrionic.harmony.ui.main.story.viewmodel.*
import xyz.sentrionic.harmony.util.ErrorHandling
import xyz.sentrionic.harmony.util.Heart
import xyz.sentrionic.harmony.util.TopSpacingItemDecoration


class SearchFragment : BaseSearchFragment(), StoryListAdapter.Interaction,
    SwipeRefreshLayout.OnRefreshListener {

    private lateinit var searchView: SearchView
    private lateinit var recyclerAdapter: StoryListAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_search, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
        (activity as AppCompatActivity).supportActionBar?.setDisplayShowTitleEnabled(false)
        swipe_refresh.setOnRefreshListener(this)

        initRecyclerView()
        subscribeObservers()
        if (savedInstanceState == null) onStorySearchOrFilter()
    }

    private fun handlePagination(dataState: DataState<StoryViewState>) {

        // Handle incoming data from DataState
        dataState.data?.let {
            it.data?.let {
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

    private fun subscribeObservers() {
        viewModel.dataState.observe(viewLifecycleOwner, Observer { dataState ->
            if (dataState != null) {
                handlePagination(dataState)
                stateChangeListener.onDataStateChange(dataState)
            }
        })

        viewModel.viewState.observe(viewLifecycleOwner, Observer { viewState ->
            Log.d(TAG, "SearchFragment, ViewState: ${viewState}")
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

    private fun initRecyclerView() {

        story_post_recyclerview.apply {
            layoutManager = LinearLayoutManager(this@SearchFragment.context)
            val topSpacingDecorator = TopSpacingItemDecoration(10)
            removeItemDecoration(topSpacingDecorator) // does nothing if not applied already
            addItemDecoration(topSpacingDecorator)

            recyclerAdapter = StoryListAdapter(requestManager, this@SearchFragment)
            addOnScrollListener(object : RecyclerView.OnScrollListener() {

                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)
                    val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                    val lastPosition = layoutManager.findLastVisibleItemPosition()
                    if (lastPosition == recyclerAdapter.itemCount.minus(1)) {
                        Log.d(TAG, "SearchFragment: attempting to load next page...")
                        viewModel.nextPage()
                    }
                }
            })
            adapter = recyclerAdapter
        }

    }

    private fun initSearchView(menu: Menu) {
        activity?.apply {
            val searchManager: SearchManager = getSystemService(SEARCH_SERVICE) as SearchManager
            searchView = menu.findItem(R.id.action_search).actionView as SearchView
            searchView.setSearchableInfo(searchManager.getSearchableInfo(componentName))
            searchView.maxWidth = Integer.MAX_VALUE
            searchView.setIconifiedByDefault(true)
            searchView.isSubmitButtonEnabled = true
        }

        // ENTER ON COMPUTER KEYBOARD OR ARROW ON VIRTUAL KEYBOARD
        val searchPlate = searchView.findViewById(R.id.search_src_text) as EditText
        searchPlate.setOnEditorActionListener { v, actionId, event ->

            if (actionId == EditorInfo.IME_ACTION_UNSPECIFIED
                || actionId == EditorInfo.IME_ACTION_SEARCH
            ) {
                val searchQuery = v.text.toString()
                Log.e(TAG, "SearchView: (keyboard or arrow) executing search...: ${searchQuery}")
                viewModel.setQuery(searchQuery).let {
                    onStorySearchOrFilter()
                }
            }
            true
        }

        // SEARCH BUTTON CLICKED (in toolbar)
        val searchButton = searchView.findViewById(R.id.search_go_btn) as View
        searchButton.setOnClickListener {
            val searchQuery = searchPlate.text.toString()
            Log.e(TAG, "SearchView: (button) executing search...: ${searchQuery}")
            viewModel.setQuery(searchQuery).let {
                onStorySearchOrFilter()
            }

        }
    }

    private fun onStorySearchOrFilter() {
        viewModel.loadFirstPage().let {
            resetUI()
        }
    }

    private fun resetUI() {
        story_post_recyclerview.smoothScrollToPosition(0)
        stateChangeListener.hideSoftKeyboard()
        focusable_view.requestFocus()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.search_menu, menu)
        initSearchView(menu)
    }

    override fun onItemSelected(position: Int, item: StoryPost) {
        Log.d(TAG, "onItemSelected: position, StoryPost: $position, ${item}")

        viewModel.setStoryPost(item)
        findNavController().navigate(R.id.action_searchFragment_to_viewStoryFragment2)
    }

    override fun onItemLiked(position: Int, item: StoryPost, heart: Heart, itemView: View) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // clear references (can leak memory)
        story_post_recyclerview.adapter = null
    }

    override fun onRefresh() {
        onStorySearchOrFilter()
        swipe_refresh.isRefreshing = false
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            R.id.action_filter_settings -> {
                showFilterDialog()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun showFilterDialog() {

        activity?.let {
            val dialog = MaterialDialog(it)
                .noAutoDismiss()
                .customView(R.layout.layout_story_filter)

            val view = dialog.getCustomView()

            val filter = viewModel.getFilter()
            val order = viewModel.getOrder()

            if (filter == STORY_FILTER_DATE_PUBLISHED) {
                view.findViewById<RadioGroup>(R.id.filter_group).check(R.id.filter_date)
            } else {
                view.findViewById<RadioGroup>(R.id.filter_group).check(R.id.filter_author)
            }

            if (order == STORY_ORDER_ASC) {
                view.findViewById<RadioGroup>(R.id.order_group).check(R.id.filter_asc)
            } else {
                view.findViewById<RadioGroup>(R.id.order_group).check(R.id.filter_desc)
            }

            view.findViewById<TextView>(R.id.positive_button).setOnClickListener {
                Log.d(TAG, "FilterDialog: apply filter.")

                val selectedFilter = dialog.getCustomView().findViewById<RadioButton>(
                    dialog.getCustomView().findViewById<RadioGroup>(R.id.filter_group).checkedRadioButtonId
                )
                val selectedOrder = dialog.getCustomView().findViewById<RadioButton>(
                    dialog.getCustomView().findViewById<RadioGroup>(R.id.order_group).checkedRadioButtonId
                )

                var filter = STORY_FILTER_DATE_PUBLISHED
                if (selectedFilter.text.toString() == getString(R.string.filter_author)) {
                    filter = STORY_FILTER_USERNAME
                }

                var order = ""
                if (selectedOrder.text.toString() == getString(R.string.filter_desc)) {
                    order = "-"
                }
                viewModel.saveFilterOptions(filter, order).let {
                    viewModel.setStoryFilter(filter)
                    viewModel.setStoryOrder(order)
                    onStorySearchOrFilter()
                }
                dialog.dismiss()
            }

            view.findViewById<TextView>(R.id.negative_button).setOnClickListener {
                Log.d(TAG, "FilterDialog: cancelling filter.")
                dialog.dismiss()
            }

            dialog.show()
        }
    }
}
