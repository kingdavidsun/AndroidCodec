package com.david.codec.camera2;

import android.content.Context;
import android.content.res.Configuration;
import android.util.AttributeSet;
import android.view.SurfaceView;

/**
 * Created by David on 2020/10/20
 */
public class AutoFitSurfaceView extends SurfaceView {
    private boolean isMeasure;
    public AutoFitSurfaceView(Context context) {
        super(context);
    }

    public AutoFitSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AutoFitSurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (isMeasure)return;
        isMeasure=true;
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        if (widthMode==MeasureSpec.EXACTLY&&heightMode==MeasureSpec.AT_MOST){
            int orientation = getResources().getConfiguration().orientation;
            if (orientation== Configuration.ORIENTATION_LANDSCAPE){
                //横屏
                setMeasuredDimension(widthSize,widthSize*3/4);
            }else if (orientation==Configuration.ORIENTATION_PORTRAIT){
                //竖屏
                setMeasuredDimension(widthSize,widthSize*4/3);
            }
        }

    }
}
