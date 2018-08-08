#ifndef __UDT_JNI_H__
#define __UDT_JNI_H__

#include <stdint.h>
#include <jni.h>
#include "udt.h"
#include <list>
#include<android/log.h>


#ifdef __cplusplus
extern "C" {
#endif

#define TAG    "wb_cim" // 这个是自定义的LOG的标识
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG,TAG ,__VA_ARGS__) // 定义LOGD类型
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO,TAG ,__VA_ARGS__) // 定义LOGI类型
#define LOGW(...) __android_log_print(ANDROID_LOG_WARN,TAG ,__VA_ARGS__) // 定义LOGW类型
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR,TAG ,__VA_ARGS__) // 定义LOGE类型
#define LOGF(...) __android_log_print(ANDROID_LOG_FATAL,TAG ,__VA_ARGS__) // 定义LOGF类型

#define READ_BUF_SIZE  1024 * 128

/*
 * Class:     com_udt_udt
 * Method:    startup
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_com_cooyet_im_imservice_manager_IMUdtManager_startup
        (JNIEnv *, jobject);

/*
 * Class:     com_udt_udt
 * Method:    cleanup
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_com_cooyet_im_imservice_manager_IMUdtManager_cleanup
        (JNIEnv *, jobject);

/*
 * Class:     com_udt_udt
 * Method:    socket
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_com_cooyet_im_imservice_manager_IMUdtManager_socket
        (JNIEnv *, jobject, jstring);

/*
 * Class:     com_udt_udt
 * Method:    connect
 * Signature: (ILjava/lang/String;I)I
 */
JNIEXPORT jint JNICALL Java_com_cooyet_im_imservice_manager_IMUdtManager_connect
        (JNIEnv *, jobject, jint, jstring, jint);

/*
 * Class:     com_udt_udt
 * Method:    close
 * Signature: (I)I
 */
JNIEXPORT jint JNICALL Java_com_cooyet_im_imservice_manager_IMUdtManager_close
        (JNIEnv *, jobject, jint);

/*
 * Class:     com_udt_udt
 * Method:    send
 * Signature: (I[BIII)I
 */
JNIEXPORT jint JNICALL Java_com_cooyet_im_imservice_manager_IMUdtManager_send
        (JNIEnv *, jobject, jint, jbyteArray, jint, jint);

/*
 * Class:     com_udt_udt
 * Method:    recv
 * Signature: (I[BIII)I
 */
JNIEXPORT jbyteArray JNICALL Java_com_cooyet_im_imservice_manager_IMUdtManager_recv
        (JNIEnv *, jobject, jint);
char *ConvertJByteaArrayToChars(JNIEnv *env, jbyteArray bytearray);

#ifdef __cplusplus
}
#endif

#endif

