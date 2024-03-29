#!/bin/bash
##
## This file is part of https://github.com/drm/java-quick-js. Refer to the
## project page for licensing and documentation.
##
## (c) Copyright 2023, Gerard van Helden
##

set -euo pipefail

export ROOT; ROOT="$(cd "$(dirname "$0")" && pwd)"
export DEBUG="${DEBUG:-0}"
export JAVAC; JAVAC="$(which javac)"
export JAVA; JAVA="$(which java)"
export JAVA_HOME; JAVA_HOME="$($JAVA -XshowSettings:properties -version 2>&1 > /dev/null | awk -F '=' '/java.home/ { print $2 }')"
export CC; CC="$(which g++)"
export OUTPUT_JAVAC="$ROOT/out/java"
export OUTPUT_SO="$ROOT/out/libquickjs.so"
export QJS_HOME="$ROOT/../quickjs"
export QJS_LIB="$QJS_HOME/libquickjs.a"

if ! [ -d "$QJS_HOME" ]; then
	echo "QJS_HOME=$QJS_HOME does not exist."
	echo "Please clone https://github.com/drm/quickjs at the specified location and try again"
	exit;
fi

if [ "${DEBUG:-}" -gt 0 ]; then
	if [ "${DEBUG:-}" -gt 1 ]; then
		set -x
	fi
	echo ""
	echo "env vars:"
	echo "-----------------------------------------"
	env
	echo "-----------------------------------------"
	echo ""
fi

build--qjs-static() {
	(
		cd $QJS_HOME
		make
	)
}

build--java() {
	find src/java test/java -name "*.java" -exec "$JAVAC" -h $ROOT/src/cpp -d "$OUTPUT_JAVAC" '{}' +
}

build--so() {
	local flags="";
	if [ "$DEBUG" -gt 0 ]; then
		flags="-g"; # overwrites default
	fi;

	mkdir -p "$ROOT/out";
	"$CC" \
		-Wall \
		-shared \
		$flags \
		-I $JAVA_HOME/include/ \
		-I $JAVA_HOME/include/linux/ \
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
	( cd "$QJS_HOME" && make clean )
	rm -rf "$ROOT/out";
}

build--all() {
	build--qjs-static
	build--java
	build--so
	build--runtest
}

build--package() {
	VERSION="$(git describe)"

	build--clean
	build--all
	( cd out/java && jar cf ../java-quick-js-$VERSION.jar $(find . -name "*.class"))

	echo out/java-quick-js-$VERSION.jar written.
	echo ""
}

if [ "$#" -eq 0 ]; then
	build--all;
else
	for a in "$@"; do
		build--$a;
	done;
fi
