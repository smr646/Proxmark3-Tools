#include <jni.h>
#include <string>
#include <android/log.h>

#define TAG "PM3_NATIVE"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, TAG, __VA_ARGS__)

extern "C" JNIEXPORT jstring JNICALL
Java_com_proxmark3_tools_MainActivity_stringFromJNI(
        JNIEnv* env,
        jobject /* this */) {
    std::string hello = "Proxmark3 Native Engine Ready!";
    LOGD("Native engine initialized successfully.");
    return env->NewStringUTF(hello.c_str());
}
