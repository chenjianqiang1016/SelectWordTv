package com.demo.selectwordtv

import android.content.Context
import android.util.DisplayMetrics
import android.view.WindowManager

class UiUtils {


    companion object {

        fun dp2px(context: Context, dpValue: Float): Int {

            var scale: Float = context.resources.displayMetrics.density

            return (dpValue * scale + 0.5f).toInt()

        }

        /**
         * 获取状态栏的高度
         */
        fun getStatusBarHeight(context: Context): Int {

            //状态栏的高度
            var statusBarHeight = 0;
            //获取status_bar_height资源的ID
            var resourceId = context.resources.getIdentifier("status_bar_height", "dimen", "android");
            if (resourceId > 0) {
                //根据资源ID获取响应的尺寸值
                statusBarHeight = context.resources.getDimensionPixelSize(resourceId);
            }

            return statusBarHeight

        }

        fun getDisplayMetrics(context: Context): DisplayMetrics {

            var displaymetrics: DisplayMetrics = DisplayMetrics()

            (context.getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay.getMetrics(displaymetrics)

            return displaymetrics;
        }

        //屏幕宽度
        fun getScreenWidth(context: Context): Int {
            return getDisplayMetrics(context).widthPixels;
        }

        //屏幕高度
        fun getScreenHeight(context: Context): Int {
            return getDisplayMetrics(context).heightPixels;
        }


    }


}