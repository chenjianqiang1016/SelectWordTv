package com.demo.selectwordtv.wordPopView;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;
import androidx.annotation.Nullable;

public class MyTriangleDownView extends View {

    private int view_width;
    private int view_height;

    //三角形左上角点坐标
    private double point_1_x;
    private double point_1_y;

    //三角形右下角点坐标
    private double point_2_x;
    private double point_2_y;

    //三角形右侧点坐标
    private double point_3_x;
    private double point_3_y;

    private Paint trianglePaint;

    private Path trianglePath;

    private Context mContext;

    public MyTriangleDownView(Context context) {
        this(context,null);
    }

    public MyTriangleDownView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public MyTriangleDownView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext=context;
        init();
    }

    /**
     * 初始化
     */
    private void init() {

        //三角形画笔
        trianglePaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
        trianglePaint.setStyle(Paint.Style.FILL);
        trianglePaint.setColor(0xff3C3C3D);

        //闭合区间路径
        trianglePath = new Path();

    }



    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        view_width = w;
        view_height = h;

        calculateCoordinate();

    }

    /**
     * 计算坐标
     */
    private void calculateCoordinate() {

        int value = dp2px(mContext, 5);

        point_1_x = 0;
        point_1_y = value;

        point_2_x = view_width/2f;
        point_2_y = view_height;

        point_3_x = view_width;
        point_3_y = value;

        trianglePath.moveTo((float) point_1_x, (float) point_1_y);
        trianglePath.lineTo((float) point_2_x, (float) point_2_y);
        trianglePath.lineTo((float) point_3_x, (float) point_3_y);
        trianglePath.lineTo(view_width, 0);
        trianglePath.lineTo(0, 0);
        trianglePath.close();

    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawPath(trianglePath, trianglePaint);
    }

    private int dp2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

}
