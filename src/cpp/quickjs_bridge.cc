#include "quickjs_bridge.h"
#include "quickjs-libc.h"

int dump_memory = 1; // boolean
size_t memory_limit = 0; // number
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

//	js_std_eval_binary(ctx, qjsc_combined, qjsc_combined_size, 0);
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
