#include <jni.h>
#include <stdio.h>
#include <stdlib.h>
#include <string>
#include <cstring>

#include "bytecode_combined.h"
#include "quickjs.h"
#include "quickjs-libc.h"

#include "nl_melp_Quickjs.h"

JSRuntime *rt;
JSContext *g_ctx;

int dump_memory = 1; // boolean
size_t memory_limit = 0; // number
size_t stack_size = 0; // number
int dump_unhandled_promise_rejection = 1;
int num_calls = 0;

/* also used to initialize the worker context */
static JSContext *JS_NewCustomContext(JSRuntime *rt)
{
	JSContext *ctx;
	ctx = JS_NewContext(rt);

	if (!ctx)
		return NULL;

	js_std_eval_binary(ctx, qjsc_combined, qjsc_combined_size, 0);
	return ctx;
}

jint JNI_OnLoad(JavaVM *vm, void *reserved)
{
	rt = JS_NewRuntime();
	g_ctx = JS_NewCustomContext(rt);

	if (!rt) {
		fprintf(stderr, "qjs: cannot allocate JS runtime\n");
		exit(2);
	}

	if (memory_limit != 0)
		JS_SetMemoryLimit(rt, memory_limit);

	if (stack_size != 0)
		JS_SetMaxStackSize(rt, stack_size);

    js_std_set_worker_new_context_func(JS_NewCustomContext);
    js_std_init_handlers(rt);

	return JNI_VERSION_1_1;
}

void JNI_OnUnload(JavaVM *vm, void *reserved)
{
	js_std_free_handlers(rt);
	JS_FreeRuntime(rt);
}

JNIEXPORT jbyteArray JNICALL Java_nl_melp_Quickjs__1eval(JNIEnv *env, jclass cls, jbyteArray js_code) {
	if (js_code == NULL) {
		return NULL;
	}
	jsize length = env->GetArrayLength(js_code);
	if (length == 0) {
		return NULL;
	}
	jboolean is_copy = false;
	jbyte *bytebuffer = env->GetByteArrayElements(js_code, &is_copy);
	char *str = (char *)malloc((size_t)length + 1);
	memcpy(str, bytebuffer, length);
	str[length] = '\0'; // see https://github.com/bellard/quickjs/issues/176

	JSContext *ctx;

// strategy1: create new context for every render:
// 	ctx = JS_NewCustomContext(rt);

// strategy 2: create new context every 500 renders :
//	ctx = g_ctx;
//	if (num_calls++ > 500) {
//		num_calls = 0;
//		if (ctx != NULL) {
//			JS_FreeContext(ctx);
//			ctx = g_ctx = JS_NewCustomContext(rt);
//		}
//	}

// strategy 3: duplicate context for every render:
	ctx = JS_DupContext(g_ctx);

// other strategies to explore: (a)synchronous worker pool

	if (!ctx) {
		fprintf(stderr, "qjs: cannot allocate JS context\n");
		exit(2);
	}
	jbyteArray ret = NULL;
	{
		JSValue val;
		val = JS_Eval(ctx, str, length, "<jni>", JS_EVAL_TYPE_GLOBAL);
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
				env->SetByteArrayRegion (ret, 0, len, (jbyte*)buf);
				JS_FreeCString(ctx, buf);
			} else {
				fprintf(stderr, "NOTICE: ignoring return value from script; currently only strings are supported. Returning NULL.\n");
			}
		}
		JS_FreeValue(ctx, val);
	}

	env->ReleaseByteArrayElements(js_code, bytebuffer, JNI_ABORT);

	return ret;
}

