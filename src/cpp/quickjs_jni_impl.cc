#include <jni.h>
#include <stdio.h>
#include <string.h>
#include <cassert>

#include "jni_helper.h"
#include "quickjs_bridge.h"
#include "quickjs-libc.h"

jbyteArray handle_return(JNIEnv *env, JSContext *ctx, JSValue val) {
	jbyteArray ret = nullptr;
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

	char *str = allocate_cstring(env, js_code);
	JSValue val = JS_Eval(ctx, str, strlen(str), "<jni>", JS_EVAL_TYPE_GLOBAL);
	free(str);

	return handle_return(env, ctx, val);
}

JNIEXPORT jbyteArray JNICALL Java_nl_melp_qjs_JNI__1evalPath(JNIEnv *env, jclass, jlong ctx_, jbyteArray path) {
	JSContext *ctx = reinterpret_cast<JSContext *>(ctx_);

	size_t buf_len = 0;
    uint8_t *buf;
	char *str_path = allocate_cstring(env, path);
    buf = js_load_file(ctx, &buf_len, str_path);
	JSValue val = JS_Eval(ctx, reinterpret_cast<const char *>(buf), buf_len, str_path, JS_EVAL_TYPE_GLOBAL);

	free(str_path);
	js_free(ctx, buf);

	return handle_return(env, ctx, val);
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

JNIEXPORT jbyteArray JNICALL Java_nl_melp_qjs_JNI__1evalBinaryPath(JNIEnv *env, jclass, jlong ctx_, jbyteArray path) {
	assert(sizeof(uint8_t) == 1);
	JSContext *ctx = reinterpret_cast<JSContext*>(ctx_);

	char *bin_path = allocate_cstring(env, path);

	FILE *fd = fopen(bin_path, "rb");
	fseek(fd, 0, SEEK_END);
	long fsize = ftell(fd);
	fseek(fd, 0, SEEK_SET);
	uint8_t *string = (uint8_t*)malloc(fsize + 1);
	fread(string, fsize, 1, fd);
	fclose(fd);

    JSValue obj;
    obj = JS_ReadObject(ctx, string, fsize, JS_READ_OBJ_BYTECODE);
    jbyteArray ret = nullptr;
    if (JS_IsException(obj)) {
    	js_std_dump_error(ctx);
    } else {
    	JSValue val = JS_EvalFunction(ctx, obj);
    	ret = handle_return(env, ctx, val);
    }
// Not sure why this triggers a segfault.
//    JS_FreeValue(ctx, obj);

  	free(bin_path);
  	free(string);

  	return ret;
}

JNIEXPORT void JNICALL Java_nl_melp_qjs_JNI__1compile(JNIEnv *env, jclass, jlong ctx_, jbyteArray src, jbyteArray tgt) {
	JSContext *ctx = reinterpret_cast<JSContext *>(ctx_);

	char *src_path = allocate_cstring(env, src);
	char *tgt_path = allocate_cstring(env, tgt);

	size_t buf_len = 0;
    uint8_t *buf;
    buf = js_load_file(ctx, &buf_len, src_path);
	JSValue val = JS_Eval(ctx, reinterpret_cast<const char *>(buf), buf_len, src_path, JS_EVAL_TYPE_GLOBAL | JS_EVAL_FLAG_COMPILE_ONLY);
	free(buf);

	write_bytecode(reinterpret_cast<JSContext *>(ctx_), val, tgt_path);

	JS_FreeValue(ctx, val);
	free(src_path);
	free(tgt_path);
}

} // extern "C"
