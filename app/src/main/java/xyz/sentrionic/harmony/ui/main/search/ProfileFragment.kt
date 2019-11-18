package xyz.sentrionic.harmony.ui.main.search


import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.layout_center_profile.*
import kotlinx.android.synthetic.main.snippet_top_profile.*
import xyz.sentrionic.harmony.R
import xyz.sentrionic.harmony.models.Profile
import xyz.sentrionic.harmony.models.StoryPost
import xyz.sentrionic.harmony.ui.main.story.state.StoryStateEvent
import xyz.sentrionic.harmony.ui.main.story.state.StoryStateEvent.FollowProfileEvent
import xyz.sentrionic.harmony.ui.main.story.state.StoryStateEvent.GetProfilePropertiesEvent
import xyz.sentrionic.harmony.ui.main.story.viewmodel.*
import xyz.sentrionic.harmony.util.ErrorHandling
import xyz.sentrionic.harmony.util.GridListAdapter
import xyz.sentrionic.harmony.util.GridSpacingItemDecoration


class ProfileFragment : BaseSearchFragment(), GridListAdapter.Interaction {

    private lateinit var recyclerAdapter: GridListAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
        subscribeObservers()
        stateChangeListener.expandAppBar()
        initRecyclerView()
    }

    fun subscribeObservers() {
        viewModel.dataState.observe(viewLifecycleOwner, Observer { dataState ->
            stateChangeListener.onDataStateChange(dataState)

            if (dataState != null) {
                dataState.data?.let { data ->
                    data.data?.let { event ->
                        event.getContentIfNotHandled()?.let { viewState ->
                            viewState.viewProfileFields.profile?.let { profile ->
                                Log.d(TAG, "ProfileFragment, DataState: ${profile}")
                                viewModel.setProfile(profile)
                                loadImages()
                            }

                            viewState.viewProfileFields.profileStories?.let { stories ->
                                Log.d(TAG, "ProfileFragment, DataState: ${stories}")
                                viewModel.handleIncomingProfileStoryListData(viewState)
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
                            viewModel.setProfileStoryListQueryExhausted(true)
                        }
                    }
                }
            }
        })

        viewModel.viewState.observe(viewLifecycleOwner, Observer { viewState ->
            viewState.viewProfileFields.profile?.let { profile ->
                setProfileProperties(profile)
            }

            viewState.viewProfileFields.profileStories?.let { stories ->
                Log.d(TAG, "ProfileFragment, ViewState: ${stories}")
                recyclerAdapter.apply {
                    preloadGlideImages(
                        requestManager = requestManager,
                        list = stories
                    )
                    submitList(
                        storyList = stories
                    )
                }
            }
        })
    }

    private fun setProfileProperties(profile: Profile) {
        textEditProfile.isVisible = false
        display_name?.text = profile.display_name
        description?.text = profile.description
        website?.text = profile.website
        tvPosts?.text = profile.posts.toString()
        tvFollowers?.text = profile.followers.toString()
        tvFollowing?.text = profile.following.toString()

        follow.setOnClickListener {
            toggleFollow(profile)
        }

        unfollow.setOnClickListener {
            toggleFollow(profile)
        }

        view?.let {
            Glide.with(it.context)
                .load(profile.image)
                .into(profile_photo)
        }

        if (profile.isFollowing) {
            follow.isVisible = false
            unfollow.isVisible = true
        } else {
            follow.isVisible = true
            unfollow.isVisible = false
        }

        if (profile.pk == sessionManager.cachedToken.value?.account_pk) {
            follow.isVisible = false
            unfollow.isVisible = false
            textEditProfile.isVisible = true
            textEditProfile.text = ""
        }

        (activity as AppCompatActivity).supportActionBar?.title = profile.username
        (activity as AppCompatActivity).supportActionBar?.setDisplayShowTitleEnabled(true)
    }

    private fun toggleFollow(profile: Profile) {
        viewModel.setStateEvent(FollowProfileEvent())

        if (profile.isFollowing) {
            tvFollowers.text = (profile.followers.minus(1)).toString()
        }
        else {
            tvFollowers.text = (profile.followers.plus(1)).toString()
        }

        follow.isVisible = profile.isFollowing
        unfollow.isVisible = !profile.isFollowing
    }

    private fun loadImages() {
        viewModel.setStateEvent(StoryStateEvent.GetProfileStoriesEvent())
    }

    private fun initRecyclerView() {

        user_stories_recyclerview.apply {
            layoutManager = GridLayoutManager(this@ProfileFragment.context, 3)
            val gridSpacingItemDecoration = GridSpacingItemDecoration(10)
            removeItemDecoration(gridSpacingItemDecoration) // does nothing if not applied already
            addItemDecoration(gridSpacingItemDecoration)

            recyclerAdapter =
                GridListAdapter(requestManager, this@ProfileFragment)
            addOnScrollListener(object : RecyclerView.OnScrollListener() {

                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)
                    val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                    val lastPosition = layoutManager.findLastVisibleItemPosition()
                    if (lastPosition == recyclerAdapter.itemCount.minus(1)) {
                        Log.d(TAG, "AccountFragment: attempting to load next page...")
                        viewModel.nextProfileStoryPage()
                    }
                }
            })
            adapter = recyclerAdapter
        }
    }

    override fun onItemSelected(position: Int, item: StoryPost) {
        viewModel.setStoryPost(item)
        //findNavController().navigate(R.id.action_profileFragment2_to_viewStoryFragment)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // clear references (can leak memory)
        user_stories_recyclerview.adapter = null
    }

    override fun onResume() {
        super.onResume()
        viewModel.setStateEvent(GetProfilePropertiesEvent())
    }

}
