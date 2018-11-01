package com.daasuu.epf.custfilter;

import com.daasuu.epf.filter.GlFilter;

public class GLImageAmaroFilter extends GlFilter {
    private static final String FRAGMENT_SHADER = "" +
            "precision mediump float;\n" +
            " \n" +
            " varying mediump vec2 textureCoordinate;\n" +
            " \n" +
            " uniform sampler2D inputTexture;\n" +
            " uniform sampler2D blowoutTexture; //blowout;\n" +
            " uniform sampler2D overlayTexture; //overlay;\n" +
            " uniform sampler2D mapTexture; //map\n" +
            " \n" +
            " uniform float strength;\n" +
            "\n" +
            " void main()\n" +
            " {\n" +
            "     vec4 originColor = texture2D(inputTexture, textureCoordinate);\n" +
            "     vec4 texel = texture2D(inputTexture, textureCoordinate);\n" +
            "     vec3 bbTexel = texture2D(blowoutTexture, textureCoordinate).rgb;\n" +
            "     \n" +
            "     texel.r = texture2D(overlayTexture, vec2(bbTexel.r, texel.r)).r;\n" +
            "     texel.g = texture2D(overlayTexture, vec2(bbTexel.g, texel.g)).g;\n" +
            "     texel.b = texture2D(overlayTexture, vec2(bbTexel.b, texel.b)).b;\n" +
            "     \n" +
            "     vec4 mapped;\n" +
            "     mapped.r = texture2D(mapTexture, vec2(texel.r, 0.16666)).r;\n" +
            "     mapped.g = texture2D(mapTexture, vec2(texel.g, 0.5)).g;\n" +
            "     mapped.b = texture2D(mapTexture, vec2(texel.b, 0.83333)).b;\n" +
            "     mapped.a = 1.0;\n" +
            "     \n" +
            "     mapped.rgb = mix(originColor.rgb, mapped.rgb, strength);\n" +
            "\n" +
            "     gl_FragColor = mapped;\n" +
            " }";
}
