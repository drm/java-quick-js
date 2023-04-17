package nl.melp.qjs;

class JNI {
	static {
		System.loadLibrary("quickjs");
	}

	static native byte[] _eval(long ctx, byte[] code);
	static native byte[] _evalPath(long ctx, byte[] pathname);
	static native long _createRuntime();
	static native void _destroyRuntime(long rt);

	static native long _createContext(long rt);
	static native void _destroyContext(long ctx);

	static native long _duplicateContext(long ctx);

	static native byte[] _evalBinaryPath(long ctx, byte[] bytes);

	static native boolean _compile(long ctx, byte[] srcpath, byte[] target);
}
