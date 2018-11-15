#extension GL_OES_EGL_image_external : require
precision highp float;
varying vec2 aCoordinate;
uniform samplerExternalOES vTexture;
uniform float saturation;
void main() {
 vec2 uv = aCoordinate.xy;
 vec2 scaleCoordinate = vec2((saturation - 1.0) * 0.5 + uv.x / saturation , (saturation - 1.0) * 0.5 + uv.y / saturation);
 vec4 smoothColor = texture2D(vTexture, scaleCoordinate); // 计算红色通道偏移值
 vec4 shiftRedColor = texture2D(vTexture, scaleCoordinate + vec2(-0.1 * (saturation - 1.0), - 0.1 *(saturation - 1.0))); // 计算绿色通道偏移值
 vec4 shiftGreenColor = texture2D(vTexture, scaleCoordinate + vec2(-0.075 * (saturation - 1.0), - 0.075 *(saturation - 1.0))); // 计算蓝色偏移值
 vec4 shiftBlueColor = texture2D(vTexture, scaleCoordinate + vec2(-0.05 * (saturation - 1.0), - 0.05 *(saturation - 1.0)));
 vec3 resultColor = vec3(shiftRedColor.r, shiftGreenColor.g, shiftBlueColor.b);
 gl_FragColor = vec4(resultColor, smoothColor.a);
}