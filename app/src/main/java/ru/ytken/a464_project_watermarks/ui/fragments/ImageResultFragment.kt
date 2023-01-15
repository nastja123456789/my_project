package ru.ytken.a464_project_watermarks.ui.fragments

import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.*
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.fragment_image_result.*
import ru.ytken.a464_project_watermarks.R
import ru.ytken.a464_project_watermarks.ui.viewmodel.ImageResultFragmentViewModel
import ru.ytken.a464_project_watermarks.domain.SavedImageFactory
import java.io.ByteArrayOutputStream
import java.util.*

class ImageResultFragment: Fragment(R.layout.fragment_image_result) {
    private val vm: ImageResultFragmentViewModel by viewModels {
        SavedImageFactory()
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setFragmentResultListener("fromButtonToImage") {
            _, bun ->
            val str = bun.getString("uri")
            val uri = Uri.parse(
                str
            )
            val bitmap = MediaStore.Images.Media.getBitmap(requireActivity().contentResolver, uri)
            vm.findTextInBitmap(bitmap)
        }
        setFragmentResultListener("fromCropToImage") {
                _, bun ->
            val str = bun.getString("uri")
            val uri = Uri.parse(
                str
            )
            val bitmap = MediaStore.Images.Media.getBitmap(requireActivity().contentResolver, uri)
            vm.findTextInBitmap(bitmap)
            bitmap.recycle()
        }
        buttonSeeSkan.setOnClickListener {
            vm.setScanImageToInit()
            val bytes = ByteArrayOutputStream()
            vm.scanImage.value!!.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
            val path: String = MediaStore.Images.Media.insertImage(
                requireActivity().contentResolver,
                vm.scanImage.value,
                "IMG_" + Calendar.getInstance().time,
                null
            )
            val uri = Uri.parse(path)
            setFragmentResult(
                "fromImageToSeeScan",
                bundleOf("uri" to uri.toString())
            )
            val arr = vm.lineBounds
            setFragmentResult(
                "arrayList",
                bundleOf( "array" to arr.toString())
            )

            findNavController().navigate(R.id.action_imageResultFragment_to_seeScanFragment)
        }
        progressBarWaitForImage.visibility = View.VISIBLE
        imageViewResultImage.visibility = View.INVISIBLE
        vm.highlightedImage.observe(viewLifecycleOwner) {
            imageViewResultImage.setImageBitmap(it)
            imageViewResultImage.visibility = View.VISIBLE
            progressBarWaitForImage.visibility = View.INVISIBLE
            if (vm.hasText.value == false) {
                Toast.makeText(activity, getString(R.string.text_not_found), Toast.LENGTH_SHORT).show()
                imageButtonClose.visibility = View.VISIBLE
            } else {
                buttonSeeSkan.visibility = View.VISIBLE
            }
        }
        imageButtonClose.setOnClickListener {
            findNavController().popBackStack()
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        imageViewResultImage.setImageBitmap(null)
    }
}