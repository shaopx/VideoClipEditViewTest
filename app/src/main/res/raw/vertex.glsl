//#version 300 es
attribute vec2 vPosition;
attribute vec2 vCoordinate;
varying vec2 aCoordinate;
uniform mat4 uTexRotateMatrix;
void main(){
    gl_Position = uTexRotateMatrix *  vec4 ( vPosition.x, vPosition.y, 0.0, 1.0 );
    aCoordinate=vCoordinate;
}