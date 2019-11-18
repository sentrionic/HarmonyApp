package xyz.sentrionic.harmony.ui.main.account


import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.customview.getCustomView
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import kotlinx.android.synthetic.main.layout_center_profile.*
import kotlinx.android.synthetic.main.snippet_top_profile.*
import xyz.sentrionic.harmony.R
import xyz.sentrionic.harmony.models.AccountProperties
import xyz.sentrionic.harmony.models.StoryPost
import xyz.sentrionic.harmony.session.SessionManager
import xyz.sentrionic.harmony.ui.main.account.state.AccountStateEvent.GetAccountPropertiesEvent
import xyz.sentrionic.harmony.ui.main.account.state.AccountStateEvent.GetAccountStoriesEvent
import xyz.sentrionic.harmony.ui.main.account.viewmodel.handleIncomingStoryListData
import xyz.sentrionic.harmony.ui.main.account.viewmodel.nextPage
import xyz.sentrionic.harmony.ui.main.account.viewmodel.setQueryExhausted
import xyz.sentrionic.harmony.util.ErrorHandling
import xyz.sentrionic.harmony.util.GridListAdapter
import xyz.sentrionic.harmony.util.GridSpacingItemDecoration
import javax.inject.Inject


class AccountFragment : BaseAccountFragment(), GridListAdapter.Interaction {

    @Inject
    lateinit var sessionManager: SessionManager

    @Inject
    lateinit var requestManager: RequestManager

    private lateinit var recyclerAdapter: GridListAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_account, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)

        textEditProfile.setOnClickListener {
            findNavController().navigate(R.id.action_accountFragment_to_updateAccountFragment)
        }

        initRecyclerView()
        subscribeObservers()
    }

    private fun subscribeObservers() {
        viewModel.dataState.observe(viewLifecycleOwner, Observer{ dataState ->
            stateChangeListener.onDataStateChange(dataState)
            if (dataState != null) {
                dataState.data?.let { data ->
                    data.data?.let { event ->
                        event.getContentIfNotHandled()?.let { viewState ->
                            viewState.accountProperties?.let { accountProperties ->
                                Log.d(TAG, "AccountFragment, DataState: ${accountProperties}")
                                viewModel.setAccountPropertiesData(accountProperties)
                                loadImages(accountProperties.username)
                            }

                            viewState.accountStories?.let { accountStories ->
                                Log.d(TAG, "AccountFragment, DataState: ${accountStories}")
                                viewModel.handleIncomingStoryListData(accountStories)
                            }
                        }
                    }
                }

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
        })

        viewModel.viewState.observe(viewLifecycleOwner, Observer { viewState->
            if (viewState != null) {
                viewState.accountProperties?.let {
                    Log.d(TAG, "AccountFragment, ViewState: ${it}")
                    setAccountDataFields(it)
                }

                viewState.accountStories?.let {
                    Log.d(TAG, "AccountFragment, ViewState: ${it}")
                    recyclerAdapter.apply {
                        preloadGlideImages(
                            requestManager = requestManager,
                            list = it.storyList!!
                        )
                        submitList(
                            storyList = it.storyList
                        )
                    }
                }
            }
        })
    }

    override fun onResume() {
        super.onResume()
        viewModel.setStateEvent(GetAccountPropertiesEvent())
    }

    private fun setAccountDataFields(accountProperties: AccountProperties) {
        display_name?.text = accountProperties.display_name
        description?.text = accountProperties.description
        website?.text = accountProperties.website
        tvPosts?.text = accountProperties.posts.toString()
        tvFollowers?.text = accountProperties.followers.toString()
        tvFollowing?.text = accountProperties.following.toString()

        view?.let {
            Glide.with(it.context)
                .load(accountProperties.image)
                .into(profile_photo)
        }

        (activity as AppCompatActivity).supportActionBar?.title = accountProperties.username
    }

    private fun loadImages(username: String) {
        viewModel.setStateEvent(GetAccountStoriesEvent(username))
    }

    private fun initRecyclerView() {

        user_stories_recyclerview.apply {
            layoutManager = GridLayoutManager(this@AccountFragment.context, 3)
            val gridSpacingItemDecoration = GridSpacingItemDecoration(10)
            removeItemDecoration(gridSpacingItemDecoration) // does nothing if not applied already
            addItemDecoration(gridSpacingItemDecoration)

            recyclerAdapter =
                GridListAdapter(requestManager, this@AccountFragment)
            addOnScrollListener(object : RecyclerView.OnScrollListener() {

                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)
                    val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                    val lastPosition = layoutManager.findLastVisibleItemPosition()
                    if (lastPosition == recyclerAdapter.itemCount.minus(1)) {
                        Log.d(TAG, "AccountFragment: attempting to load next page...")
                        viewModel.nextPage()
                    }
                }
            })
            adapter = recyclerAdapter
        }
    }

    private fun showDialog() {

        activity?.let {
            val dialog = MaterialDialog(it)
                .noAutoDismiss()
                .customView(R.layout.layout_profile_dialog)

            val view = dialog.getCustomView()

            view.findViewById<Button>(R.id.dialog_nav_edit_profile).setOnClickListener {
                findNavController().navigate(R.id.action_accountFragment_to_updateAccountFragment)
                dialog.dismiss()
            }

            view.findViewById<Button>(R.id.dialog_nav_change_password).setOnClickListener {
                findNavController().navigate(R.id.action_accountFragment_to_changePasswordFragment)
                dialog.dismiss()
            }

            view.findViewById<Button>(R.id.dialog_nav_logout).setOnClickListener {
                viewModel.logout()
                dialog.dismiss()
            }

            view.findViewById<TextView>(R.id.negative_button).setOnClickListener {
                dialog.dismiss()
            }

            dialog.show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // clear references (can leak memory)
        user_stories_recyclerview.adapter = null
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.settings_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.settings -> {
                showDialog()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onItemSelected(position: Int, item: StoryPost) {
        Log.d(TAG, item.toString())
    }

}
