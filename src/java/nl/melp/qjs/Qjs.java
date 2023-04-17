/**
 * This file is part of https://github.com/drm/java-quick-js. Refer to the
 * project page for licensing and documentation.
 *
 * (c) Copyright 2023, Gerard van Helden
 */

package nl.melp.qjs;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
public class Qjs {
	private static byte[] toBytes(final String s) {
		return s.getBytes(StandardCharsets.UTF_8);
	}

	private static byte[] toBytes(final Path f) {
		return toBytes(f.toAbsolutePath().toString());
	}

	private static String fromBytes(final byte[] bytes) {
		if (bytes != null) {
			return new String(bytes, StandardCharsets.UTF_8);
		}
		return null;
	}

	private static void usage() {
		System.out.printf("""
			Usage:
			
			java -Djava.library.path=out -cp out/java %s [OPTIONS]
			
			With OPTIONS being (a combination of) the following:
			
				--run path/to/script.js
					
					Evaluate the script and print the resulting string.
				
				--eval 'js-code;'
					
					Evaluate the js-code and print the resulting string.
				
				--compile path/to/src.js path/to/src.js.bin
					
					Compile the bytecode for src.js and save it in src.js.bin
			""",
			Qjs.class.getCanonicalName()
		);
	}

	public static void main(String[] args) throws Exception {
		if (args.length == 0) {
			usage();
		} else {
			try {
				for (int i = 0; i < args.length; i ++) {
					String s = args[i];
					switch (s) {
						case "--run":
							System.out.println(Qjs.evalPath(Path.of(args[++i])));
							break;
						case "--eval":
							System.out.println(Qjs.eval(args[++i]));
							break;
						case "--compile": {
							final Path src = Path.of(args[++i]);
							final Path tgt = Path.of(args[++i]);
							System.out.println(Qjs.compile(src, tgt));
							break;
						}
					}
				}
			} catch (IndexOutOfBoundsException e) {
				usage();
			}
		}
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
			return fromBytes(JNI._eval(ctx, toBytes(code)));
		}

		public String evalPath(Path pathname) {
			return fromBytes(JNI._evalPath(ctx, pathname.toAbsolutePath().toString().getBytes(StandardCharsets.UTF_8)));
		}

		public void close() {
			JNI._destroyContext(ctx);
		}

		public String evalBinaryPath(Path pathname) {
			return fromBytes(JNI._evalBinaryPath(ctx, pathname.toAbsolutePath().toString().getBytes(StandardCharsets.UTF_8)));
		}

		public boolean compile(Path src, Path tgt) {
			return JNI._compile(ctx, toBytes(src), toBytes(tgt));
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

	public static String evalPath(Path s) throws Exception {
		try (Runtime r = createRuntime()) {
			try (Context c = r.createContext()) {
				return c.evalPath(s);
			}
		}
	}

	public static boolean compile(Path src, Path tgt) throws Exception {
		try (Runtime r = createRuntime()) {
			try (Context c = r.createContext()) {
				return c.compile(src, tgt);
			}
		}
	}
}
