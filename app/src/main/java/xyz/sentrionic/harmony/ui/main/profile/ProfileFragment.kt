package xyz.sentrionic.harmony.ui.main.profile


import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.layout_center_profile.*
import kotlinx.android.synthetic.main.snippet_top_profile.*
import xyz.sentrionic.harmony.R
import xyz.sentrionic.harmony.models.Profile
import xyz.sentrionic.harmony.ui.main.search.BaseSearchFragment
import xyz.sentrionic.harmony.ui.main.search.viewmodel.setProfile
import xyz.sentrionic.harmony.ui.main.story.state.StoryStateEvent.FollowProfileEvent
import xyz.sentrionic.harmony.ui.main.story.state.StoryStateEvent.GetProfilePropertiesEvent


class ProfileFragment : BaseSearchFragment() {

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
                            }
                        }
                    }
                }
            }
        })

        viewModel.viewState.observe(viewLifecycleOwner, Observer { viewState ->
            viewState.viewProfileFields.profile?.let { profile ->
                setProfileProperties(profile)
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

    override fun onResume() {
        super.onResume()
        viewModel.setStateEvent(GetProfilePropertiesEvent())
    }

}
