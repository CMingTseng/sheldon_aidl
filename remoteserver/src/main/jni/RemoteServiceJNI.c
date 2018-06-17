#include <jni.h>
#include <string.h>
#include <android/log.h>


#define TAG "nano-jni"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG,TAG ,__VA_ARGS__)
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO,TAG ,__VA_ARGS__)
#define LOGW(...) __android_log_print(ANDROID_LOG_WARN,TAG ,__VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR,TAG ,__VA_ARGS__)
#define LOGF(...) __android_log_print(ANDROID_LOG_FATAL,TAG ,__VA_ARGS__)


#ifdef __cplusplus
extern "C" {
#endif

/*
 * Class:     com_example_remoteserver_RemoteService
 * Method:    stringFromJNI
 * Signature: ()Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_com_example_remoteserver_RemoteService_stringFromJNI
  (JNIEnv *env, jobject thiz){
    return (*env)->NewStringUTF(env, "Hi! Sheldon, I`m JNI ~");
  }

/*
 * Class:     com_example_remoteserver_RemoteService
 * Method:    NanoOpen
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_com_example_remoteserver_RemoteService_NanoOpen
  (JNIEnv *env, jobject thiz){
    return Nano_Open(NULL);
  }

/*
 * Class:     com_example_remoteserver_RemoteService
 * Method:    NanoPollEvent
 * Signature: ([BI)I
 */
JNIEXPORT jint JNICALL Java_com_example_remoteserver_RemoteService_NanoPollEvent
  (JNIEnv *env, jobject thiz, jbyteArray dataBuf, jint size){

   /*Jni层接收到Java层传递过来的byte[]数组，一般有2个函数来获取它的值，
   一个 GetByteArrayRegion，另一个是 GetByteArrayElements ，
   前者是进行值拷贝，将Java端数组的数据拷贝到本地的数组中，
   后者是指针的形式，将本地的数组指针直接指向Java端的数组地址(效率更高)，
   其实本质上是JVM在堆上分配的这个数组对象上增加一个引用计数，
   保证垃圾回收的时候不要释放，从而交给本地的指针使用，
   使用完毕后指针一定要记得通过ReleaseByteArrayElements进行释放，
   否则会产生内存泄露。*/

    unsigned char* local = (*env)->GetByteArrayElements(env, dataBuf, NULL);
    if(!local){
        LOGW("invalid buff\n");
        return -1;
    }

    if ((*env)->ExceptionCheck(env)) return -1;


    int ret = Nano_PollEvent(local,size);

    //一定要释放减少引用计数
    (*env)->ReleaseByteArrayElements(env,dataBuf,local,0);


    /*第三种Direct Buffer方式*/
    /*
    unsigned char* local = (unsigned char*)(*env)->GetDirectBufferAddress(env, dataBuf);
    int ret = Nano_PollEvent(local,size);
    LOGI("Need size = %d , Get data size = %d",size,ret);
    */
    return ret;
  }

/*
 * Class:     com_example_remoteserver_RemoteService
 * Method:    NanosetSpeakerOn
 * Signature: (Z)Z
 */
JNIEXPORT jboolean JNICALL Java_com_example_remoteserver_RemoteService_NanosetSpeakerOn
  (JNIEnv *env, jobject thiz, jboolean state){
  	unsigned char s = state;
  	LOGD("Speaker state : %d",s);
  	return Nano_setSpeakerOn(state);
  }

/*
 * Class:     com_example_remoteserver_RemoteService
 * Method:    NanodialCall
 * Signature: (Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_com_example_remoteserver_RemoteService_NanodialCall
  (JNIEnv *env, jobject thiz, jstring phoneNumber){
  	char* number = (char *) (*env)->GetStringUTFChars(env, phoneNumber, 0);
  	LOGD("phoneNumber %s",number);
  	return Nano_dialCall(number);
  }

/*
 * Class:     com_example_remoteserver_RemoteService
 * Method:    NanoincomingCall
 * Signature: (Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_com_example_remoteserver_RemoteService_NanoincomingCall
  (JNIEnv *env, jobject thiz, jstring phoneNumber){
  	char* number = (char *) (*env)->GetStringUTFChars(env, phoneNumber, 0);
  	LOGD("phoneNumber %s",number);
  	return Nano_incomingCall(number);
  }


/*
 * Class:     com_example_remoteserver_RemoteService
 * Method:    NanoanswerCall
 * Signature: (Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_com_example_remoteserver_RemoteService_NanoanswerCall
  (JNIEnv *env, jobject thiz, jstring phoneNumber){
  	char* number = (char *) (*env)->GetStringUTFChars(env, phoneNumber, 0);
  	LOGD("phoneNumber %s",number);
  	return Nano_answerCall(number);
  }

/*
 * Class:     com_example_remoteserver_RemoteService
 * Method:    NanohangupCall
 * Signature: (Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_com_example_remoteserver_RemoteService_NanohangupCall
  (JNIEnv *env, jobject thiz, jstring phoneNumber){
  	char* number = (char *) (*env)->GetStringUTFChars(env, phoneNumber, 0);
  	LOGD("phoneNumber %s",number);
  	return Nano_hangupCall(number);
  }

#ifdef __cplusplus
}
#endif