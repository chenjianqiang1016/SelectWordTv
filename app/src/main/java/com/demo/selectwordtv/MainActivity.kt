package com.demo.selectwordtv

import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.Window
import androidx.appcompat.app.AppCompatActivity
import com.demo.selectwordtv.PARAM.Companion.WordPopLocation
import com.demo.selectwordtv.mySelectWord.CharacterClickListener
import com.demo.selectwordtv.wordPopView.WordPopShowView
import kotlinx.android.synthetic.main.activity_main.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import kotlin.concurrent.thread

/**
 * https://github.com/otwayz/Scallop
 */

class MainActivity : AppCompatActivity() {

    //单词弹框view
    private var wordPopShowView: WordPopShowView? = null

    private var wordPopDataBean: WordPopDataBean? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.activity_main)

        EventBus.getDefault().register(this)

        //句子
        var s1 = "A It is our choices that show what we truly are, far more than our abilities."

        //需要高亮的关键字
        var highLight = "show"

        var tip = "（测试提示语）"

        var s2 =
            "Face the past with the least regrets, face the present with the least waste and face the future with the a most dreams"

        //给第一个控件赋值

        selectWord_1?.setCharacterClickListener(object : CharacterClickListener {
            override fun onCharacterClick(view: View, character: String) {

                try {

                    /**
                     * 这里，拿到定位坐标后，其实可以直接处理弹框展示相关，之所以再次发出去，是为了模拟在 listView等中的使用
                     *
                     * 假设在 adapter中点击了一个item，item中拿到了定位数据，就发到 listView所在的界面，界面去处理弹框
                     */
                    var sLocation = SpUtil.getStringValue(WordPopLocation)

                    if (sLocation.isNullOrEmpty().not()) {
                        EventBus.getDefault().post(
                            SelectWordEvent(
                                SelectWordEvent.SearchSelectWord,
                                WordPopDataBean(
                                    sLocation!!,//定位相关数据
                                    character,//单词
                                    selectWord_1
                                )
                            )
                        )
                    }

                } catch (e: Exception) {
                    e.printStackTrace()
                }

            }

            override fun onClickBlank() {

            }

        })

        selectWord_1?.setText("")

        selectWord_1?.setTextWord2Span(
            s1 + tip,
            highLight,
            tip
        )


        //给第二个控件赋值

        selectWord_2?.setCharacterClickListener(object : CharacterClickListener {
            override fun onCharacterClick(view: View, character: String) {

                try {

                    var sLocation = SpUtil.getStringValue(WordPopLocation)

                    if (sLocation.isNullOrEmpty().not()) {
                        EventBus.getDefault().post(
                            SelectWordEvent(
                                SelectWordEvent.SearchSelectWord,
                                WordPopDataBean(
                                    sLocation!!,//定位相关数据
                                    character,//单词
                                    selectWord_2
                                )
                            )
                        )
                    }

                } catch (e: Exception) {
                    e.printStackTrace()
                }

            }

            override fun onClickBlank() {

            }

        })

        selectWord_2?.setText("")

        selectWord_2?.setTextWord2Span(
            s2,
            "",
            ""
        )

    }

    override fun onStop() {
        if (wordPopShowView != null) {
            wordPopShowView?.popDimss()
        }
        super.onStop()
    }

    override fun onDestroy() {
        EventBus.getDefault().unregister(this)
        super.onDestroy()
    }

    override fun onBackPressed() {

        if (wordPopShowView != null && wordPopShowView!!.wordPopIsShow()) {
            wordPopShowView?.popDimss()
            return
        }

        super.onBackPressed()
    }


    //展示 单词弹框
    private fun showSelectWordPop() {

    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    fun handleEvent(event: SelectWordEvent) {

        when (event.flag) {

            SelectWordEvent.QueryStart -> {
                //开始进行查询，模拟接口

                wordPopDataBean?.queryStatus = PARAM.WordQueryRuning
                wordPopDataBean?.queryTipContent = "查询中"

                wordPopShowView = WordPopShowView(
                    this,
                    wordPopDataBean!!
                )

                thread {
                    Thread.sleep(2000)
                    EventBus.getDefault().post(SelectWordEvent(SelectWordEvent.QueryFail))
                }

            }
            SelectWordEvent.QueryFail -> {
                //查询失败
                wordPopDataBean?.queryStatus = PARAM.WordQueryFail
                wordPopDataBean?.queryTipContent = "查询失败"

                wordPopShowView?.refreshWordPop(
                    wordPopDataBean!!,
                    null
                )

                thread {
                    Thread.sleep(2000)
                    EventBus.getDefault().post(SelectWordEvent(SelectWordEvent.QuerySuccess))
                }

            }
            SelectWordEvent.QuerySuccess -> {
                //查询成功


                wordPopDataBean?.queryStatus = PARAM.WordQuerySuccess
                wordPopDataBean?.queryTipContent = "查看详情"

                var queryResultBean:QueryResultBean = QueryResultBean(
                    wordPopDataBean?.wordContent.orEmpty(),
                    "释义1；释义2；释义3"

                )

                wordPopShowView?.refreshWordPop(
                    wordPopDataBean!!,
                    queryResultBean
                )

            }

            SelectWordEvent.SearchSelectWord -> {

                //开始搜索选中单词

                //模拟请求过程。开始请求、2秒后处理失败、再2秒后，展示数据

                try {

                    wordPopDataBean = event.bean

                    EventBus.getDefault().post(SelectWordEvent(SelectWordEvent.QueryStart))


                } catch (e: Exception) {
                    e.printStackTrace()
                }

            }

            SelectWordEvent.ShowSelectWordView -> {

                try {

                    if (wordPopShowView != null) {
                        wordPopShowView?.showView()
                    }

                } catch (e: Exception) {
                    e.printStackTrace()
                }

            }

            SelectWordEvent.RefreshSelectWordView -> {

                try {
                    Log.e("RefreshSelectWordView ", "${System.currentTimeMillis()}")

                    /**
                     * 弹框消失后，需要刷新一下，因为选中单词后，选中的单词有个带背景色的框，需要重绘刷新才能去掉
                     *
                     * 如果是在listView等的adapter中，可以直接用 adapter的 notify 方法整体刷新,
                     *
                     * 这里，有个问题。我调用控件的 invalidate() 方法，刷新无效。试过其父控件的刷新，还是不行。
                     *
                     * 这里，我暂时没有什么解决办法（adapter 的 notify 方法除外）。请注意
                     */


                } catch (e: Exception) {
                    e.printStackTrace()

                }

            }


        }

    }
}
