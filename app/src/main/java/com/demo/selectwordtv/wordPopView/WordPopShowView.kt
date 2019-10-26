package com.demo.selectwordtv.wordPopView

import android.content.Context
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.demo.selectwordtv.R
import com.demo.selectwordtv.SelectWordEvent
import com.demo.selectwordtv.UiUtils
import com.demo.selectwordtv.WordPopDataBean
import org.greenrobot.eventbus.EventBus
import kotlin.properties.Delegates


/**
 * 点击句子中某个单词，出现单词释义等的popView
 */
class WordPopShowView {

    private var context: Context by Delegates.notNull()

    //用于定位的view
    private var locationView: View by Delegates.notNull()

    private var popupWindow: PopupWindow by Delegates.notNull()

    //popupWindow中，加载的布局
    private var popupWindowView: View by Delegates.notNull()

    private var myTriangleUpView: MyTriangleUpView? = null

    private var myTriangleDownView: MyTriangleDownView? = null

    private var wordPopContentView: LinearLayout? = null

    private var wordPopContent: LinearLayout? = null

    private var wordTv: TextView? = null
    private var wordMeanTv: TextView? = null
    private var wordMore: TextView? = null

    private var pop_root: RelativeLayout? = null

    constructor(context: Context, view: View, bean: WordPopDataBean) {

        this.context = context

        locationView = view

        initPopupWindow(bean)

    }

    private fun initPopupWindow(bean: WordPopDataBean) {

        popupWindowView = LayoutInflater.from(context).inflate(R.layout.word_popup_view_layout, null)

        popupWindowView.setFocusable(true)

        popupWindow =
            PopupWindow(popupWindowView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, true)

        myTriangleUpView = popupWindowView.findViewById(R.id.wordPopTopView)
        myTriangleDownView = popupWindowView.findViewById(R.id.wordPopBottomView)
        wordPopContentView = popupWindowView.findViewById(R.id.wordPopContentView)
        wordPopContent = popupWindowView.findViewById(R.id.wordPopContent)

        wordTv = popupWindowView.findViewById(R.id.wordTv)
        wordMeanTv = popupWindowView.findViewById(R.id.wordMeanTv)
        wordMore = popupWindowView.findViewById(R.id.wordMore)

        wordTv?.text = bean.wordContent

        wordMeanTv?.text = bean.wordMean

        wordMore?.setOnClickListener {
            Toast.makeText(context, "查看更多", Toast.LENGTH_SHORT).show()
            popDimss()
        }

        pop_root = popupWindowView.findViewById(R.id.pop_root)

        pop_root?.setOnClickListener {
            popDimss()
        }

        popupWindow.setOutsideTouchable(true);
        popupWindow.setFocusable(false);

        popupWindow.animationStyle = R.style.WordPopShowAnim

        //状态栏高度
        var statuBarHeight = UiUtils.getStatusBarHeight(context)

        //屏幕宽度
        val screenWidth = UiUtils.getScreenWidth(context)

        //屏幕高度
        val screenHeight = UiUtils.getScreenHeight(context)

        //标题高度
        var titleHeight = UiUtils.dp2px(context, 44f)

        var location = bean.location

        var locations = location.split("-")

        //弹框后，三角形的尖角，所在 x 轴位置
        var xLocation: Int = locations[0].toInt()

        //点击的单词，距离屏幕顶部的距离（含状态栏）
        var yLocation: Int = locations[1].toInt()

        //点击的单词，在 TextView 中，单词顶部 距离控件顶部的距离
        var topValue: Int = locations[2].toInt()

        //点击的单词，在 TextView 中，单词底部 距离控件部的距离
        var bottomValue: Int = locations[3].toInt()

        var popHeight_base = 0

        //设置完内容后，通知系统，去测量一下，后面就能拿到
        wordPopContent?.measure(0, 0)

        popHeight_base = wordPopContent!!.measuredHeight

        Log.e("popHeight_base ", "$popHeight_base")

        //三角形高度
        var triangle_height = UiUtils.dp2px(context, 8f)

        //三角形宽度
        var triangle_width = UiUtils.dp2px(context, 16f)

        //完整的弹框，真实高度
        var realHeight = popHeight_base + triangle_height

        //文字上方预计高度
        var expectTopHeight = yLocation - statuBarHeight - titleHeight

        //文字下方预计高度
        var expectBottomHeight = screenHeight - (yLocation - statuBarHeight + (bottomValue - topValue))

        if (expectTopHeight >= realHeight) {
            //文字上面的空间可以容纳弹框

            myTriangleUpView?.visibility = View.GONE
            myTriangleDownView?.visibility = View.VISIBLE

            var lp: LinearLayout.LayoutParams = myTriangleDownView?.layoutParams as LinearLayout.LayoutParams

            var resultMargin = xLocation - triangle_width / 2

            if (xLocation - triangle_width / 2 < UiUtils.dp2px(context, 20f)) {

                resultMargin = UiUtils.dp2px(context, 20f)

            } else if (xLocation + triangle_width / 2 > screenWidth - UiUtils.dp2px(context, 20f)) {

                resultMargin = screenWidth - UiUtils.dp2px(context, 20f) - triangle_width

            }

            lp.marginStart = resultMargin

            myTriangleDownView?.layoutParams = lp

            var contentLp: RelativeLayout.LayoutParams = wordPopContentView?.layoutParams as RelativeLayout.LayoutParams

            contentLp.topMargin = yLocation - statuBarHeight - realHeight

            wordPopContentView?.layoutParams = contentLp

            EventBus.getDefault().post(SelectWordEvent(SelectWordEvent.ShowSelectWordView))

        } else if (expectBottomHeight > realHeight) {
            //文字上面的空间不足以容纳弹框，文字下面的空间可以

            myTriangleUpView?.visibility = View.VISIBLE
            myTriangleDownView?.visibility = View.GONE


            var lp: LinearLayout.LayoutParams = myTriangleUpView?.layoutParams as LinearLayout.LayoutParams

            var resultMargin = xLocation - triangle_width / 2

            if (xLocation - triangle_width / 2 < UiUtils.dp2px(context, 20f)) {

                resultMargin = UiUtils.dp2px(context, 20f)

            } else if (xLocation + triangle_width / 2 > screenWidth - UiUtils.dp2px(context, 20f)) {

                resultMargin = screenWidth - UiUtils.dp2px(context, 20f) - triangle_width

            }

            lp.marginStart = resultMargin

            myTriangleUpView?.layoutParams = lp

            var contentLp: RelativeLayout.LayoutParams = wordPopContentView?.layoutParams as RelativeLayout.LayoutParams

            contentLp.topMargin = yLocation - statuBarHeight + (bottomValue - topValue)

            wordPopContentView?.layoutParams = contentLp

            EventBus.getDefault().post(SelectWordEvent(SelectWordEvent.ShowSelectWordView))

        } else {

            /**
             * 极限情况，文字上下的空间，都容纳不下弹框
             *
             * 不作处理
             */

        }

    }

    fun showView() {
        popupWindow.showAtLocation(
            locationView,
            Gravity.CENTER,
            0,
            0
        )
    }


    //取消
    fun popDimss() {

        EventBus.getDefault().post(SelectWordEvent(SelectWordEvent.RefreshSelectWordView))
        if (popupWindow != null) {
            popupWindow.dismiss()
        }
    }

    public fun wordPopIsShow(): Boolean {
        return popupWindow != null && popupWindow.isShowing
    }


}