/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class nl_melp_qjs_JNI */

#ifndef _Included_nl_melp_qjs_JNI
#define _Included_nl_melp_qjs_JNI
#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     nl_melp_qjs_JNI
 * Method:    _eval
 * Signature: (J[B)[B
 */
JNIEXPORT jbyteArray JNICALL Java_nl_melp_qjs_JNI__1eval
  (JNIEnv *, jclass, jlong, jbyteArray);

/*
 * Class:     nl_melp_qjs_JNI
 * Method:    _evalPath
 * Signature: (J[B)[B
 */
JNIEXPORT jbyteArray JNICALL Java_nl_melp_qjs_JNI__1evalPath
  (JNIEnv *, jclass, jlong, jbyteArray);

/*
 * Class:     nl_melp_qjs_JNI
 * Method:    _createRuntime
 * Signature: ()J
 */
JNIEXPORT jlong JNICALL Java_nl_melp_qjs_JNI__1createRuntime
  (JNIEnv *, jclass);

/*
 * Class:     nl_melp_qjs_JNI
 * Method:    _destroyRuntime
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_nl_melp_qjs_JNI__1destroyRuntime
  (JNIEnv *, jclass, jlong);

/*
 * Class:     nl_melp_qjs_JNI
 * Method:    _createContext
 * Signature: (J)J
 */
JNIEXPORT jlong JNICALL Java_nl_melp_qjs_JNI__1createContext
  (JNIEnv *, jclass, jlong);

/*
 * Class:     nl_melp_qjs_JNI
 * Method:    _destroyContext
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_nl_melp_qjs_JNI__1destroyContext
  (JNIEnv *, jclass, jlong);

/*
 * Class:     nl_melp_qjs_JNI
 * Method:    _duplicateContext
 * Signature: (J)J
 */
JNIEXPORT jlong JNICALL Java_nl_melp_qjs_JNI__1duplicateContext
  (JNIEnv *, jclass, jlong);

/*
 * Class:     nl_melp_qjs_JNI
 * Method:    _evalBinaryPath
 * Signature: (J[B)[B
 */
JNIEXPORT jbyteArray JNICALL Java_nl_melp_qjs_JNI__1evalBinaryPath
  (JNIEnv *, jclass, jlong, jbyteArray);

/*
 * Class:     nl_melp_qjs_JNI
 * Method:    _compile
 * Signature: (J[B[B)Z
 */
JNIEXPORT jboolean JNICALL Java_nl_melp_qjs_JNI__1compile
  (JNIEnv *, jclass, jlong, jbyteArray, jbyteArray);

#ifdef __cplusplus
}
#endif
#endif
