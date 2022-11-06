package ru.ytken.a464_project_watermarks.ui.fragments

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.fragment_button.*
import ru.ytken.a464_project_watermarks.R
import ru.ytken.a464_project_watermarks.SaveImageUtil
import ru.ytken.a464_project_watermarks.ui.MainViewModel
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class ButtonFragment: Fragment(R.layout.fragment_button) {

    private val REQUEST_IMAGE_CAPTURE = 1
    private val PICKFILE_RESULT_CODE = 2
    private val TAG = ButtonFragment::class.simpleName

    private val vm: MainViewModel by activityViewModels()

    private var currentPhotoPath: String = Environment.DIRECTORY_PICTURES
    lateinit var photoName: String
    lateinit var imageFile: File

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        buttonTakePhoto.setOnClickListener {
            launchCamera()
        }

        buttonChoosePhotoFromStorage.setOnClickListener {
            getPictureFromExternal()
        }


    }


    private fun getPictureFromExternal() {
        if (context?.let {
                ContextCompat.checkSelfPermission(it, Manifest.permission.READ_EXTERNAL_STORAGE) } ==
            PackageManager.PERMISSION_GRANTED) {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, PICKFILE_RESULT_CODE)
        } else
            Toast.makeText(activity, getString(R.string.permissionRead), Toast.LENGTH_SHORT).show()
    }

    private fun launchCamera() {
        if (context?.let { ContextCompat.checkSelfPermission(it, Manifest.permission.CAMERA) } ==
            PackageManager.PERMISSION_GRANTED) {
            Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
                // Ensure that there's a camera activity to handle the intent
                activity?.packageManager?.let {
                    takePictureIntent.resolveActivity(it)?.also {
                        // Create the File where the photo should go
                        imageFile = createImageFile()
                        photoName = imageFile.name
                        // Continue only if the File was successfully created
                        imageFile.also {
                            val photoURI: Uri? = context?.let { it1 ->
                                FileProvider.getUriForFile(
                                    it1,
                                    "com.example.android.fileprovider",
                                    it
                                )
                            }
                            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
                        }
                    }
                }
            }
        } else
            Toast.makeText(context, getString(R.string.CameraPermission), Toast.LENGTH_SHORT).show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == AppCompatActivity.RESULT_OK) {
            when (requestCode) {
                REQUEST_IMAGE_CAPTURE -> {
                    galleryAddPic()
                    vm.setInitImage(path = currentPhotoPath)
                    findNavController().navigate(R.id.action_mainFragment_to_imageResultFragment)
                }
                PICKFILE_RESULT_CODE -> {
                    val imageStream = data?.data?.let {
                        requireActivity().contentResolver.openInputStream(it)
                    }
                    if (imageStream != null) {
                        vm.setInitImage(instr = imageStream)
                        findNavController().navigate(R.id.action_mainFragment_to_imageResultFragment)
                    }
                }
            }
        }
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File? = activity?.getExternalFilesDir(currentPhotoPath)
        return File.createTempFile(
            "JPEG_${timeStamp}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
            currentPhotoPath = absolutePath
        }
    }

    private fun galleryAddPic() {
        if (context?.let { ContextCompat.checkSelfPermission(it, Manifest.permission.WRITE_EXTERNAL_STORAGE) } ==
            PackageManager.PERMISSION_GRANTED) {
            var imageBitmap = BitmapFactory.decodeFile(currentPhotoPath)
            val savedToExternal =
                context?.let {
                    SaveImageUtil.savePhotoToExternalStorage(
                        it,
                        photoName,
                        imageBitmap
                    )
                }
            if (savedToExternal == false)
                Toast.makeText(activity, getString(R.string.PhotoNotSaved), Toast.LENGTH_SHORT)
                    .show()
        }
    }
}