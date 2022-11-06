package ru.ytken.a464_project_watermarks.ocr

import android.util.Log
import kotlin.math.floor

class WatermarkExtractor {

    fun extractWatermark(watermarkedArray: FloatArray, L: Int): Int {
        val tWatermarkedArray = Array (L) { i -> FloatArray (1) { watermarkedArray[i].toFloat() } }
        val p = Array(1) { FloatArray(L) {0.25F} }
        val dotArray = multiplyMatrices(p, tWatermarkedArray, L)

        val qmZero = fQm(dotArray, L, 5, 0)
        val qmOne = fQm(dotArray, L, 5, 1)

        val resZero = dotArray - qmZero
        val resOne = dotArray - qmOne

        val result = when {
            resZero > resOne -> 1
            resZero < resOne -> return 0
            else -> -1
        }
        Log.d("WatermarkExtractor", "resZero = $resZero resOne = $resOne")
        return result
    }

    private fun multiplyMatrices(firstMatrix: Array <FloatArray>,
                                 secondMatrix: Array <FloatArray>,
                                 r1: Int): Float {
        var product = 0f
        for (i in 0 until r1)
            product += firstMatrix[0][i] * secondMatrix[i][0]

        return product
    }

    fun fQm(dotVar: Float, L: Int, delta: Int, m: Int): Float {
        val d = if (m==1) delta/4 else -delta/4
        return floor((dotVar - d)/delta) * delta + d
    }
}