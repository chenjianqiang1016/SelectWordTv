package com.demo.selectwordtv.mySelectWord

import android.content.res.Resources
import android.text.TextPaint
import android.text.style.ClickableSpan
import android.view.View


class CharacterSpan : ClickableSpan {

    private val TAG = "CharacterSpan"
    private val textColor: Int


    private val text: String
    private val resources: Resources? = null
    private val characterClickListener: CharacterClickListener? = null

    constructor(text: String, currentTextColor: Int) {
        this.text = text
        this.textColor = currentTextColor
    }

    override fun updateDrawState(ds: TextPaint) {
        super.updateDrawState(ds)
        ds.color = textColor
        ds.isUnderlineText = false
    }

    fun getText(): String {
        return text
    }

    override fun onClick(widget: View) {}


}