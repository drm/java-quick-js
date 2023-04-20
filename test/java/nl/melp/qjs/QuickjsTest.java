/**
 * This file is part of https://github.com/drm/java-quick-js. Refer to the
 * project page for licensing and documentation.
 *
 * (c) Copyright 2023, Gerard van Helden
 */

package nl.melp.qjs;

import nl.melp.qjs.Qjs.Context;
import nl.melp.qjs.Qjs.Runtime;

import java.nio.file.Path;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Supplier;

import static nl.melp.qjs.Assert.assertEquals;

@SuppressWarnings("unused")
public class QuickjsTest {
	private static final int num_runs;
	static {
		int n;
		if (System.getenv("NUM_RUNS") != null) {
			n = Integer.parseInt(System.getenv("NUM_RUNS"));
		} else {
			n = 100;
		}
		num_runs = n;
	}

	public void testJni() throws Exception {
		assertEquals(null, Qjs.eval("JSON"));
		assertEquals(null, Qjs.eval(""));
		assertEquals("object", Qjs.eval("typeof JSON;"));
		assertEquals("function", Qjs.eval("typeof JSON.stringify;"));
		assertEquals("string", Qjs.eval("typeof JSON.stringify({});"));
		assertEquals("Hello", Qjs.eval("'Hello'"));
		assertEquals("blu", Qjs.eval("\"blahbluh\".match(/b../g)[1]"));
		assertEquals(null, Qjs.eval("var global = this;"));
	}

	public void testReact() throws Exception {
		try (Runtime rt = Qjs.createRuntime()) {
			try (Context template = rt.createContext()) {
				template.evalPath(Path.of("resources/js/combined.js"));

				assertEquals(
					"<body bgcolor=\"red\">Hello world</body>",
					template.eval(
						"ReactDOMServer.renderToStaticMarkup(React.createElement('body', {bgcolor: 'red'}, ['Hello world']));"
					)
				);
			}
		}
	}

	public void testTemplatePerformance() throws Exception {
		long t_start = System.nanoTime();
		try (Runtime rt = Qjs.createRuntime()) {
			try (Context template = rt.createContext()) {
				template.evalPath(Path.of("resources/js/combined.js"));

				for (int i = 0; i < num_runs; i ++) {
					try (Context c = template.duplicate()) {
						assertEquals(
							"<body bgcolor=\"red\">Hello world</body>",
							c.eval(
								"ReactDOMServer.renderToStaticMarkup(React.createElement('body', {bgcolor: 'red'}, ['Hello world']));"
							)
						);
					}
				}
			}
		}
		final long dur_ns = (System.nanoTime() - t_start);
		System.out.printf("Num runs: %d, total runtime: %.2f s, avg: %.2f ms/c\n", num_runs, dur_ns / 1_000_000_000F, dur_ns / 1_000_000F / num_runs);
	}

	public void testEvalPath() throws Exception {
		try (Runtime rt = Qjs.createRuntime()) {
			try (Context c = rt.createContext()) {
				assertEquals("Hello world", c.evalPath(Path.of("resources/js/hello-world-string.js")));
			}
		}
	}

	public void testCompile() throws Exception {
		{
			final Path binPath = Path.of("resources/js/hello-world-string.js.bin");
			try (Runtime rt = Qjs.createRuntime()) {
				try (Context c = rt.createContext()) {
					c.compile(Path.of("resources/js/hello-world-string.js"), binPath);
				}
			}

			try (Runtime rt = Qjs.createRuntime()) {
				try (Context c = rt.createContext()) {
					assertEquals("Hello world", c.evalBinaryPath(binPath));
				}
			}
		}
		{
			final Path binPath = Path.of("resources/js/hello-world-fn.js.bin");
			try (Runtime rt = Qjs.createRuntime()) {
				try (Context c = rt.createContext()) {
					c.compile(Path.of("resources/js/hello-world-fn.js"), binPath);
				}
			}

			try (Runtime rt = Qjs.createRuntime()) {
				try (Context template = rt.createContext()) {
					template.evalBinaryPath(binPath);

					try (Context c = template.duplicate()) {
						assertEquals("Hello World", c.eval("fn(['Hello', 'World'].join(' '))"));
					}
				}
			}
		}
	}

	public void testRealworldPrecompiledTemplatePerformance() throws Exception {
		final Path srcFile = Path.of("resources/js/realworld/combined.js");
		if (!srcFile.toFile().exists()) {
			// this is an actual real world example that is not part of the public
			// repository.
			return;
		}

		long t_start = System.nanoTime();
		final Path binary = Path.of("resources/js/realworld/combined.js.bin");
		assertEquals(true, Qjs.compile(srcFile, binary));
		final long compilation_dur_ns = (System.nanoTime() - t_start);
		long total_duplication_ns = 0;
		long total_binary_eval_ns = 0;

		Runtime rt = null;
		Context template = null;
		int i = 0;
		do {
			if (rt == null) {
				rt = Qjs.createRuntime();
				template = rt.createContext();
				long t_pre = System.nanoTime();
				template.evalBinaryPath(binary);
				total_binary_eval_ns += System.nanoTime() - t_pre;
			}
			long t_pre = System.nanoTime();
			Context c = template.duplicate();
			total_duplication_ns += (System.nanoTime() - t_pre);
			c.evalPath(Path.of("resources/js/realworld/input.js"));
			c.close();
			if (i % 20 == 19) {
				template.close();
				rt.close();
				rt = null;
				template = null;
			}
		} while (i ++ < num_runs);
		if (rt != null) {
			template.close();
			rt.close();
		}

		final long dur_ns = (System.nanoTime() - t_start);
		System.out.println(total_duplication_ns);
		System.out.printf(
			"Num runs: %d, total runtime: %.2f s, compilation time: %.2f ms, duplication time: %.2f s, total_binary_eval_ns: %.2f s, avg(incl. compilation): %.2f ms/c, avg(without compilation): %.2f ms/c\n",
			num_runs,
			dur_ns / 1_000_000_000F,
			compilation_dur_ns / 1_000_000F,
			total_duplication_ns / 1_000_000_000F,
			total_binary_eval_ns / 1_000_000_000F,
			dur_ns / 1_000_000F / num_runs,
			(dur_ns - compilation_dur_ns) / 1_000_000F / num_runs
		);
	}

	public void testPrecompiledPoolPerformance() throws Exception {
		final Path srcFile = Path.of("resources/js/realworld/combined.js");
		if (!srcFile.toFile().exists()) {
			// this is an actual real world example that is not part of the public
			// repository.
			return;
		}

		class Pool<T extends AutoCloseable> {
			private final BlockingQueue<T> pool;
			private final ExecutorService producer;

			private boolean stop = false;

			Pool(Supplier<T> supplier, int size) {
				this.pool = new ArrayBlockingQueue<T>(size);
				this.producer = Executors.newSingleThreadExecutor();

				this.producer.submit(() -> {
					T item = supplier.get();
					do {
						try {
							if (pool.offer(item, 500, TimeUnit.MILLISECONDS)) {
								System.out.printf("offered and accepted at %d%n", pool.size());
								item = supplier.get();
							} else {
								System.out.printf("offered, but failed at %d, [stop=%b]%n", pool.size(), stop);
							}
						} catch (InterruptedException e) {
							stop = true;
						}
					} while (!stop);

					if (item != null) {
						try {
							item.close();
						} catch (Exception e) {
							e.printStackTrace();
							// continue anyway.
						}
					}

					do {
						item = pool.poll();
						if (item != null) {
							try {
								item.close();
							} catch (Exception e) {
								e.printStackTrace();
								// continue anyway.
							}
						}
					} while (item != null);
				});
			}

			<R> R with(Function<T, R> t) throws InterruptedException {
				try (T c = take()) {
					return t.apply(c);
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					throw new InterruptedException();
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}

			private T take() throws InterruptedException {
				try {
					return pool.take();
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					throw new InterruptedException();
				}
			}

			void close() throws Exception {
				this.stop = true;
				this.producer.shutdown();
				final boolean b = this.producer.awaitTermination(1, TimeUnit.SECONDS);
				if (!b) {
					throw new RuntimeException("Could not stop executor");
				}
				AutoCloseable c;
				while (null != (c = pool.poll())) {
					c.close();
				}
			}
		}

		class ContextPool implements AutoCloseable {
			private final Runtime runtime;
			private final Context template;
			private final Pool<Context> pool;

			public ContextPool() {
				this.runtime = Qjs.createRuntime();
				this.template = this.runtime.createContext();
				this.pool = new Pool<>(this::duplicateContext, 40);
				final Path binary = Path.of("resources/js/realworld/combined.js.bin");
				this.template.evalBinaryPath(binary);
			}

			private Context duplicateContext() {
				return this.template.duplicate();
			}

			@Override
			public void close() throws Exception {
				this.pool.close();
				this.template.close();
				this.runtime.close();
			}

			<R> R with(Function<Context, R> f) throws InterruptedException {
				return pool.with(f);
			}
		}

		try (var pool = new ContextPool()) {
			for (int i = 0; i < num_runs; i ++) {
				pool.with((c) -> c.evalPath(Path.of("resources/js/realworld/input.js")));
			}
		}

//		System.out.println(total_duplication_ns);
//		System.out.printf(
//			"Num runs: %d, total runtime: %.2f s, compilation time: %.2f ms, duplication time: %.2f s, total_binary_eval_ns: %.2f s, avg(incl. compilation): %.2f ms/c, avg(without compilation): %.2f ms/c\n",
//			num_runs,
//			dur_ns / 1_000_000_000F,
//			compilation_dur_ns / 1_000_000F,
//			total_duplication_ns / 1_000_000_000F,
//			total_binary_eval_ns / 1_000_000_000F,
//			dur_ns / 1_000_000F / num_runs,
//			(dur_ns - compilation_dur_ns) / 1_000_000F / num_runs
//		);
	}

	public void testPrecompiledTemplatePerformance() throws Exception {
		long t_start = System.nanoTime();
		final Path binary = Path.of("resources/js/combined.js.bin");
		assertEquals(true, Qjs.compile(Path.of("resources/js/combined.js"), binary));
		final long compilation_dur_ns = (System.nanoTime() - t_start);

		try (Runtime rt = Qjs.createRuntime()) {
			try (Context template = rt.createContext()) {
				template.evalBinaryPath(binary);

				for (int i = 0; i < num_runs; i ++) {
					try (Context c = template.duplicate()) {
						assertEquals(
							"<body bgcolor=\"red\">Hello world</body>",
							c.eval(
								"ReactDOMServer.renderToStaticMarkup(React.createElement('body', {bgcolor: 'red'}, ['Hello world']));"
							)
						);
					}
				}
			}
		}
		final long dur_ns = (System.nanoTime() - t_start);
		System.out.printf(
			"Num runs: %d, total runtime: %.2f s, compilation time: %.2f ms, avg(incl. compilation): %.2f ms/c, avg(without compilation): %.2f ms/c\n",
			num_runs,
			dur_ns / 1_000_000_000F,
			compilation_dur_ns / 1_000_000F,
			dur_ns / 1_000_000F / num_runs,
			(dur_ns - compilation_dur_ns) / 1_000_000F / num_runs
		);
	}
}
