#extension GL_OES_EGL_image_external : require
precision mediump float;
varying vec2 aCoordinate;
uniform samplerExternalOES vTexture;

uniform lowp float saturation;
// Values from "Graphics Shaders: Theory and Practice" by Bailey and Cunningham
const mediump vec3 luminanceWeighting = vec3(0.2125, 0.7154, 0.0721);



void main(){
//    gl_FragColor=texture2D(vTexture,aCoordinate);

//    lowp vec4 textureColor = texture2D(vTexture, aCoordinate);
//    lowp float luminance = dot(textureColor.rgb, luminanceWeighting);
//    lowp vec3 greyScaleColor = vec3(luminance);
//    gl_FragColor = vec4(mix(greyScaleColor, textureColor.rgb, saturation), textureColor.w);

    gl_FragColor = texture2D(vTexture, aCoordinate);
}