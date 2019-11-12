package xyz.sentrionic.harmony.ui.main.story

import android.os.Bundle
import android.view.*
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.fragment_view_story.*
import xyz.sentrionic.harmony.R
import xyz.sentrionic.harmony.models.StoryPost
import xyz.sentrionic.harmony.ui.AreYouSureCallback
import xyz.sentrionic.harmony.ui.UIMessage
import xyz.sentrionic.harmony.ui.UIMessageType
import xyz.sentrionic.harmony.ui.main.search.viewmodel.setUsername
import xyz.sentrionic.harmony.ui.main.story.state.StoryStateEvent.*
import xyz.sentrionic.harmony.ui.main.story.viewmodel.isAuthorOfStoryPost
import xyz.sentrionic.harmony.ui.main.story.viewmodel.removeDeletedStoryPost
import xyz.sentrionic.harmony.ui.main.story.viewmodel.setIsAuthorOfStoryPost
import xyz.sentrionic.harmony.util.DateUtils
import xyz.sentrionic.harmony.util.Heart
import xyz.sentrionic.harmony.util.SuccessHandling.Companion.SUCCESS_STORY_DELETED

class ViewStoryFragment : BaseStoryFragment() {


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_view_story, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
        subscribeObservers()
        checkIsAuthorOfStoryPost()
        stateChangeListener.expandAppBar()
    }

    fun subscribeObservers() {
        viewModel.dataState.observe(viewLifecycleOwner, Observer { dataState ->
            stateChangeListener.onDataStateChange(dataState)

            dataState.data?.let { data ->
                data.data?.getContentIfNotHandled()?.let { viewState ->
                    viewModel.setIsAuthorOfStoryPost(
                        viewState.viewStoryFields.isAuthorOfStoryPost
                    )
                }

                data.response?.peekContent()?.let { response ->
                    if (response.message.toString() == SUCCESS_STORY_DELETED) {
                        viewModel.removeDeletedStoryPost()
                        findNavController().popBackStack()
                    }
                }
            }
        })

        viewModel.viewState.observe(viewLifecycleOwner, Observer { viewState ->
            viewState.viewStoryFields.storyPost?.let { storyPost ->
                setStoryProperties(storyPost)
            }

            if (viewState.viewStoryFields.isAuthorOfStoryPost) {
                adaptViewToAuthorMode()
            }
        })
    }

    private fun checkIsAuthorOfStoryPost() {
        viewModel.setIsAuthorOfStoryPost(false) // reset
        viewModel.setStateEvent(CheckAuthorOfStoryPost())
    }

    private fun setStoryProperties(storyPost: StoryPost) {
        requestManager
            .load(storyPost.image)
            .into(story_image)

        requestManager
            .load(storyPost.profile_image)
            .into(story_profile_photo)

        story_username.text = storyPost.username
        story_caption_username.text = storyPost.username
        story_image_caption.text = storyPost.caption
        story_image_likes.text = resources.getQuantityString(R.plurals.likes, storyPost.likes, storyPost.likes)

        if (storyPost.liked) {
            story_image_heart_red.visibility = View.VISIBLE
            story_image_heart.visibility = View.GONE
        }
        else {
            story_image_heart_red.visibility = View.GONE
            story_image_heart.visibility = View.VISIBLE
        }

        image_time_posted.text = DateUtils.convertLongToStringDate(storyPost.date_published)

        // Like Interaction
        story_image_heart.setOnClickListener {
            toggleLike(storyPost)
        }

        story_image_heart_red.setOnClickListener {
            toggleLike(storyPost)
        }

        // Profile Interaction
        story_username.setOnClickListener {
            goToProfile(storyPost.username)
        }

        story_profile_photo.setOnClickListener {
            goToProfile(storyPost.username)
        }
    }

    private fun toggleLike(storyPost: StoryPost) {
        val heart = Heart(heartWhite = story_image_heart, heartRed = story_image_heart_red)
        viewModel.setStateEvent(LikeStoryPostEvent())
        val likes = storyPost.likes + heart.toggleLike()
        story_image_likes.text = resources.getQuantityString(R.plurals.likes, likes, likes)
    }

    private fun goToProfile(username: String) {
        viewModel.setUsername(username)
        findNavController().navigate(R.id.action_viewStoryFragment_to_profileFragment2)
    }

    private fun deleteStoryPost() {
        viewModel.setStateEvent(
            DeleteStoryPostEvent()
        )
    }

    private fun adaptViewToAuthorMode() {
        activity?.invalidateOptionsMenu()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        if (viewModel.isAuthorOfStoryPost()) {
            inflater.inflate(R.menu.edit_view_menu, menu)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (viewModel.isAuthorOfStoryPost()) {
            when (item.itemId) {
                R.id.edit -> {
                    return true
                }

                R.id.delete -> {
                    confirmDeleteRequest()
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun confirmDeleteRequest(){
        val callback: AreYouSureCallback = object: AreYouSureCallback {

            override fun proceed() {
                deleteStoryPost()
            }

            override fun cancel() {
                // ignore
            }

        }
        uiCommunicationListener.onUIMessageReceived(
            UIMessage(
                getString(R.string.are_you_sure_delete),
                UIMessageType.AreYouSureDialog(callback)
            )
        )
    }

}