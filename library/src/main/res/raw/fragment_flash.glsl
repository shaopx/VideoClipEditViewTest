#extension GL_OES_EGL_image_external : require
precision mediump float;
varying vec2 textureCoordinate;
uniform sampler2D inputTexture;
//修改这个值，可以控制曝光的程度
uniform float uAdditionalColor;
void main()
{
vec4 color = texture2D(inputTexture,textureCoordinate.xy);
gl_FragColor = vec4(color.r + uAdditionalColor,color.g + uAdditionalColor,color.b + uAdditionalColor,color.a);
}