package com.demo.selectwordtv.mySelectWord

import java.io.Serializable


class BaseRes<T> : Serializable {

    private var status_code: Int = 0
    private var msg: String? = null

    private var data: T? = null

    fun getStatus_code(): Int {
        return status_code
    }

    fun setStatus_code(status_code: Int) {
        this.status_code = status_code
    }

    fun getMsg(): String? {
        return msg
    }

    fun setMsg(msg: String) {
        this.msg = msg
    }

    fun getData(): T? {
        return data
    }

    fun setData(data: T) {
        this.data = data
    }

}