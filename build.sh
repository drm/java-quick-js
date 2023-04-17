#!/bin/bash

set -euo pipefail

export ROOT="$(cd "$(dirname "$0")" && pwd)"
export DEBUG="${DEBUG:-0}"
export JAVAC="$(which javac)"
export JAVA="$(which java)"
export CC="$(which g++)"
export OUTPUT_JAVAC="$ROOT/out/java"
export OUTPUT_SO="$ROOT/out/libquickjs.so"
export QJS_HOME="/home/gerard/git/bellard/quickjs"
export QJS_LIB="$QJS_HOME/libquickjs.a"

if [ "${DEBUG:-}" -gt 0 ]; then
	if [ "${DEBUG:-}" -gt 1 ]; then
		set -x
	fi
	echo "env vars:"
	env | grep -P '^(DEBUG|JAVAC|JAVA)='
	echo "-------"
	echo ""
fi

build--java() {
	find src/java test/java -name "*.java" -exec "$JAVAC" -h $ROOT/src/cpp -d "$OUTPUT_JAVAC" '{}' +
}

build--so() {
	local flags="";
	if [ "$DEBUG" -gt 0 ]; then
		flags="-g -DNDEBUG";
	fi;

	mkdir -p "$ROOT/out";
	"$CC" \
		-Wall \
		-shared \
		$flags \
		-I /usr/lib/jvm/java-17-openjdk-amd64/include/ \
		-I /usr/lib/jvm/java-17-openjdk-amd64/include/linux/ \
		-I $QJS_HOME/ \
		-I "$ROOT/src/cpp" \
		-L $QJS_HOME \
		-o $OUTPUT_SO \
		-fPIC \
		"$ROOT/src/cpp/jni_helper.cc" \
		"$ROOT/src/cpp/quickjs_bridge.cc" \
		"$ROOT/src/cpp/quickjs_jni_impl.cc" \
		-ldl \
		-lpthread \
		-lquickjs

	echo "$OUTPUT_SO written"
}

build--runtest() {
	run="$JAVA -Djava.library.path=$ROOT/out -cp "$OUTPUT_JAVAC" nl.melp.qjs.TestRunner"
	if [ $DEBUG -gt 1 ]; then
		$run &
		pid=$!
		gdb -p $pid
	else
		$run
	fi
}

build--clean() {
	rm -rf "$ROOT/out";
}

build--all() {
	build--clean
	build--java
	build--so
	build--runtest
}

if [ "$#" -eq 0 ]; then
	build--all;
else
	for a in "$@"; do
		build--$a;
	done;
fi
