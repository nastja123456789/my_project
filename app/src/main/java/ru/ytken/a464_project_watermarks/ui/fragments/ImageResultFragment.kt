package ru.ytken.a464_project_watermarks.ui.fragments

import android.Manifest
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.fragment_image_result.*
import ru.ytken.a464_project_watermarks.R
import ru.ytken.a464_project_watermarks.ui.MainViewModel

class ImageResultFragment: Fragment(R.layout.fragment_image_result) {
    private val vm: MainViewModel by activityViewModels()
    private val TAG = ImageResultFragment::class.simpleName

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.let {
            ActivityCompat.requestPermissions(it,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                20)
        }
        imageButtonClose.setOnClickListener { findNavController().popBackStack() }

        //pathToImage = arguments?.getString(getString(R.string.NAME_BITMAP)).toString()
        //val imageBitmap = BitmapFactory.decodeFile(pathToImage)

        progressBarWaitForImage.visibility = View.VISIBLE
        vm.findTextInBitmap()

        vm.highlightedImage.observe(viewLifecycleOwner) {
            val image = imageViewResultImage.drawable
            Log.d(TAG, "image=$image")
            Log.d(TAG, "Highlighted image changed!")
            progressBarWaitForImage.visibility = View.INVISIBLE
            imageViewResultImage.setImageBitmap(it)
            if (vm.hasText.value == false) {
                Toast.makeText(activity, getString(R.string.text_not_found), Toast.LENGTH_SHORT).show()
            } else {
                buttonSeeSkan.visibility = View.VISIBLE
                buttonSeeSkan.setOnClickListener {
                    findNavController().navigate(R.id.action_imageResultFragment_to_imageCropFragment)
                }
                buttonIsScan.visibility = View.VISIBLE
                buttonIsScan.setOnClickListener {
                    vm.setScanImageToInit()
                    findNavController().navigate(R.id.action_imageResultFragment_to_seeScanFragment)
                }
            }
        }
    }
}