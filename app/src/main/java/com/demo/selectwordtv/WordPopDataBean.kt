package com.demo.selectwordtv

import com.demo.selectwordtv.mySelectWord.SelectWordView


data class WordPopDataBean(
    var location:String,
    var wordContent: String,
    var selectWordView: SelectWordView,//选中单词所在的TextView
    var queryStatus: Int = PARAM.WordQueryRuning,//查询的状态。用于标记，是查询中，还是查询成功、失败
    var queryTipContent: String = ""//提示信息，和查询状态结合使用
)