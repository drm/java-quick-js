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
