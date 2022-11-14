package ru.ytken.a464_project_watermarks

import kotlin.math.sqrt

object Watermarks {
    fun getWatermark(lineBounds: ArrayList<Int>): String? {
        val meanInterval = lineBounds.mean()
        val stdIntervals = lineBounds.std()
        if (stdIntervals < 0.4) return null
        val maxInterval = lineBounds.maxOrNull() ?: 0
        var watermark = ""
        for (i in lineBounds)
            if (i > meanInterval + stdIntervals*0.7)
                watermark += "1"
            else
                watermark += 0
        return watermark
    }

    private fun ArrayList<Int>.mean(): Float = this.sum().toFloat() / this.size

    private fun ArrayList<Int>.std(): Float {
        val mean = this.mean()
        var sqSum = 0f
        for (i in this) sqSum += (i - mean)*(i - mean)
        sqSum /= this.size
        return sqrt(sqSum)
    }
}