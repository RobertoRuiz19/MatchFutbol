package com.example.futbolmatch.ui.resultados

import android.text.InputFilter
import android.text.Spanned

class InputFilterMinMax(private val min: String, private val max: String) : InputFilter {
    override fun filter(
        source: CharSequence?,
        start: Int,
        end: Int,
        dest: Spanned?,
        dstart: Int,
        dend: Int
    ): CharSequence? {
        try {
            val input = (dest?.subSequence(0, dstart).toString() + source?.subSequence(
                start,
                end
            ).toString() + dest?.subSequence(dend, dest.length)).toInt()
            if (isInRange(input))
                return null
        } catch (e: NumberFormatException) {
            e.printStackTrace()
        }
        return ""
    }

    private fun isInRange(input: Int): Boolean {
        return input >= min.toInt() && input <= max.toInt()
    }
}