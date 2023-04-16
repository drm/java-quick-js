#include <jni.h>
#include <stdio.h>
#include <stdlib.h>
#include <string>

#include "nl_melp_Quickjs.h"

JNIEXPORT jbyteArray JNICALL Java_nl_melp_Quickjs_eval  (JNIEnv *env , jclass cls, jbyteArray js_code, jbyteArray js_context) {
	if (js_code == NULL) {
		return NULL;
	}
	jsize length = env->GetArrayLength(js_code);
	if (length == 0) {
		return NULL;
	}
	jboolean is_copy = false;
	jbyte *bytebuffer = env->GetByteArrayElements(js_code, &is_copy);
//	std::string str((char *)bytebuffer, (size_t)length);
//	printf("We got input :)\n\n\t\t\t---\n%s\n\t\t\t---\n", str.c_str());
	env->ReleaseByteArrayElements(js_code, bytebuffer, JNI_ABORT);

	return NULL;
}
