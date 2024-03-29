/**
 * This file is part of https://github.com/drm/java-quick-js. Refer to the
 * project page for licensing and documentation.
 *
 * (c) Copyright 2023, Gerard van Helden
 */

#ifndef __INCLUDED_QUICKJS_BRIDGE_H
#define __INCLUDED_QUICKJS_BRIDGE_H

#include "quickjs-libc.h"

JSRuntime *create_runtime();
JSContext *create_context(JSRuntime *);
void destroy_runtime(JSRuntime*);
void destroy_context(JSContext*);
JSContext *duplicate_context(JSContext*);
bool write_bytecode(JSContext *ctx, JSValue obj, char *tgt_path);

#endif
