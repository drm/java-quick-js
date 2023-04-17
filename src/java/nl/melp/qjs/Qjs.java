package nl.melp.qjs;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

public class Qjs {
	public static void main(String[] args) {
		// implement compilation to custom bytecode format.
		// command line interface for evaluation
	}

	public static class Runtime implements AutoCloseable {
		long rt;

		protected Runtime(long rt) {
			this.rt = rt;
		}

		@Override
		public void close() throws Exception {
			JNI._destroyRuntime(rt);
		}

		public Context createContext() {
			return new Context(rt, JNI._createContext(rt));
		}
	}

	public static class Context implements AutoCloseable {
		private final long rt;
		private final long ctx;

		Context(long rt, long ptr) {
			this.rt = rt;
			this.ctx = ptr;
		}

		public Context duplicate() {
			return new Context(rt, JNI._duplicateContext(ctx));
		}

		public String eval(String code) {
			return handleBytes(JNI._eval(ctx, code.getBytes(StandardCharsets.UTF_8)));
		}

		private static String handleBytes(final byte[] bytes) {
			if (bytes != null) {
				return new String(bytes, StandardCharsets.UTF_8);
			}
			return null;
		}


		public String evalPath(Path pathname) {
			return handleBytes(JNI._evalPath(ctx, pathname.toAbsolutePath().toString().getBytes(StandardCharsets.UTF_8)));
		}

		public void close() {
			JNI._destroyContext(ctx);
		}
	}

	public static Runtime createRuntime() {
		return new Runtime(JNI._createRuntime());
	}


	public static String eval(String s) throws Exception {
		try (Runtime r = createRuntime()) {
			try (Context c = r.createContext()) {
				return c.eval(s);
			}
		}
	}
}
