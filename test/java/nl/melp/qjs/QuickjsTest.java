package nl.melp.qjs;


import nl.melp.qjs.Qjs.Context;
import nl.melp.qjs.Qjs.Runtime;

import java.nio.file.Path;

import static nl.melp.qjs.Assert.assertEquals;

@SuppressWarnings("unused")
public class QuickjsTest {

	private static final int num_runs = 100;

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
						System.out.println(c.eval(
							"ReactDOMServer.renderToStaticMarkup(React.createElement('body', {bgcolor: 'red'}, ['Hello world']));"
						));
					}
				}
			}
		}
		final long dur_ns = (System.nanoTime() - t_start);
		System.out.printf("Num runs: %d, total runtime: %.2f s, avg: %.2f ms/c\n", num_runs, dur_ns / 1_000_000_000F, dur_ns / 1_000_000F / num_runs);
	}
}
