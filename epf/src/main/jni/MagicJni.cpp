#include <jni.h>
#include <GLES2/gl2.h>

#ifdef __cplusplus
extern "C" {
#endif

JNIEXPORT void JNICALL
Java_com_spx_egl_MagicJni_glReadPixels(
        JNIEnv *env, jclass cls, jint x, jint y, jint width, jint height,
        jint format, jint type) {
    glReadPixels(x, y, width, height, format, type, 0);
}

#ifdef __cplusplus
}
#endif
