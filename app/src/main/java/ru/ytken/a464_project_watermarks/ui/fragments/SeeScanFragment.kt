package ru.ytken.a464_project_watermarks.ui.fragments

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.fragment_scan_result.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ru.ytken.a464_project_watermarks.R
import ru.ytken.a464_project_watermarks.Watermarks
import ru.ytken.a464_project_watermarks.toGrayscale
import ru.ytken.a464_project_watermarks.ui.MainActivity.Companion.pageFileStorage
import ru.ytken.a464_project_watermarks.ui.MainViewModel

class SeeScanFragment: Fragment(R.layout.fragment_scan_result) {
    private val vm: MainViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val fileWithImage = vm.scanImage.value
        if (fileWithImage != null) {
            processImage(fileWithImage.toGrayscale()!!)
        }

        imageButtonNoSkan.setOnClickListener {
            findNavController().navigate(SeeScanFragmentDirections.actionSeeScanFragmentToButtonFragment())
        }

        imageViewCopyToBuffer.setOnClickListener {
            val clipboard: ClipboardManager =
                context?.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("", textViewRecognizedText.text)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(activity, getString(R.string.copyToBuffer), Toast.LENGTH_SHORT).show()
        }
    }

    private fun processImage(fileWithImage: Bitmap) = lifecycleScope.launch(Dispatchers.Main) {
        progressBarWaitForScan.visibility = View.VISIBLE
        textViewProgress.visibility = View.VISIBLE
        textViewProgress.text = getString(R.string.ScanningImage)

        fileWithImage?.let {
            imageViewSkanned.setImageBitmap(vm.initImage.value)
            imageButtonNoSkan.visibility = View.VISIBLE
            val watermarkSize = 24
            var resMatrix = ""
            val lineBounds = vm.lineBounds

            try{
                val lineIntervals = ArrayList<Int>()
                for (i in 1 until lineBounds.size)
                    lineIntervals.add(lineBounds[i]-lineBounds[i-1])
                val watermark = Watermarks.getWatermark(lineIntervals)
                if (watermark != null) {
                    setTextButton(watermark.subSequence(0,watermarkSize).toString())
                    vm.setLetterText(resMatrix)
                } else
                    setTextButton("No watermark")
            } catch (e: java.lang.IndexOutOfBoundsException) {
                Toast.makeText(context, "К сожалению изображение не содержит водяной знак!", Toast.LENGTH_SHORT).show()
            }

            progressBarWaitForScan.visibility = View.INVISIBLE
            textViewProgress.visibility = View.INVISIBLE
        }
    }

    private fun setTextButton(text: String) {
        textViewRecognizedText.visibility = View.VISIBLE
        imageViewCopyToBuffer.visibility = View.VISIBLE
        textViewRecognizedText.text = text
    }

    override fun onDestroy() {
        vm.setImageToNull()
        pageFileStorage.removeAll()
        super.onDestroy()
    }
}