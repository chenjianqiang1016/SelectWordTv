package com.demo.selectwordtv.mySelectWord

import android.content.Context
import android.graphics.Color
import android.graphics.Rect
import android.text.*
import android.text.method.LinkMovementMethod
import android.text.style.BackgroundColorSpan
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.widget.TextView
import android.widget.Toast
import com.demo.selectwordtv.PARAM
import com.demo.selectwordtv.R
import com.demo.selectwordtv.SpUtil
import java.util.regex.Pattern


/**
 * 2019/07/01
 * 英语文章中，高亮选中单词，并联网获取单词详情TextView
 */
class SelectWordView : TextView {

    private var characterClickListener: CharacterClickListener? = null

    private var mSelectedBgColor: Int = getResources().getColor(R.color.default_selected_bg)
    private var mSelectedWordColor: Int = getResources().getColor(R.color.default_selected_word)

    private var mTextColor: Int = 0

    private var mLastStart: Int = 0

    private var mLastEnd: Int = 0

    private var mContext: Context? = null

    constructor(context: Context?) : this(context, null)

    constructor(context: Context?, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        mContext = context
        init()

    }

    //初始化
    private fun init() {
        mTextColor = getCurrentTextColor()
        setMovementMethod(LinkMovementMethod())
        setHighlightColor(Color.TRANSPARENT)
    }

    override fun performLongClick(): Boolean {
        try {
            return super.performLongClick()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }


    override fun setText(text: CharSequence, type: TextView.BufferType) {
        super.setText(text, type)
    }

    private var highLightString: String? = null

    /**
     * 将单词转化为 span
     * @param text
     */
    fun setTextWord2Span(text: CharSequence, highlight: String? = "", tip: String = "") {

//        setTextForegColor(text.toString(),highlight)

        highLightString = highlight

        //获取高亮关键字的 span
        var fSpans: MutableList<SpanBean> = getTextForegColor(text.toString(), highlight, tip)

        var builder: SpannableStringBuilder? = null
        if (text is SpannableStringBuilder) {
            builder = text
        } else {
            builder = SpannableStringBuilder(text)
        }
        /**
         * 将单词转化为Span
         */
        val pattern = Pattern.compile("[A-Za-z]+")

        val matcher = pattern.matcher(text.toString())

        while (matcher.find()) {
            val span = CharacterSpan(
                matcher.group(),
                mTextColor
            )
            builder.setSpan(span, matcher.start(), matcher.end(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }

        if (fSpans.isNullOrEmpty().not()) {

            fSpans.forEach {
                builder.setSpan(it.span, it.start, it.end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            }

        }

        append(builder)
    }


    fun getTextForegColor(content: String?, highlight: String?, tip: String): MutableList<SpanBean> {

        var spans: MutableList<SpanBean> = mutableListOf()

        //如果文本为空，就 setText=""
        if (content.isNullOrEmpty() || highlight.isNullOrEmpty()) {
            return spans
        }

        /**
         * 整体思路
         *
         * 1、将全部的高亮关键字（keys），进行切分，拿到单个的关键字（key）
         * 2、用每个key，去原始文本中进行遍历，找到出现的位置（每次出现的位置）
         * 3、对该位置的前后进行判断，是否是有效的单词分隔符，如：空格、逗号、句号等
         * 4、如果符合条件，就是最终，要高亮的位置（有效位），这个位置上的单词，就是要高亮的单词
         *
         * 如：
         * 原始文本：abc is a Teacher.
         * 高亮单词：a
         * 结果：abc、Teacher 中的"a"，不能高亮，只有单独的那个"a"，才能高亮，单独的"a"出现的位置，才是真正的有效位
         * 理由：abc中，虽然可以找到a，但是a后面，是"b"，不是单词分隔符，所有不能算有效位
         *
         * 关于位置的处理思路：
         * 目前，找到后，先存起来（封装成一个bean（index：出现位置，key：key的内容），最后整体遍历一遍；
         * 也可以找到有效位，就高亮变色
         *
         */

        //所有要高亮的单词
        var keys = highlight.trim().split(";", "；")

        if (keys.isNullOrEmpty().not()) {
            //高亮关键字集合不为空

            //创建一个 spannableString
            var spannableString: SpannableString = SpannableString(content)

            //最后要集中处理的高亮位置的单词
            var keyBeanList: MutableList<HKeyBean> = mutableListOf()

            //视为分隔符的 字符 的集合
            var separatedChars: MutableList<Char> = mutableListOf(
                'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j',
                'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't',
                'u', 'v', 'w', 'x', 'y', 'z'
            )

            //遍历keys中，每个高亮关键字，并对其进行处理
            keys.forEach {

                var index = 0

                //截取原始文本的开始位置
                var subStartIndex = 0

                var subContent = content!!

                while (subContent.contains(it)) {

                    //找到当前的这个关键字，在剩余文本中第一次出现的位置（不一定是有效位）
                    var indexOf = subContent.indexOf(it)

                    //设置、记录下一次，要截取的位置的开始
                    subStartIndex = indexOf + it.length

                    index = index + indexOf

                    //剩余文本中，找到了这个单词，判断其有效位
                    if (indexOf == 0) {
                        //单词出现的位置，是剩余文本的起点处

                        /**
                         * 特别说明
                         *
                         * 这里要检查一下标记的前一位，是否是有效位
                         *
                         * 因为会出现特殊情况
                         *
                         * 如：abc haha def
                         * key = ha
                         *
                         * 后续处理时
                         * 第一次：
                         * abc haha def
                         *
                         * indexOf = 4
                         * subStartIndex = indexOf + it.length = 4 + ha.length = 4+2 = 6
                         * index = 0 + 4 = 4
                         *
                         * 会走到 indexOf != 0 情况中的一种，判定结果为：haha中，前半部分的"ha"，不能高亮
                         *
                         * 第二次：从 haha 的第二个 h 处开始截取
                         * 子串为：ha def
                         * key = ha
                         * 这个时候，这个 ha 还是不能高亮，因为它前面还紧跟字母，只是因为截取看不到
                         *
                         * 所以要对它的前一位做判断，同时，为了避免繁琐的判断，直接用 try...catch...
                         */
                        var checkIndex = true
                        try {

                            if (separatedChars.contains(content[index - 1].toLowerCase())) {
                                checkIndex = false
                            }

                        } catch (e: Exception) {

                        }

                        if (checkIndex) {


                            if (it.length == subContent.length) {
                                /**
                                 * 当前剩余文本，都是关键字
                                 * 如：剩余文本：abc
                                 * key=abc
                                 */

                                //是有效位

                                keyBeanList.add(
                                    HKeyBean(
                                        index,
                                        it
                                    )
                                )

                            } else if (!separatedChars.contains(subContent[it.length].toLowerCase())) {

                                /**
                                 * 单词的下一个位置的字符，是分隔类型字符
                                 *
                                 * 如：剩余文本：ab cd
                                 * key = ab
                                 * 则：
                                 * indexOf = 0
                                 * keyLength = 2
                                 * indexOf + it.length = 2
                                 *
                                 * subContent[it.length] = subContent[2] = ' '(空格)
                                 *
                                 * separatedChars中不包含空格
                                 */
                                //是有效位
                                keyBeanList.add(
                                    HKeyBean(
                                        index,
                                        it
                                    )
                                )
                            }
                        }

                    } else {
                        //出现位置，不再文本开始处

                        if (indexOf + it.length == subContent.length && !separatedChars.contains(subContent[indexOf - 1].toLowerCase())) {

                            /**
                             * 如：剩余文本为：ab cde
                             * key = cde
                             *
                             * indexOf = 3
                             * keyLength = 3
                             * indexOf + it.length =6
                             * subContent.length = 6
                             *
                             * subContent[indexOf -1] = subContent[2] = ' '(空格)
                             */
                            //是有效位
                            keyBeanList.add(
                                HKeyBean(
                                    index,
                                    it
                                )
                            )

                        } else {

                            if (!separatedChars.contains(subContent[indexOf - 1].toLowerCase()) && !separatedChars.contains(
                                    subContent[indexOf + it.length].toLowerCase()
                                )
                            ) {
                                keyBeanList.add(
                                    HKeyBean(
                                        index,
                                        it
                                    )
                                )
                            }

                        }
                    }

                    //下一次要处理的子串
                    subContent = subContent.substring(subStartIndex)

                    //避免错位，要进行对应的移位
                    index += it.length

                }
            }

            Log.e("keyBeanList = ", "$keyBeanList")

            //集中处理需要高亮的单词
            if (keyBeanList.isNotEmpty()) {

                keyBeanList.forEach {

                    spans.add(
                        SpanBean(
                            ForegroundColorSpan(Color.parseColor("#ff0000")),
                            it.index,
                            it.index + it.key.length
                        )
                    )

                }
            }

            if (tip.isEmpty().not()) {

                spans.add(
                    SpanBean(
                        ForegroundColorSpan(Color.parseColor("#0000ff")),
                        content.indexOf(tip),
                        content.indexOf(tip) + tip.length
                    )
                )

            }

            return spans

        } else {
            return spans
        }


    }


    override fun onTouchEvent(event: MotionEvent): Boolean {
        val result = super.onTouchEvent(event)
        val action = event.action
        var x = event.x
        var y = event.y

        x -= totalPaddingLeft.toFloat()
        y -= totalPaddingTop.toFloat()

        x += scrollX.toFloat()
        y += scrollY.toFloat()

        val layout = layout
        val line = layout.getLineForVertical(y.toInt())
        var off = layout.getOffsetForHorizontal(line, x)

        val v = layout.getPrimaryHorizontal(off)// 获取 屏幕显示点击字符 的水平x

        if (x > v) {
            off++
        }

        if (characterClickListener == null) {
            return false
        }

        if (action == MotionEvent.ACTION_DOWN) {// 解决点击末尾空白处最后一个span响应的bug
            if (x < 0 || x > layout.getLineWidth(line) + 1) {
                removeSelection()
                characterClickListener?.onClickBlank()
                return false
            }
        }

        val text = text
        if (TextUtils.isEmpty(text) || text !is Spannable) {
            return result
        }

        val link = text.getSpans(off, off, ClickableSpan::class.java)

        if (link.size != 0) {
            if (action == MotionEvent.ACTION_DOWN) {
                //1.恢复上一次的选中颜色
                //2.选中新的部分
                removeSelection()


                val spanStart = text.getSpanStart(link[0])
                val spanEnd = text.getSpanEnd(link[0])

                if (spanStart >= 0 && spanEnd > spanStart && (highLightString == null || !(highLightString!!.contains(
                        text.subSequence(
                            spanStart,
                            spanEnd
                        ).toString()
                    )))
                ) {
                    mLastStart = spanStart
                    mLastEnd = spanEnd

                    Selection.setSelection(text, spanStart, spanEnd)
                } else {
                    characterClickListener?.onClickBlank()
                }
            } else if (action == MotionEvent.ACTION_UP) {
                // 1,改变选中字背景
                // 2,返回选中数据
                val start = Selection.getSelectionStart(text)
                val end = Selection.getSelectionEnd(text)
                if (start >= 0 && end > start) {
                    text.setSpan(BackgroundColorSpan(mSelectedBgColor), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                    text.setSpan(ForegroundColorSpan(mSelectedWordColor), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

                    if (highLightString == null || !(highLightString!!.contains(
                            text.subSequence(
                                start,
                                end
                            ).toString()
                        ))
                    ) {


                        var layout: Layout = this.getLayout()

                        //当前单词在第几行
                        var line: Int = layout.getLineForOffset(start)

                        var bound: Rect = Rect()

                        layout.getLineBounds(line, bound);

                        //到此 bound 表示点击选中的单词的矩形

                        var left = bound.left;

                        //选中的单词的顶部，距离其所在 TextView 的顶部的高度
                        var top = bound.top;
                        var right = bound.right;

                        //选中的单词的底部，距离其所在 TextView 的顶部（注意，是距离顶部、顶部）的高度
                        var bottom = bound.bottom;
                        var width = bound.width();
                        var height = bound.height();

                        Log.e("left ", "$left")
                        Log.e("top ", "$top")
                        Log.e("right ", "$right")
                        Log.e("bottom ", "$bottom")
                        Log.e("width ", "$width")
                        Log.e("height ", "$height")

                        //选中的单词的左边界，距离其所在 TextView 的左边的距离。注意传值
                        var primaryHorizontal: Float = layout.getPrimaryHorizontal(start);

                        //选中的单词的右边界，距离其所在 TextView 的左边的距离。注意传值
                        var secondaryHorizontal: Float = layout.getSecondaryHorizontal(end);

                        Log.e("primaryHorizontal ", "$primaryHorizontal")
                        Log.e("secondaryHorizontal ", "$secondaryHorizontal")

                        val intArray = IntArray(2)
                        val trashArray = IntArray(2)

                        /**
                         * 关于getLocationInWindow、getLocationOnScreen
                         *
                         * https://blog.csdn.net/tmj2014/article/details/53283804
                         */
                        this.getLocationInWindow(intArray)
                        this.getLocationOnScreen(trashArray)


                        Log.e("intArray ", "intArray 0 is ${intArray[0]} 1 is ${intArray[1]}")
                        Log.e("trashArray ", "trashArray 0 is ${trashArray[0]} 1 is ${trashArray[1]}")

                        //单词所在控件，距离屏幕左边的距离
                        var xTv = trashArray[0]
                        //单词所在控件，距离屏幕顶部（含状态栏）的距离
                        var yTv = trashArray[1]

                        //选中的单词的中点，对应的 x 轴坐标
                        var x = xTv + primaryHorizontal + (secondaryHorizontal - primaryHorizontal) / 2

                        //注意yTv、top的含义。y 表示，选中的单词的顶部，距离屏幕顶部的距离（含状态栏）
                        var y = yTv + top

                        var sb: StringBuilder = StringBuilder()

                        sb.append(x.toInt())
                        sb.append("-")
                        sb.append(y.toInt())
                        sb.append("-")
                        sb.append(top.toInt())
                        sb.append("-")
                        sb.append(bottom.toInt())

                        SpUtil.put(PARAM.WordPopLocation, sb.toString())

                        Toast.makeText(mContext,"第 $line 行 ${text.subSequence(start, end).toString()}", Toast.LENGTH_SHORT).show()

                        characterClickListener?.onCharacterClick(this, text.subSequence(start, end).toString())
                    }

                }

            } else if (action == MotionEvent.ACTION_CANCEL) {
                removeSelection()
                characterClickListener?.onClickBlank()
            }/*|| action == MotionEvent.ACTION_CANCEL*/
            return true
        } else {
            removeSelection()
            characterClickListener?.onClickBlank()
        }

        return result
    }

    /**
     * 删除选中效果
     */
    fun removeSelection() {

        val buffer = text as Spannable
        val start = mLastStart
        val end = mLastEnd

        if (start >= 0 && end > start) {
            buffer.setSpan(BackgroundColorSpan(Color.TRANSPARENT), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            buffer.setSpan(ForegroundColorSpan(mTextColor), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }

        Selection.removeSelection(buffer)
    }

    /**
     *
     * 设置点击事件
     * @param characterClickListener
     */
    fun setCharacterClickListener(characterClickListener: CharacterClickListener?) {
        this.characterClickListener = characterClickListener
    }


    /**
     * dip转pix
     *
     * @param dpValue
     * @return
     */
    fun dp2px(context: Context, dpValue: Float): Int {
        val scale = context.resources.displayMetrics.density
        return (dpValue * scale + 0.5f).toInt()
    }


}