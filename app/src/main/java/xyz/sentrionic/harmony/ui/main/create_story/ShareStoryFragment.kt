package xyz.sentrionic.harmony.ui.main.create_story


import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.fragment_share.*
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import xyz.sentrionic.harmony.R
import xyz.sentrionic.harmony.ui.*
import xyz.sentrionic.harmony.ui.main.create_story.state.CreateStoryStateEvent.CreateNewStoryEvent
import xyz.sentrionic.harmony.util.ErrorHandling
import xyz.sentrionic.harmony.util.SuccessHandling.Companion.SUCCESS_STORY_CREATED
import java.io.File

class ShareStoryFragment : BaseCreateStoryFragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_share, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
        subscribeObservers()
    }

    fun subscribeObservers() {
        viewModel.dataState.observe(viewLifecycleOwner, Observer { dataState ->
            stateChangeListener.onDataStateChange(dataState)

            dataState.data?.let { data ->
                data.response?.let { event ->
                    event.peekContent().let { response ->
                        response.message?.let { message ->
                            if (message == SUCCESS_STORY_CREATED) {
                                viewModel.clearNewStoryFields()
                                findNavController().popBackStack()
                            }
                        }
                    }
                }
            }
        })

        viewModel.viewState.observe(viewLifecycleOwner, Observer { viewState ->
            viewState.storyFields.let { newStoryFields ->
                setStoryProperties(
                    newStoryFields.newStoryCaption,
                    newStoryFields.newImageUri
                )
            }
        })

    }

    private fun setStoryProperties(caption: String?, imageLocation: Uri?) {
        if (imageLocation != null) {
            requestManager
                .load(imageLocation)
                .into(imageShare)
        }
        else {
            requestManager
                .load(R.drawable.default_image)
                .into(imageShare)
        }

        story_caption.setText(caption)
    }

    private fun publishNewStory() {
        var multipartBody: MultipartBody.Part? = null
        viewModel.viewState.value?.storyFields?.newImageUri?.let { imageUri ->
            imageUri.path?.let { filePath ->
                val imageFile = File(filePath)
                Log.d(TAG, "ShareStoryFragment, imageFile: file: ${imageFile}")
                val requestBody =
                    RequestBody.create(
                        MediaType.parse("image/*"),
                        imageFile
                    )
                // name = field name in serializer
                // filename = name of the image file
                // requestBody = file with file type information
                multipartBody = MultipartBody.Part.createFormData(
                    "image",
                    imageFile.name,
                    requestBody
                )
            }
        }

        multipartBody?.let {

            viewModel.setStateEvent(
                CreateNewStoryEvent(
                    story_caption.text.toString(),
                    it
                )
            )
            stateChangeListener.hideSoftKeyboard()
        }?: showErrorDialog(ErrorHandling.ERROR_MUST_SELECT_IMAGE)

    }

    private fun showErrorDialog(errorMessage: String) {
        stateChangeListener.onDataStateChange(
            DataState(
                Event(StateError(Response(errorMessage, ResponseType.Dialog()))),
                Loading(isLoading = false),
                Data(Event.dataEvent(null), null)
            )
        )
    }

    override fun onPause() {
        super.onPause()
        viewModel.setNewStoryFields(
            story_caption.text.toString(),
            viewModel.viewState.value?.storyFields?.newImageUri
        )
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.publish_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.publish -> {
                publishNewStory()
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
