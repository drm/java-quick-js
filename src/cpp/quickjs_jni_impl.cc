#include <jni.h>
#include <stdio.h>
#include <string.h>

#include "jni_helper.h"
#include "quickjs_bridge.h"
#include "quickjs-libc.h"

extern "C" {

JNIEXPORT jint JNI_OnLoad(JavaVM *vm, void *reserved)
{
	return JNI_VERSION_1_1;
}

JNIEXPORT jbyteArray JNICALL Java_nl_melp_qjs_JNI__1eval (JNIEnv *env, jclass cls, jlong ctx_, jbyteArray js_code) {
	if (js_code == NULL) {
		return NULL;
	}
	JSContext *ctx = reinterpret_cast<JSContext *>(ctx_);

	jbyteArray ret = NULL;

	char *str = allocate_cstring(env, js_code);
	JSValue val = JS_Eval(ctx, str, strlen(str), "<jni>", JS_EVAL_TYPE_GLOBAL);
	free(str);

	if (JS_IsException(val)) {
		js_std_dump_error(ctx);
	} else {
		if (JS_IsNull(val)
			|| JS_IsUndefined(val)
			|| JS_IsUninitialized(val)
		) {
			// noop
		} else if (JS_IsString(val)) {
			const char *buf;
			size_t len = 0;
			JS_ToCStringLen(ctx, &len, val);
			buf = JS_ToCString(ctx, val);
			ret = env->NewByteArray(len);
			env->SetByteArrayRegion (ret, 0, len, const_cast<jbyte *>(reinterpret_cast<const jbyte*>(buf)));
			JS_FreeCString(ctx, buf);
		} else {
			fprintf(stderr, "NOTICE: ignoring return value from script; currently only strings are supported. Returning NULL.\n");
		}
	}

	JS_FreeValue(ctx, val);
	return ret;
}

JNIEXPORT jbyteArray JNICALL Java_nl_melp_qjs_JNI__1evalPath(JNIEnv *, jclass, jlong, jbyteArray) {

	return nullptr;
}

JNIEXPORT jlong JNICALL Java_nl_melp_qjs_JNI__1createRuntime(JNIEnv *a, jclass b) {
	return reinterpret_cast<jlong>(create_runtime());
}

JNIEXPORT void JNICALL Java_nl_melp_qjs_JNI__1destroyRuntime(JNIEnv *, jclass, jlong rt) {
	destroy_runtime(reinterpret_cast<JSRuntime *>(rt));
}

JNIEXPORT jlong JNICALL Java_nl_melp_qjs_JNI__1createContext(JNIEnv *, jclass, jlong rt) {
	return reinterpret_cast<jlong>(create_context(reinterpret_cast<JSRuntime *>(rt)));
}

JNIEXPORT void JNICALL Java_nl_melp_qjs_JNI__1destroyContext (JNIEnv *, jclass, jlong ctx) {
	destroy_context(reinterpret_cast<JSContext *>(ctx));
}

JNIEXPORT jlong JNICALL Java_nl_melp_qjs_JNI__1duplicateContext(JNIEnv *, jclass, jlong ctx) {
	return reinterpret_cast<jlong>(duplicate_context(reinterpret_cast<JSContext *>(ctx)));
}

} // extern "C"
