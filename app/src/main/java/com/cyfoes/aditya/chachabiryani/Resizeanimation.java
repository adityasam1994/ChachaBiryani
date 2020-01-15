package com.cyfoes.aditya.chachabiryani;

import android.content.Context;
import android.content.res.Resources;
import android.util.TypedValue;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class Resizeanimation extends Animation {

    final int targetHeight;
    LinearLayout drawlayout;
    int startHeight;
    private Context context;

    public Resizeanimation(LinearLayout drawlayout, int targetHeight, int startHeight, Context current){
        this.drawlayout = drawlayout;
        this.targetHeight = targetHeight;
        this.startHeight = startHeight;
        this.context = current;
    }

    @Override
    protected void applyTransformation(float interpolatedTime, Transformation t) {

        float newHeight = (targetHeight - startHeight)*interpolatedTime + startHeight;
        drawlayout.getLayoutParams().height = (int) newHeight;
        if(targetHeight == converttopx(60)){
            int startwidth = converttopx(50);
            float newWidth = (targetHeight - startwidth)*interpolatedTime + startwidth;
            drawlayout.getLayoutParams().width = (int) newWidth;
        }
        drawlayout.requestLayout();
    }

    @Override
    public void initialize(int width, int height, int parentWidth, int parentHeight) {
        super.initialize(width, height, parentWidth, parentHeight);
    }

    @Override
    public boolean willChangeBounds() {
        return true;
    }

    private int converttopx(int dip){
        Resources r = context.getResources();
        int px = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dip,
                r.getDisplayMetrics()
        );
        return px;
    }
}

