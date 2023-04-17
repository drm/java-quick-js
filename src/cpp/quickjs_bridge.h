#ifndef __INCLUDED_QUICKJS_BRIDGE_H
#define __INCLUDED_QUICKJS_BRIDGE_H

#include "quickjs-libc.h"

JSRuntime *create_runtime();
JSContext *create_context(JSRuntime *);
void destroy_runtime(JSRuntime*);
void destroy_context(JSContext*);
JSContext *duplicate_context(JSContext*);

#endif
