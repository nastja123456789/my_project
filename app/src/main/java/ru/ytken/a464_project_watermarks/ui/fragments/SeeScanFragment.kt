package ru.ytken.a464_project_watermarks.ui.fragments

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.RectShape
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
import ru.ytken.a464_project_watermarks.ocr.TessOCR
import ru.ytken.a464_project_watermarks.ocr.WatermarkExtractor
import ru.ytken.a464_project_watermarks.ui.MainViewModel
import ru.ytken.a464_project_watermarks.toGrayscale


class SeeScanFragment: Fragment(R.layout.fragment_scan_result) {
    private val vm: MainViewModel by activityViewModels()
    val TAG = SeeScanFragment::class.simpleName

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        imageButtonNoSkan.setOnClickListener {
            //activity?.supportFragmentManager?.popBackStack()
            findNavController().navigate(SeeScanFragmentDirections.actionSeeScanFragmentToButtonFragment())
        }
        imageButtonNoSkan.visibility = View.INVISIBLE

        val fileWithImage = vm.scanImage.value
        Log.d(TAG, "fileWithImage=$fileWithImage")
        context?.let { TessOCR.initDirs(it) }
        TessOCR.initLang("eng")
        TessOCR.initLang("rus")
        if (fileWithImage != null) {
            processImage(fileWithImage.toGrayscale()!!)
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
            imageViewSkanned.setImageBitmap(it)
            TessOCR.setTessImage(it)

            textViewProgress.text = getString(R.string.ExtractingText)
            val imageText = vm.getTextFromImage().await()

            textViewProgress.text = getString(R.string.SearchCharacters)
            val letterBoxes  = vm.getLettersBounds().await()

            val mutableBitmap = it.copy(Bitmap.Config.ARGB_8888, true)
            val canvas = Canvas(mutableBitmap)
            var shapeDrawable = ShapeDrawable(RectShape())
            shapeDrawable.paint.style = Paint.Style.STROKE
            shapeDrawable.paint.strokeWidth = 0.6F
            shapeDrawable.paint.color = resources.getColor(R.color.primary_blue)

            val xCoords = FloatArray(letterBoxes.size)
            var norm = 0
            for ((c, charCoord) in letterBoxes.withIndex()) {
                shapeDrawable.bounds = charCoord
                shapeDrawable.draw(canvas)
                if (c==0) {
                    norm = charCoord.left
                    xCoords[c] = 0f
                }
                xCoords[c] = (charCoord.left - norm).toFloat()
                //xCoords[c] = charCoord.left
            }

            vm.setLetterImage(mutableBitmap)
            imageViewSkanned.setImageBitmap(mutableBitmap)
            imageButtonNoSkan.visibility = View.VISIBLE

            val extractor = WatermarkExtractor()
            val watermarkSize = 24
            val L = (xCoords.size / watermarkSize).toInt()
            var resMatrix = "L = $L = ${xCoords.size} / $watermarkSize\n"

            for (i in 0 until watermarkSize)
                if (L*(i+1) < xCoords.size)
                    resMatrix += extractor.extractWatermark(xCoords.slice(L*i..L*(i+1)).toFloatArray(), L).toString()
                else {
                    Toast.makeText(activity, "L слишком большой! Удалось извлечь $i бит сообщения", Toast.LENGTH_SHORT).show()
                    break
                }
            resMatrix += xCoords.joinToString(separator = "\n", prefix = "\n\n")

            setTextButton(resMatrix)
            vm.setLetterText(resMatrix)
            progressBarWaitForScan.visibility = View.INVISIBLE
            textViewProgress.visibility = View.INVISIBLE
        }
    }

    private fun setTextButton(text: String) {
        textViewRecognizedText.visibility = View.VISIBLE
        imageViewCopyToBuffer.visibility = View.VISIBLE
        textViewRecognizedText.text = text
    }

}