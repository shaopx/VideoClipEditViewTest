attribute vec4 aPosition;
attribute vec4 aTextureCoord;
varying highp vec2 textureCoordinate;
void main() {
gl_Position = aPosition;
textureCoordinate = aTextureCoord.xy;
}