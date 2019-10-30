package com.demo.selectwordtv

/**
 * 选单词相关事件
 */
class SelectWordEvent {

    companion object {

        //搜索选中单词
        val SearchSelectWord = "SearchSelectWord"


        val QueryStart = "QueryStart"
        val QueryFail = "QueryFail"
        val QuerySuccess = "QuerySuccess"

        //展示弹框
        val ShowSelectWordView = "ShowSelectWordView"

        //刷新
        val RefreshSelectWordView = "RefreshSelectWordView"

    }

    var flag = ""

    var bean: WordPopDataBean? = null

    constructor(flag: String, bean: WordPopDataBean? = null) {
        this.flag = flag
        this.bean = bean
    }

}