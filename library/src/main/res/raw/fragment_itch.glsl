#extension GL_OES_EGL_image_external : require
precision highp float;
varying vec2 textureCoordinate;
uniform sampler2D inputTexture;
//这是个二阶向量，x是横向偏移的值，y是阈值
uniform vec2 uScanLineJitter;
//颜色偏移的值
uniform float uColorDrift;
//随机函数
float nrand(in float x, in float y)
{
    return fract(sin(dot(vec2(x, y), vec2(12.9898, 78.233))) * 43758.5453);
}

void main()
{
float u = textureCoordinate.x;
float v = textureCoordinate.y;
float jitter = nrand(v,0.0) * 2.0 - 1.0;
float drift = uColorDrift;
float offsetParam = step(uScanLineJitter.y,abs(jitter));
jitter = jitter * offsetParam * uScanLineJitter.x;
vec4 color1 = texture2D(inputTexture,fract(vec2( u + jitter,v)));
vec4 color2 = texture2D(inputTexture,fract(vec2(u + jitter + v*drift ,v)));
gl_FragColor = vec4(color1.r,color2.g,color1.b,1.0);
}