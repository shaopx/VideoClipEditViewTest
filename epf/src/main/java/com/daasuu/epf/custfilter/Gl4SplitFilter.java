package com.daasuu.epf.custfilter;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLES30;

import com.daasuu.epf.R;
import com.daasuu.epf.filter.FilterType;
import com.daasuu.epf.filter.GlFilter;

public class Gl4SplitFilter extends GlFilter {


    public Gl4SplitFilter(Context context) {
        super(context, R.raw.def_vertext, R.raw.fragment_split4);
    }

    @Override
    public FilterType getFilterType() {
        return FilterType.SPX_4SPLIT;
    }
}


