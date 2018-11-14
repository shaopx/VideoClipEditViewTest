package com.spx.egl;

import com.daasuu.epf.filter.GlFilter;

import java.io.Serializable;

public class GlFilterPeriod implements Serializable {
    public long startTimeMs;
    public long endTimeMs;
    public GlFilter filter;

    public GlFilterPeriod(long startTimeMs, long endTimeMs, GlFilter filter) {
        this.startTimeMs = startTimeMs;
        this.endTimeMs = endTimeMs;
        this.filter = filter;
    }

    public boolean contains(long time) {
        return time >= startTimeMs && time <= endTimeMs;
    }

    public boolean touched(GlFilterPeriod period) {
        return false;
    }

    @Override
    public String toString() {
        return "[" + startTimeMs + "," + endTimeMs + "]" + filter.getName() + ";";
    }

    public GlFilterPeriod copy() {
        GlFilterPeriod period = new GlFilterPeriod(startTimeMs, endTimeMs, filter);
        return period;
    }
}
