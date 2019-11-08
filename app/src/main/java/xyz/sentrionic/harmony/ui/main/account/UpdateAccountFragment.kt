package xyz.sentrionic.harmony.ui.main.account

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.*
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import kotlinx.android.synthetic.main.fragment_update_account.*
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import xyz.sentrionic.harmony.R
import xyz.sentrionic.harmony.models.AccountProperties
import xyz.sentrionic.harmony.ui.*
import xyz.sentrionic.harmony.ui.main.account.state.AccountStateEvent
import xyz.sentrionic.harmony.ui.main.account.state.AccountViewState
import xyz.sentrionic.harmony.util.Constants.Companion.GALLERY_REQUEST_CODE
import java.io.File

class UpdateAccountFragment : BaseAccountFragment() {


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_update_account, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
        subscribeObservers()

        profile_photo.setOnClickListener {
            if (stateChangeListener.isStoragePermissionGranted()) {
                pickImageFromGallery()
            }
        }

        changeProfilePhoto.setOnClickListener {
            if (stateChangeListener.isStoragePermissionGranted()) {
                pickImageFromGallery()
            }
        }
    }

    private fun subscribeObservers() {
        viewModel.dataState.observe(viewLifecycleOwner, Observer { dataState ->
            stateChangeListener.onDataStateChange(dataState)
            Log.d(TAG, "UpdateAccountFragment, DataState: ${dataState}")
        })

        viewModel.viewState.observe(viewLifecycleOwner, Observer { viewState ->
            if (viewState != null) {
                viewState.accountProperties?.let {
                    Log.d(TAG, "UpdateAccountFragment, ViewState: ${it}")
                    setAccountDataFields(it)
                }
                viewState.newProfileField?.let {
                    setProfilePicture(it)
                }
            }
        })
    }

    private fun setAccountDataFields(accountProperties: AccountProperties) {
        if (input_email.text.isNullOrBlank()) {
            input_email.setText(accountProperties.email)
        }
        if (input_username.text.isNullOrBlank()) {
            input_username.setText(accountProperties.username)
        }
        if (input_description.text.isNullOrBlank()) {
            input_description.setText(accountProperties.description)
        }
        if (input_website.text.isNullOrBlank()) {
            input_website.setText(accountProperties.website)
        }
        if (input_display_name.text.isNullOrBlank()) {
            input_display_name.setText(accountProperties.display_name)
        }

        view?.let {
            Glide.with(it.context)
                .load(accountProperties.image)
                .into(profile_photo)
        }
    }

    private fun saveChanges() {

        var multipartBody: MultipartBody.Part? = null
        viewModel.getUpdatedProfilePictureUri()?.let{ imageUri ->
            imageUri.path?.let{filePath ->
                val imageFile = File(filePath)
                Log.d(TAG, "UpdateAccountFragment, imageFile: file: ${imageFile}")
                if (imageFile.exists()) {
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
        }

        viewModel.setStateEvent(
            AccountStateEvent.UpdateAccountPropertiesEvent(
                input_email.text.toString(),
                input_username.text.toString(),
                input_description.text.toString(),
                input_website.text.toString(),
                input_display_name.text.toString(),
                multipartBody
            )
        )

        stateChangeListener.hideSoftKeyboard()
    }

    private fun pickImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        val mimeTypes = arrayOf("image/jpeg", "image/png", "image/jpg")
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
        intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        startActivityForResult(intent, GALLERY_REQUEST_CODE)
    }

    private fun launchImageCrop(uri: Uri){
        context?.let {
            CropImage.activity(uri)
                .setGuidelines(CropImageView.Guidelines.ON)
                .start(it, this)
        }
    }

    private fun showImageSelectionError(){
        stateChangeListener.onDataStateChange(
            DataState(
                Event(
                    StateError(
                    Response(
                        "Something went wrong with the image.",
                        ResponseType.Dialog()
                    )
                )
                ),
                Loading(isLoading = false),
                Data(Event.dataEvent(null), null)
            )
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {

                GALLERY_REQUEST_CODE -> {
                    data?.data?.let { uri ->
                        launchImageCrop(uri)
                    } ?: showImageSelectionError()
                }

                CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE -> {
                    Log.d(TAG, "CROP: CROP_IMAGE_ACTIVITY_REQUEST_CODE")
                    val result = CropImage.getActivityResult(data)
                    val resultUri = result.uri
                    Log.d(TAG, "CROP: CROP_IMAGE_ACTIVITY_REQUEST_CODE: uri: ${resultUri}")
                    profile_photo.setImageURI(resultUri)
                    viewModel.setNewProfilePicture(uri = resultUri)
                }

                CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE -> {
                    Log.d(TAG, "CROP: ERROR")
                    showImageSelectionError()
                }
            }
        }
    }

    private fun setProfilePicture(newProfileField: AccountViewState.NewProfileField) {
        view?.let {
            Glide.with(it.context)
                .load(newProfileField.newImageUri)
                .into(profile_photo)
        }
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.update_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.save -> {
                saveChanges()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}