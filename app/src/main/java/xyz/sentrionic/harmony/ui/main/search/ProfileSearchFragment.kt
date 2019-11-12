package xyz.sentrionic.harmony.ui.main.search


import android.app.SearchManager
import android.content.Context.SEARCH_SERVICE
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.fragment_profile_search.*
import xyz.sentrionic.harmony.R
import xyz.sentrionic.harmony.models.Profile
import xyz.sentrionic.harmony.ui.DataState
import xyz.sentrionic.harmony.ui.main.search.viewmodel.*
import xyz.sentrionic.harmony.ui.main.story.state.StoryViewState
import xyz.sentrionic.harmony.util.ErrorHandling
import xyz.sentrionic.harmony.util.TopSpacingItemDecoration

class ProfileSearchFragment : BaseSearchFragment(), ProfileListAdapter.Interaction {
    
    private lateinit var searchView: SearchView
    private lateinit var recyclerAdapter: ProfileListAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile_search, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
        (activity as AppCompatActivity).supportActionBar?.setDisplayShowTitleEnabled(false)

        initRecyclerView()
        subscribeObservers()
    }

    private fun handlePagination(dataState: DataState<StoryViewState>) {

        // Handle incoming data from DataState
        dataState.data?.let {
            it.data?.let {
                it.getContentIfNotHandled()?.let {
                    viewModel.handleIncomingProfileListData(it)
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
                    viewModel.setUserQueryExhausted(true)
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
            Log.d(TAG, "ProfileSearchFragment, ViewState: ${viewState.userFields}")
            if (viewState != null && viewState.userFields.userList.isNotEmpty()) {
                recyclerAdapter.apply {
                    preloadGlideImages(
                        requestManager = requestManager,
                        list = viewState.userFields.userList
                    )
                    submitList(
                        storyList = viewState.userFields.userList
                    )
                }
            }
        })
    }

    private fun initRecyclerView() {

        search_profiles_recyclerview.apply {
            layoutManager = LinearLayoutManager(this@ProfileSearchFragment.context)
            val topSpacingDecorator = TopSpacingItemDecoration(10)
            removeItemDecoration(topSpacingDecorator) // does nothing if not applied already
            addItemDecoration(topSpacingDecorator)

            recyclerAdapter =
                ProfileListAdapter(requestManager, this@ProfileSearchFragment)
            addOnScrollListener(object : RecyclerView.OnScrollListener() {

                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)
                    val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                    val lastPosition = layoutManager.findLastVisibleItemPosition()
                    if (lastPosition == recyclerAdapter.itemCount.minus(1)) {
                        Log.d(TAG, "ProfileSearchFragment: attempting to load next page...")
                        viewModel.nextUserPage()
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
            searchView.queryHint = "Name"
        }

        // ENTER ON COMPUTER KEYBOARD OR ARROW ON VIRTUAL KEYBOARD
        val searchPlate = searchView.findViewById(R.id.search_src_text) as EditText
        searchPlate.setOnEditorActionListener { v, actionId, event ->

            if (actionId == EditorInfo.IME_ACTION_UNSPECIFIED
                || actionId == EditorInfo.IME_ACTION_SEARCH
            ) {
                val searchQuery = v.text.toString()
                Log.e(TAG, "SearchView: (keyboard or arrow) executing search...: ${searchQuery}")
                viewModel.setUserQuery(searchQuery).let {
                    onUserSearch()
                }
            }
            true
        }

        // SEARCH BUTTON CLICKED (in toolbar)
        val searchButton = searchView.findViewById(R.id.search_go_btn) as View
        searchButton.setOnClickListener {
            val searchQuery = searchPlate.text.toString()
            viewModel.setUserQuery(searchQuery).let {
                onUserSearch()
            }

        }

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                viewModel.setUserQuery(query!!).let {
                    onUserSearch()
                }
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText!!.length > 2) {
                    viewModel.setUserQuery(newText).let {
                        viewModel.loadFirstUserPage()
                    }
                }
                else {
                    recyclerAdapter.submitList(emptyList())
                }
                return true
            }

        })
    }

    private fun onUserSearch() {
        viewModel.loadFirstUserPage().let {
            resetUI()
        }
    }

    private fun resetUI() {
        search_profiles_recyclerview.smoothScrollToPosition(0)
        stateChangeListener.hideSoftKeyboard()
        focusable_view.requestFocus()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.search_menu, menu)
        menu.findItem(R.id.action_search_profile).isVisible = false
        initSearchView(menu)
    }
    
    override fun onProfileSelected(position: Int, item: Profile) {
        viewModel.setProfile(item)
        viewModel.setUsername(item.username)
        findNavController().navigate(R.id.action_profileSearchFragment_to_profileFragment)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // clear references (can leak memory)
        search_profiles_recyclerview.adapter = null
    }
}
