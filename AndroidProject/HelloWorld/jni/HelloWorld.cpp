#include <jni.h>
#include <com_example_helloworld_MainActivity.h>

JNIEXPORT jstring JNICALL Java_com_example_helloworld_MainActivity_msgFromNDK
  (JNIEnv *env, jobject thisz){
	jstring jstr = env->NewStringUTF("Hello from NDK!");
	return jstr;
}
