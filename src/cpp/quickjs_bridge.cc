/**
 * This file is part of https://github.com/drm/java-quick-js. Refer to the
 * project page for licensing and documentation.
 *
 * (c) Copyright 2023, Gerard van Helden
 */

#include "quickjs_bridge.h"
#include "quickjs-libc.h"
#include <cassert>

// TODO these need to be configurable
int dump_memory = 1; // boolean
size_t memory_limit = 1024 * 1024 * 40; // number
size_t stack_size = 0; // number
int dump_unhandled_promise_rejection = 1;
int num_calls = 0;

static void error(const char *msg) {
	fprintf(stderr, msg);
}

JSRuntime *create_runtime()
{
	JSRuntime *rt = JS_NewRuntime();

	if (!rt) {
		fprintf(stderr, "qjs: cannot allocate JS runtime\n");
		exit(2);
	}

	if (memory_limit != 0)
		JS_SetMemoryLimit(rt, memory_limit);

	if (stack_size != 0)
		JS_SetMaxStackSize(rt, stack_size);

	js_std_set_worker_new_context_func(create_context);
	js_std_init_handlers(rt);

	return rt;
}

void destroy_runtime(JSRuntime *rt) {
	js_std_free_handlers(rt);
	JS_FreeRuntime(rt);
}

/* also used to initialize the worker context */
JSContext *create_context(JSRuntime *rt)
{
	JSContext *ctx;
	ctx = JS_NewContext(rt);

	if (!ctx) {
		error("qjs: cannot allocate JS context\n");
		return NULL;
	}

	return ctx;
}

JSContext *duplicate_context(JSContext *ctx) {
	JSContext *dup = JS_DupContext(ctx);

	if (!dup) {
		error("qjs: cannot allocate JS context\n");
		return NULL;
	}

	return dup;
}

void destroy_context(JSContext *ctx) {
	JS_FreeContext(ctx);
}

bool write_bytecode(JSContext *ctx, JSValue obj, char *tgt_path) {
	assert(sizeof(uint8_t) == 1);
	FILE *fp = fopen(tgt_path, "w");
	if (!fp) {
		error("Can't write to file");
		return false;
	}

	uint8_t *out_buf;
	size_t out_buf_len;
	out_buf = JS_WriteObject(ctx, &out_buf_len, obj, JS_WRITE_OBJ_BYTECODE);
	if (!out_buf) {
		js_std_dump_error(ctx);
		return false;
	}
	size_t written = fwrite(out_buf, sizeof(uint8_t), out_buf_len, fp);
	if (written != out_buf_len) {
		error("Written bytes doesn't match size??");
	}
	fclose(fp);
	js_free(ctx, out_buf);
	return true;
}
