package com.demo.selectwordtv


class PARAM {

    companion object {

        val WordPopLocation: String = "WordPopLocation"

        //单词正在查询中
        val WordQueryRuning = 1

        //单词查询成功
        val WordQuerySuccess = WordQueryRuning + 1

        //单词查询失败
        val WordQueryFail = WordQuerySuccess + 1


    }

}