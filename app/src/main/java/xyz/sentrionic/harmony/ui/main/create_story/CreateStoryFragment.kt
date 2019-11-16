package xyz.sentrionic.harmony.ui.main.create_story

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.PointF
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.*
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
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
import kotlin.math.round
import kotlin.math.sqrt


class CreateStoryFragment : BaseCreateStoryFragment(), AdapterView.OnItemSelectedListener, View.OnTouchListener {

    private var directories : ArrayList<String> = ArrayList()
    private var selectedImage : String = ""

    // these matrices will be used to move and zoom image
    private val matrix = Matrix()
    private val savedMatrix = Matrix()

    // we can be in one of these 3 states
    private val NONE = 0
    private val DRAG = 1
    private val ZOOM = 2
    private var mode = NONE

    // remember some things for zooming
    private val start = PointF()
    private val mid = PointF()
    private var oldDist = 1f
    private var lastEvent : FloatArray? = null


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
        galleryImageView.setOnTouchListener(this)
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


    private fun setImage(imgURL: String, image: ImageView) {
        requestManager.load(imgURL).into(image)
        galleryImageView.imageMatrix = Matrix().apply {
            val dWidth = image.drawable.intrinsicWidth
            val dHeight = image.drawable.intrinsicHeight

            val vWidth = image.measuredWidth
            val vHeight = image.measuredHeight
            setTranslate(
                round((vWidth - dWidth) * 0.5f),
                round((vHeight - dHeight) * 0.5f)
            )
        }
        matrix.set(galleryImageView.imageMatrix)
    }

    override fun onTouch(v: View, event: MotionEvent): Boolean {
        // handle touch events here

        val view = v as ImageView
        when (event.action and MotionEvent.ACTION_MASK) {

            MotionEvent.ACTION_DOWN -> {
                savedMatrix.set(matrix)
                start.set(event.x, event.y)
                mode = DRAG
                lastEvent = null
            }

            MotionEvent.ACTION_POINTER_DOWN -> {
                oldDist = spacing(event)
                if (oldDist > 10f) {
                    savedMatrix.set(matrix)
                    midPoint(mid, event)
                    mode = ZOOM
                }
                lastEvent = FloatArray(4)
                lastEvent!![0] = event.getX(0)
                lastEvent!![1] = event.getX(1)
                lastEvent!![2] = event.getY(0)
                lastEvent!![3] = event.getY(1)
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP -> {
                mode = NONE
                lastEvent = null
            }

            MotionEvent.ACTION_MOVE -> if (mode == DRAG) {
                matrix.set(savedMatrix)
                val dx = event.x - start.x
                val dy = event.y - start.y
                matrix.postTranslate(dx, dy)
            } else if (mode == ZOOM) {
                val newDist = spacing(event)
                if (newDist > 10f) {
                    matrix.set(savedMatrix)
                    val scale = newDist / oldDist
                    matrix.postScale(scale, scale, mid.x, mid.y)
                }
            }
        }

        view.imageMatrix = matrix

        val canvas = Canvas(Bitmap.createBitmap(view.width, view.height, Bitmap.Config.RGB_565))
        view.draw(canvas)

        return true
    }

        /**
     * Determine the space between the first two fingers
     */
    private fun spacing(event : MotionEvent) : Float {
        val x = event.getX(0) - event.getX(1)
        val y = event.getY(0) - event.getY(1)
        val s=x * x + y * y
        return sqrt(s)
    }

    /**
     * Calculate the mid point of the first two fingers
     */
    private fun midPoint(point : PointF, event : MotionEvent) {
        val x = event.getX(0) + event.getX(1)
        val y = event.getY(0) + event.getY(1)
        point.set(x / 2, y / 2)
    }

}
