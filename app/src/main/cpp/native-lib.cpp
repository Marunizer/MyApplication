#include <jni.h>
#include <string>
#include <draco_decoder.cc>

extern "C"
JNIEXPORT jstring JNICALL
Java_sadappp_myapplication_model3D_view_DemoActivity_decoder(JNIEnv *env, jobject instance,
                                                             jstring dracoFile_, jstring objFile_) {
    const char *dracoFile = env->GetStringUTFChars(dracoFile_, 0);
    const char *objFile = env->GetStringUTFChars(objFile_, 0);

    // TODO

    env->ReleaseStringUTFChars(dracoFile_, dracoFile);
    env->ReleaseStringUTFChars(objFile_, objFile);

    //draco_decoder.decodeMachine(dracoFile,objFile);


    const char *returnValue = objFile;
    return env->NewStringUTF(returnValue);
}