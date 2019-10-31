package com.demo.selectwordtv.wordPopView

import android.content.Context
import android.graphics.Paint
import android.text.TextPaint
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.demo.selectwordtv.*
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
    private var wordPopTip: TextView? = null

    private var pop_root: RelativeLayout? = null

    private var wordPopDataBean: WordPopDataBean? = null
    private var queryResultBean: QueryResultBean? = null

    constructor(context: Context, bean: WordPopDataBean) {

        this.context = context

        wordPopDataBean = bean

        locationView = bean.selectWordView

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
        wordPopTip = popupWindowView.findViewById(R.id.wordPopTip)

        pop_root = popupWindowView.findViewById(R.id.pop_root)

        popupWindow.setOutsideTouchable(true);
        popupWindow.setFocusable(false);

        popupWindow.animationStyle = R.style.WordPopShowAnim

        handleShow()
    }


    //处理展示
    private fun handleShow() {

        try {

            pop_root?.setOnClickListener {
                popDimss()
            }

            if (wordPopDataBean!!.queryStatus != PARAM.WordQuerySuccess) {
                //不是查询成功状态

                wordTv?.text = wordPopDataBean!!.wordContent

                wordMeanTv?.visibility = View.GONE

                wordPopTip?.text = wordPopDataBean!!.queryTipContent

            } else {
                //查询成功

                wordMeanTv?.visibility = View.GONE

                wordPopTip?.text = wordPopDataBean!!.queryTipContent

                wordPopTip?.setOnClickListener {
                    Toast.makeText(context, "查看更多", Toast.LENGTH_SHORT).show()
                    popDimss()
                }


                //填充数据

                wordTv?.text = wordPopDataBean!!.wordContent

                var means = queryResultBean!!.means

                if (means.isNullOrEmpty().not()) {

                    var meanList = means.split(";", "；")

                    if (meanList.isNullOrEmpty().not()) {

                        wordMeanTv?.visibility = View.VISIBLE

                        var sb: StringBuilder = StringBuilder()

                        for (i in 0 until meanList!!.size) {

                            sb.append(meanList[i])

                            if (i != meanList.size - 1) {
                                sb.append("\n")
                            }

                        }
                        wordMeanTv?.text = sb.toString()
                    }
                }

            }

            //以下开始定位等计算操作

            //状态栏高度
            var statuBarHeight = UiUtils.getStatusBarHeight(context)

            //屏幕宽度
            val screenWidth = UiUtils.getScreenWidth(context)

            //屏幕高度
            val screenHeight = UiUtils.getScreenHeight(context)

            //标题高度
            var titleHeight = UiUtils.dp2px(context, 40f)

            var location = wordPopDataBean!!.location

            var locations = location.split("-")

            //弹框后，三角形的尖角，所在 x 轴位置
            var xLocation: Int = locations[0].toInt()

            //点击的单词，距离屏幕顶部的距离（含状态栏）
            var yLocation: Int = locations[1].toInt()

            //点击的单词，在 TextView 中，单词顶部 距离控件顶部的距离
            var topValue: Int = locations[2].toInt()

            //点击的单词，在 TextView 中，单词底部 距离控件部的距离
            var bottomValue: Int = locations[3].toInt()

            //弹框距离左右各 20dp，所以减掉 40
            var widthSpec = View.MeasureSpec.makeMeasureSpec(
                (screenWidth - UiUtils.dp2px(context, 40f)).toInt(),
                View.MeasureSpec.AT_MOST
            )
            wordPopContent?.measure(widthSpec, 0)

            //文字弹框的高（基本高度，不含三角形）
            var realPopHeightBase = wordPopContent!!.measuredHeight

            Log.e("realPopHeightBase is ", "$realPopHeightBase")

            //===== 以下模拟最大高度进行计算 开始计算=====

            /**
             * 关于计算，请详细看下
             *
             * https://blog.csdn.net/u014620028/article/details/102831595
             */

            //内容区域中，margin相关的值的总和
            var marginHeight = UiUtils.dp2px(context, 20f) * 4

            var textPaint: TextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG)

            //单词大小是 20dp
            textPaint.textSize = UiUtils.dp2px(context, 20f).toFloat()
            var mFontMetrics_word: Paint.FontMetrics = textPaint.fontMetrics

            //单词的高度
            var wordHeight = mFontMetrics_word.bottom - mFontMetrics_word.top

            //释义文字的大小是 15dp
            textPaint.textSize = UiUtils.dp2px(context, 15f).toFloat()
            var mFontMetrics_mean: Paint.FontMetrics = textPaint.fontMetrics

            //释义最多展示 2行，这里就按 2行 算
            var meanHeight = (mFontMetrics_mean.bottom - mFontMetrics_mean.top) * 2

            //提示文字的大小是 15dp
            textPaint.textSize = UiUtils.dp2px(context, 15f).toFloat()
            var mFontMetrics_tip: Paint.FontMetrics = textPaint.fontMetrics

            //提示文字的高度
            var tipHeight = mFontMetrics_tip.bottom - mFontMetrics_tip.top

            //最后额外加点高度。如：40像素。因为画笔测量有误差，具体情况，视情况而定
            var virtualPopHeightBase =
                (marginHeight + wordHeight + meanHeight + tipHeight + 5).toInt()

            Log.e("virtualPopHeightBase is ", "$virtualPopHeightBase")

            //===== 以上模拟最大高度进行计算 计算结束=====

            //三角形高度
            var triangle_height = UiUtils.dp2px(context, 8f)

            //三角形宽度
            var triangle_width = UiUtils.dp2px(context, 16f)

            //完整的弹框，真实高度
            var realHeight = realPopHeightBase + triangle_height

            //虚拟高度
            var virtualHeight = virtualPopHeightBase + triangle_height

            //文字上方预计高度
            var expectTopHeight = yLocation - statuBarHeight - titleHeight

            //文字下方预计高度
            var expectBottomHeight = screenHeight - (yLocation - statuBarHeight + (bottomValue - topValue))

            if (expectTopHeight >= virtualHeight) {
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

                var contentLp: RelativeLayout.LayoutParams =
                    wordPopContentView?.layoutParams as RelativeLayout.LayoutParams

                contentLp.topMargin = yLocation - statuBarHeight - realHeight

                wordPopContentView?.layoutParams = contentLp

                EventBus.getDefault().post(SelectWordEvent(SelectWordEvent.ShowSelectWordView))

            } else if (expectBottomHeight > virtualHeight) {
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

                var contentLp: RelativeLayout.LayoutParams =
                    wordPopContentView?.layoutParams as RelativeLayout.LayoutParams

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

            pop_root?.requestLayout()


        } catch (e: Exception) {
            e.printStackTrace()
        }

    }


    /**
     * 刷新单词弹框
     */
    fun refreshWordPop(bean: WordPopDataBean, qBean: QueryResultBean?) {

        wordPopDataBean = bean

        queryResultBean = qBean

        locationView = wordPopDataBean!!.selectWordView

        handleShow()
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