package ru.ytken.a464_project_watermarks.ui.fragments

import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.fragment_image_result.*
import ru.ytken.a464_project_watermarks.R
import ru.ytken.a464_project_watermarks.ui.MainViewModel

class ImageResultFragment: Fragment(R.layout.fragment_image_result) {
    private val vm: MainViewModel by activityViewModels()

    private var selectedImage: Bitmap? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        progressBarWaitForImage.visibility = View.VISIBLE
        imageViewResultImage.visibility = View.INVISIBLE
        //selectedImage = vm.initImage.value
        //imageViewResultImage.setImageBitmap(selectedImage)

        imageButtonClose.setOnClickListener { findNavController().popBackStack() }

        vm.findTextInBitmap()

        vm.highlightedImage.observe(viewLifecycleOwner) {
            //val image = selectedImage
//            Log.d(TAG, "image=$image")
//            Log.d(TAG, "Highlighted image changed!")
            progressBarWaitForImage.visibility = View.INVISIBLE
            imageViewResultImage.setImageBitmap(it)
            imageViewResultImage.visibility = View.VISIBLE
            if (vm.hasText.value == false) {
                Toast.makeText(activity, getString(R.string.text_not_found), Toast.LENGTH_SHORT).show()
            } else {
                buttonSeeSkan.visibility = View.VISIBLE
                buttonSeeSkan.setOnClickListener {
                    vm.setScanImageToInit()
                    findNavController().navigate(R.id.action_imageResultFragment_to_seeScanFragment)
                }
            }
        }
    }
    companion object {
        fun newInstance(): ImageResultFragment {
            Log.d("конструктор","создан конструктор")
            return ImageResultFragment()
        }
    }
}