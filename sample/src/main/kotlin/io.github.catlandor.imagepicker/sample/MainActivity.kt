package io.github.catlandor.imagepicker.sample

import android.app.Activity
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import io.github.catlandor.imagepicker.ImagePicker
import io.github.catlandor.imagepicker.constant.ImageProvider
import io.github.catlandor.imagepicker.sample.databinding.ActivityMainBinding
import io.github.catlandor.imagepicker.sample.databinding.ContentCameraOnlyBinding
import io.github.catlandor.imagepicker.sample.databinding.ContentGalleryOnlyBinding
import io.github.catlandor.imagepicker.sample.databinding.ContentProfileBinding
import io.github.catlandor.imagepicker.sample.util.FileUtil
import io.github.catlandor.imagepicker.sample.util.IntentUtil
import io.github.catlandor.imagepicker.util.IntentUtils

class MainActivity : AppCompatActivity() {

    companion object {
        private const val GITHUB_REPOSITORY = "https://github.com/catlandor/ImagePicker"
    }

    private var cameraUri: Uri? = null
    private var galleryUri: Uri? = null
    private var profileUri: Uri? = null
    private lateinit var binding: ActivityMainBinding
    private lateinit var contentProfileBinding: ContentProfileBinding
    private lateinit var contentCameraOnlyBinding: ContentCameraOnlyBinding
    private lateinit var contentGalleryOnlyBinding: ContentGalleryOnlyBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Note: In order to use the view binding like this, enhance your
        // module gradle settings within the android section with following
        // setting:
        // buildFeatures {
        //    viewBinding true
        // }
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        contentProfileBinding = binding.content.contentProfile
        contentProfileBinding.imgProfile.setDrawableImage(R.drawable.ic_person, true)

        contentProfileBinding.fabAddPhoto.setOnClickListener {
            pickProfileImage()
        }

        contentProfileBinding.imgProfile.setOnClickListener { view ->
            showImage(view)
        }

        contentProfileBinding.imgProfileInfo.setOnClickListener { view ->
            showImageInfo(view)
        }

        contentCameraOnlyBinding = binding.content.contentCameraOnly
        contentCameraOnlyBinding.fabAddCameraPhoto.setOnClickListener {
            pickCameraImage()
        }

        contentCameraOnlyBinding.imgCamera.setOnClickListener { view ->
            showImage(view)
        }

        contentCameraOnlyBinding.imgCameraInfo.setOnClickListener { view ->
            showImageInfo(view)
        }

        contentGalleryOnlyBinding = binding.content.contentGalleryOnly
        contentGalleryOnlyBinding.fabAddGalleryPhoto.setOnClickListener {
            pickGalleryImage()
        }

        contentGalleryOnlyBinding.imgGallery.setOnClickListener { view ->
            showImage(view)
        }

        contentGalleryOnlyBinding.imgGalleryInfo.setOnClickListener { view ->
            showImageInfo(view)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_github -> {
                IntentUtil.openURL(this, GITHUB_REPOSITORY)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private val profileLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                val uri = it.data?.data!!
                profileUri = uri
                contentProfileBinding.imgProfile.setLocalImage(uri, true)
            } else {
                parseError(it)
            }
        }
    private val galleryLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                val imgGalleryView = contentGalleryOnlyBinding.imgGallery
                if (it.data?.hasExtra(ImagePicker.EXTRA_FILE_PATH)!!) {
                    val uri = it.data?.data!!
                    galleryUri = uri
                    imgGalleryView.setLocalImage(uri)
                } else if (it.data?.hasExtra(ImagePicker.MULTIPLE_FILES_PATH)!!) {
                    val files = ImagePicker.getAllFile(it.data) as ArrayList<Uri>
                    if (files.size > 0) {
                        val uri = files[0] // first image
                        galleryUri = uri
                        imgGalleryView.setLocalImage(uri)
                    }
                } else {
                    parseError(it)
                }
            } else {
                parseError(it)
            }
        }

    private val cameraLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                val uri = it.data?.data!!
                cameraUri = uri
                contentCameraOnlyBinding.imgCamera.setLocalImage(uri, false)
            } else {
                parseError(it)
            }
        }

    private fun parseError(activityResult: ActivityResult) {
        if (activityResult.resultCode == ImagePicker.RESULT_ERROR) {
            Toast.makeText(this, ImagePicker.getError(activityResult.data), Toast.LENGTH_SHORT)
                .show()
        } else {
            Toast.makeText(this, "Task Cancelled", Toast.LENGTH_SHORT).show()
        }
    }

    private fun pickProfileImage() {
        ImagePicker.with(this)
            .crop()
            .cropOval()
            .maxResultSize(512, 512, true)
            .provider(ImageProvider.BOTH) // Or bothCameraGallery()
            .setDismissListener {
                Log.d("ImagePicker", "onDismiss")
            }
            .createIntentFromDialog { profileLauncher.launch(it) }
    }

    private fun pickGalleryImage() {
        galleryLauncher.launch(
            ImagePicker.with(this)
                .crop()
                .galleryOnly()
                .setMultipleAllowed(true)
//                .setOutputFormat(Bitmap.CompressFormat.WEBP)
                .cropFreeStyle()
                .galleryMimeTypes( // no gif images at all
                    mimeTypes = arrayOf(
                        "image/png",
                        "image/jpg",
                        "image/jpeg"
                    )
                )
                .createIntent()
        )
    }

    private fun pickCameraImage() {
        cameraLauncher.launch(
            ImagePicker.with(this)
                .crop()
                .cameraOnly()
                .maxResultSize(1080, 1920, true)
                .createIntent()
        )
    }

    private fun showImage(view: View) {
        val uri = when (view) {
            contentProfileBinding.imgProfile -> profileUri
            contentCameraOnlyBinding.imgCamera -> cameraUri
            contentGalleryOnlyBinding.imgGallery -> galleryUri
            else -> null
        }

        uri?.let {
            startActivity(IntentUtils.getUriViewIntent(this, uri))
        }
    }

    private fun showImageInfo(view: View) {
        val uri = when (view) {
            contentProfileBinding.imgProfileInfo -> profileUri
            contentCameraOnlyBinding.imgCameraInfo -> cameraUri
            contentGalleryOnlyBinding.imgGalleryInfo -> galleryUri
            else -> null
        }

        AlertDialog.Builder(this)
            .setTitle("Image Info")
            .setMessage(FileUtil.getFileInfo(this, uri))
            .setPositiveButton("Ok", null)
            .show()
    }
}
