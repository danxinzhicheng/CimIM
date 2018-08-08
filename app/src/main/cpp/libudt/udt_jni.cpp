//
// Created by user on 2018/5/31.
//

#include <malloc.h>
#include <netdb.h>
#include "udt_jni.h"
#include "udt.h"
#include<pthread.h>
#include<set>
#include <unistd.h>
#include <list>

#ifdef __cplusplus
extern "C" {
#endif

using namespace UDT;
using namespace std;
namespace {
    std::string to_string(int value) {
        char buffer[100] = {0};
        sprintf(buffer, "%d", value);

        return std::string(buffer);
    }
}
jint JNICALL Java_com_cooyet_im_imservice_manager_IMUdtManager_startup(JNIEnv *, jobject) {
    return UDT::startup();
}

jint JNICALL Java_com_cooyet_im_imservice_manager_IMUdtManager_cleanup(JNIEnv *, jobject) {
    return UDT::cleanup();
}

jint JNICALL
Java_com_cooyet_im_imservice_manager_IMUdtManager_socket(JNIEnv *env, jobject, jstring port) {

    const char *s_port = env->GetStringUTFChars(port, NULL);

    struct addrinfo hints, *local;

    memset(&hints, 0, sizeof(struct addrinfo));

    hints.ai_flags = AI_PASSIVE;
    hints.ai_family = AF_INET;
    hints.ai_socktype = SOCK_STREAM;

    int ret1;
    if (0 != (ret1 = getaddrinfo(NULL, s_port, &hints, &local))) {
        LOGE("===connect: %s, %d", getlasterror().getErrorMessage(), ret1);
        return -1;
    }

    int m_udt_socket = UDT::socket(local->ai_family, local->ai_socktype, local->ai_protocol);
    LOGD("UdpCBaseSocket::connect, m_udt_socket=%d", m_udt_socket);

//    int snd_buf = 16000;
//    int rcv_buf = 16000;
//    UDT::setsockopt(m_udt_socket, 0, UDT_SNDBUF, &snd_buf, sizeof(int));
//    UDT::setsockopt(m_udt_socket, 0, UDT_RCVBUF, &rcv_buf, sizeof(int));
//    snd_buf = 8192;
//    rcv_buf = 8192;
//    UDT::setsockopt(m_udt_socket, 0, UDP_SNDBUF, &snd_buf, sizeof(int));
//    UDT::setsockopt(m_udt_socket, 0, UDP_RCVBUF, &rcv_buf, sizeof(int));
//    int fc = 16;
//    UDT::setsockopt(m_udt_socket, 0, UDT_FC, &fc, sizeof(int));
//    bool reuse = true;
//    bool rendezvous = false;
//    UDT::setsockopt(m_udt_socket, 0, UDT_REUSEADDR, &reuse, sizeof(bool));
//    UDT::setsockopt(m_udt_socket, 0, UDT_RENDEZVOUS, &rendezvous, sizeof(bool));

    if (UDT::ERROR == UDT::bind(m_udt_socket, local->ai_addr, local->ai_addrlen)) {
        LOGE("bind: %s", UDT::getlasterror().getErrorMessage());
        return -1;
    }

    freeaddrinfo(local);

    return m_udt_socket;
}

jint JNICALL
Java_com_cooyet_im_imservice_manager_IMUdtManager_connect(JNIEnv *env, jobject thiz, jint handle,
                                                          jstring ip, jint port) {
    const char *ip_address = env->GetStringUTFChars(ip, NULL);

    struct addrinfo hints, *peer;
    memset(&hints, 0, sizeof(struct addrinfo));

    hints.ai_flags = AI_PASSIVE;
    hints.ai_family = AF_INET;
    hints.ai_socktype = SOCK_STREAM;

    std::string port_str = to_string(port);
    if (0 != getaddrinfo(ip_address, port_str.c_str(), &hints, &peer)) {
        LOGE("incorrect server/peer address. ");
        return -1;
    }

    int connect_result = 0;
    if ((connect_result = UDT::connect(handle, peer->ai_addr, peer->ai_addrlen)) == UDT::ERROR) {
        LOGE("connect udt server error");
        return -1;
    }

    freeaddrinfo(peer);

    return connect_result;
}

jint JNICALL
Java_com_cooyet_im_imservice_manager_IMUdtManager_close(JNIEnv *env, jobject thiz, jint handle) {
    return UDT::close(handle);
}

jint JNICALL
Java_com_cooyet_im_imservice_manager_IMUdtManager_send(JNIEnv *env, jobject thiz, jint handle,
                                                       jbyteArray buffer,
                                                       jint max_send, jint flag) {
    char *buf = ConvertJByteaArrayToChars(env, buffer);
    int sent_size = UDT::send(handle, buf, max_send, flag);
    if (sent_size == UDT::ERROR) {
        LOGE("send error : %s", UDT::getlasterror().getErrorMessage());
    } else {
        LOGD("send success : %d bytes", sent_size);
    }
    delete buf;
    return sent_size;
}

char *ConvertJByteaArrayToChars(JNIEnv *env, jbyteArray bytearray) {
    char *chars = NULL;
    jbyte *bytes;
    bytes = env->GetByteArrayElements(bytearray, 0);
    int chars_len = env->GetArrayLength(bytearray);
    chars = new char[chars_len + 1];
    memset(chars, 0, chars_len + 1);
    memcpy(chars, bytes, chars_len);
    chars[chars_len] = 0;
    env->ReleaseByteArrayElements(bytearray, bytes, 0);

    return chars;
}


jbyteArray JNICALL
Java_com_cooyet_im_imservice_manager_IMUdtManager_recv(JNIEnv *env, jobject thiz, jint handle) {

    char *buf = (char *) calloc(sizeof(char), READ_BUF_SIZE);

    int rcv_size;
    int var_size = sizeof(int);

    // reset buf
    memset(buf, 0, READ_BUF_SIZE);

    UDT::getsockopt(handle, 0, UDT_RCVDATA, &rcv_size, &var_size);

    if (UDT::ERROR == (rcv_size = UDT::recv(handle, buf, READ_BUF_SIZE, 0))) {
        LOGE("UdpCBaseSocket::Recv error %s", getlasterror().getErrorMessage());
//        jbyteArray bytes = env->NewByteArray(rcv_size);
//        env->SetByteArrayRegion(bytes, 0, rcv_size, (jbyte *) buf);
//        free(buf);
//        return bytes;
        free(buf);
        return NULL;
    } else {
        LOGD("recv success : %d bytes", rcv_size);
        jbyteArray bytes = env->NewByteArray(rcv_size);
        env->SetByteArrayRegion(bytes, 0, rcv_size, (jbyte *) buf);
        free(buf);
        return bytes;
    }

}
#ifdef __cplusplus
}
#endif
