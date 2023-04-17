#include <jni.h>
#include <stdlib.h>
#include <string.h>

char *allocate_cstring(JNIEnv *env, jbyteArray string) {
	jboolean is_copy = false;
	jint length = env->GetArrayLength(string);
	jbyte *bytebuffer = env->GetByteArrayElements(string, &is_copy);
	char *str = (char *)malloc((size_t)length + 1);
	memcpy(str, bytebuffer, length);
	str[length] = '\0'; // see https://github.com/bellard/quickjs/issues/176
	env->ReleaseByteArrayElements(string, bytebuffer, JNI_ABORT);
	return str;
}
