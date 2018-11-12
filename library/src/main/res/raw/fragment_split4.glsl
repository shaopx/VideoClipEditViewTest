#extension GL_OES_EGL_image_external : require
precision highp float;
varying highp vec2 textureCoordinate;
uniform sampler2D inputTexture;
void main() {
vec2 uv = textureCoordinate;
if (uv.x <= 0.5) { uv.x = uv.x * 2.0; }
else { uv.x = (uv.x - 0.5) * 2.0; }
if (uv.y <= 0.5) { uv.y = uv.y * 2.0; }
else { uv.y = (uv.y - 0.5) * 2.0; }
gl_FragColor = texture2D(inputTexture, fract(uv));
}
