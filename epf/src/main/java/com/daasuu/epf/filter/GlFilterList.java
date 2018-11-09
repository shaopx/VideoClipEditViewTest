package com.daasuu.epf.filter;

import android.util.Log;

import com.daasuu.epf.EFramebufferObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

/**
 * Created by shaopx on 2018/11/05.
 * glfiter list , time aware
 * its name is like a list , But, one time one filter.  different with  GlFilterGroup.
 */
public class GlFilterList {

    private static final String TAG = "GlFilterList";
    private final LinkedList<GlFilterPeriod> glFilerPeriod = new LinkedList<>();

    public GlFilterList() {
        glFilerPeriod.add(0, new GlFilterPeriod(0, 600 * 1000, new GlFilter()));
    }

    public void putGlFilter(GlFilterPeriod period) {
//        period.filter.setup();
        glFilerPeriod.add(0, period);
    }

    public GlFilter getGlFilter(long time) {
        for (GlFilterPeriod glFilterPeriod : glFilerPeriod) {
            if (glFilterPeriod.contains(time)) {
                return glFilterPeriod.filter;
            }
        }
        return null;
    }

    public void draw(int texName, EFramebufferObject fbo, long presentationTimeUs, Map<String, Integer> extraTextureIds) {
//        Log.d(TAG, "draw: presentationTimeUs:"+presentationTimeUs+", glFilerPeriod:"+glFilerPeriod);
        for (GlFilterPeriod glFilterPeriod : glFilerPeriod) {
            if (glFilterPeriod.contains(presentationTimeUs / (1000*1000))) {
                glFilterPeriod.filter.draw(texName, fbo, extraTextureIds);
                return;
            }
        }
    }

    public void release() {
        for (GlFilterPeriod glFilterPeriod : glFilerPeriod) {
            glFilterPeriod.filter.release();
        }
    }

    public void setFrameSize(int width, int height) {
        for (GlFilterPeriod glFilterPeriod : glFilerPeriod) {
            glFilterPeriod.filter.setFrameSize(width, height);
        }
    }

    public void setup() {
        for (GlFilterPeriod glFilterPeriod : glFilerPeriod) {
            glFilterPeriod.filter.setup();
        }
    }
}
