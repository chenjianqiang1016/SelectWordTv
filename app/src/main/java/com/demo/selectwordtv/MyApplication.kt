package com.demo.selectwordtv

import android.app.Application
import kotlin.properties.Delegates

class MyApplication:Application() {

    companion object {

        var instance: MyApplication by Delegates.notNull()

        fun getMyInstance() = instance
    }

    override fun onCreate() {
        super.onCreate()

        instance = this
    }


}