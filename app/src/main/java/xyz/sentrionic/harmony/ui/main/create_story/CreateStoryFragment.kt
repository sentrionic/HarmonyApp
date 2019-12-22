package xyz.sentrionic.harmony.ui.main.create_story

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.*
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.github.chrisbanes.photoview.PhotoView
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import kotlinx.android.synthetic.main.fragment_gallery.*
import xyz.sentrionic.harmony.R
import xyz.sentrionic.harmony.ui.*
import xyz.sentrionic.harmony.util.Constants.Companion.CAMERA_REQUEST_CODE
import xyz.sentrionic.harmony.util.ErrorHandling.Companion.ERROR_SOMETHING_WRONG_WITH_IMAGE
import xyz.sentrionic.harmony.util.FilePaths
import xyz.sentrionic.harmony.util.FileSearch
import xyz.sentrionic.harmony.util.SuccessHandling.Companion.SUCCESS_STORY_CREATED
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.*
import kotlin.collections.ArrayList


class CreateStoryFragment : BaseCreateStoryFragment(), AdapterView.OnItemSelectedListener {

    private var directories : ArrayList<String> = ArrayList()
    private var selectedImage : String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_gallery, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
        (activity as AppCompatActivity).supportActionBar?.setDisplayShowTitleEnabled(false)
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
                            }
                        }
                    }
                }
            }
        })

    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            Log.d(TAG, "CROP: RESULT OK")
            when (requestCode) {

                CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE -> {
                    Log.d(TAG, "CROP: CROP_IMAGE_ACTIVITY_REQUEST_CODE")
                    val result = CropImage.getActivityResult(data)
                    val resultUri = result.uri
                    Log.d(TAG, "CROP: CROP_IMAGE_ACTIVITY_REQUEST_CODE: uri: ${resultUri}")
                    viewModel.setNewStoryFields(
                        caption = null,
                        uri = resultUri
                    )
                    findNavController().navigate(R.id.action_createStoryFragment_to_shareStoryFragment)
                }

                CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE -> {
                    Log.d(TAG, "CROP: ERROR")
                    showErrorDialog(ERROR_SOMETHING_WRONG_WITH_IMAGE)
                }

                CAMERA_REQUEST_CODE -> {
                    val bitmap : Bitmap = data?.extras?.get("data") as Bitmap

                    val bytes = ByteArrayOutputStream()
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, bytes)
                    val temp = MediaStore.Images.Media.insertImage(context?.contentResolver, bitmap, "Title", null)

                    var path = ""

                    if (context?.contentResolver != null) {
                        val cursor = context?.contentResolver?.query(Uri.parse(temp), null, null, null, null)
                        if (cursor != null) {
                            cursor.moveToFirst()
                            val idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA)
                            path = cursor.getString(idx)
                            cursor.close()
                        }
                    }

                    viewModel.setNewStoryFields(
                        caption = null,
                        uri = Uri.fromFile(File(path))
                    )
                    findNavController().navigate(R.id.action_createStoryFragment_to_shareStoryFragment)
                }
            }
        }
    }

    private fun launchImageCrop(uri: Uri) {
        context?.let{
            CropImage.activity(uri)
                .setGuidelines(CropImageView.Guidelines.ON)
                .start(it, this)
        }
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

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.gallery_share_menu, menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)

        if (stateChangeListener.isStoragePermissionGranted()) {
            setupMenu(menu)
        }
    }

    private fun setupMenu(menu: Menu) {
        val toolbar : MenuItem = menu.findItem(R.id.gallery_spinner)
        val rootView : View = toolbar.actionView

        val filePaths = FilePaths()

        //check for other folders indide "/storage/emulated/0/pictures"
        directories = FileSearch.getDirectoryPaths(filePaths.PICTURES)
        directories.add(filePaths.CAMERA)
        directories.add(filePaths.DOWNLOADS)
        directories.add(filePaths.PICTURES)

        val directoryNames : ArrayList<String> = ArrayList()

        for (i in 0 until directories.size) {
            val index = directories[i].lastIndexOf("/")
            val string = directories[i].substring(index + 1)
            directoryNames.add(string.capitalize())
        }

        directories.add(filePaths.CAMERA)

        val spinner: Spinner = rootView.findViewById(R.id.spinnerDirectory) as Spinner
        val adapter : ArrayAdapter<String> = ArrayAdapter(context!!, android.R.layout.simple_spinner_item, directoryNames)
        adapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item)
        spinner.adapter = adapter

        spinner.onItemSelectedListener = this
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.gallery_next -> {
                launchImageCrop(Uri.fromFile(File(selectedImage)))
            }

            R.id.gallery_close -> {
                findNavController().popBackStack()
            }

            R.id.gallery_camera -> {
                val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {}

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        setupGridView(directories[position])
    }

    private fun setupGridView(selectedDirectory: String) {
        val imgURLs = FileSearch.getFilePaths(selectedDirectory)

        //set the grid column width
        val gridWidth = resources.displayMetrics.widthPixels
        val imageWidth = gridWidth / 4
        gridView.columnWidth = imageWidth

        Collections.sort(imgURLs, Collections.reverseOrder())

        //use the grid adapter to adapter the images to gridview
        val adapter = GridImageAdapter(this.context!!, R.layout.layout_grid_imageview, imgURLs, requestManager)
        gridView.adapter = adapter

        //set the first image to be displayed when the activity fragment view is inflated
        try {
            setImage(imgURLs[0], galleryImageView)
            selectedImage = imgURLs[0]
        } catch (e: IndexOutOfBoundsException) {
            Log.e("GalleryFragment", e.message.toString())
        }

        gridView.onItemClickListener =
            AdapterView.OnItemClickListener { parent, view, position, id ->
                setImage(imgURLs[position], galleryImageView)
                selectedImage = imgURLs[position]
            }

    }

    private fun setImage(imgURL: String, image: PhotoView) {
        requestManager.load(imgURL).into(image)
    }

}
