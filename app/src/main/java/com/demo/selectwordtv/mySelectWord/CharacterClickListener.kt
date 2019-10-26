package com.demo.selectwordtv.mySelectWord

import android.view.View

interface CharacterClickListener {

    fun onCharacterClick(view: View, character: String)

    fun onClickBlank()

}