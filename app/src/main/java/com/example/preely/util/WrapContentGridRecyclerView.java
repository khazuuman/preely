package com.example.preely.util;

import android.content.Context;
import android.util.AttributeSet;

import androidx.recyclerview.widget.RecyclerView;

public class WrapContentGridRecyclerView extends RecyclerView {
    public WrapContentGridRecyclerView(Context context) {
        super(context);
    }

    public WrapContentGridRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public WrapContentGridRecyclerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onMeasure(int widthSpec, int heightSpec) {
        int expandSpec = MeasureSpec.makeMeasureSpec(
                Integer.MAX_VALUE >> 2, MeasureSpec.AT_MOST
        );
        super.onMeasure(widthSpec, expandSpec);
    }
}
