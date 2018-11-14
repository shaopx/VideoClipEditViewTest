package com.spx.egl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class VideoProcessConfig implements Serializable {
    public String srcMediaPath;
    public String outMediaPath;

    public List<GlFilterConfig> filterConfigList = new ArrayList<>();

    public VideoProcessConfig(String srcMediaPath, String outMediaPath) {
        this.srcMediaPath = srcMediaPath;
        this.outMediaPath = outMediaPath;
    }

    public List<GlFilterConfig> getFilterConfigList() {
        return filterConfigList;
    }


}
