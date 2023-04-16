#!/bin/bash

set -euo pipefail

export ROOT="$(cd "$(dirname "$0")" && pwd)"
export DEBUG="${DEBUG:-0}"
export JAVAC="$(which javac)"
export JAVA="$(which java)"
export CC="$(which g++)"
export OUTPUT_JAVAC="$ROOT/out/java"
export OUTPUT_SO="$ROOT/out/libquickjs.so"

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
	find src/java test/java -name "*.java" -exec "$JAVAC" -d "$OUTPUT_JAVAC" '{}' +
}

build--so() {
	mkdir -p "$ROOT/out";
	"$CC" \
		-shared  \
		-I /usr/lib/jvm/java-17-openjdk-amd64/include/ \
		-I /usr/lib/jvm/java-17-openjdk-amd64/include/linux/ \
		-I "$ROOT/src/cpp" \
		-L . \
		-o $OUTPUT_SO \
		"$ROOT/src/cpp/quickjs_jni.cc"
	echo "$OUTPUT_SO written"
}

build--runtest() {
	"$JAVA" -Djava.library.path=$ROOT/out -cp "$OUTPUT_JAVAC" nl.melp.TestRunner
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