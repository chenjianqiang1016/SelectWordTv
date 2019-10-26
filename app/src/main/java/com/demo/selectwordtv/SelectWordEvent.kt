package com.demo.selectwordtv

/**
 * 选单词相关事件
 */
class SelectWordEvent {

    companion object {

        //选择单词的坐标事件
        val SelectWordLocation = "SelectWordLocation"

        //展示弹框
        val ShowSelectWordView = "ShowSelectWordView"

        //刷新
        val RefreshSelectWordView = "RefreshSelectWordView"

    }

    var flag = ""

    var bean: WordPopDataBean? = null

    var obj: Any? = null

    constructor(flag: String, bean: WordPopDataBean? = null, obj: Any? = null) {
        this.flag = flag
        this.bean = bean
        this.obj = obj
    }

}