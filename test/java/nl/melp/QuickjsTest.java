package nl.melp;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

@SuppressWarnings("unused")
public class QuickjsTest {

	private static final int num_runs = 1_000_000;

	private static void assertEquals(Object x, Object y) {
		if (x == null && y == null) {
			return;
		}
		if (x == null) {
			throw new AssertionError("Assertion failed, null != "  + y.getClass());
		}
		if (y == null) {
			throw new AssertionError("Assertion failed, " + x.getClass() + " != null");
		}
		if (!x.getClass().equals(y.getClass())) {
			throw new AssertionError("Assertion failed, types differ: " + x.getClass() + " != " + y.getClass());
		}
		if (x.getClass().equals(String.class)) {
			if (!x.equals(y)) {
				throw new AssertionError(String.format("Assertion failed, '%s' != '%s'", x, y));
			}
		}
	}

	public void mtestJni() throws IOException {
		assertEquals(null, Quickjs.eval("JSON"));
		assertEquals(null, Quickjs.eval(""));
		assertEquals("object", Quickjs.eval("typeof JSON;"));
		assertEquals("function", Quickjs.eval("typeof JSON.stringify;"));
		assertEquals("string", Quickjs.eval("typeof JSON.stringify({});"));
		assertEquals("Hello", Quickjs.eval("'Hello'"));
		assertEquals("blu", Quickjs.eval("\"blahbluh\".match(/b../g)[1]"));
	}

	public void testReact() throws IOException {
		assertEquals(
			"<body bgcolor=\"red\">Hello world</body>",
			Quickjs.eval(
				"ReactDOMServer.renderToStaticMarkup(React.createElement('body', {bgcolor: 'red'}, ['Hello world']));"
			)
		);
	}

	public void testPerf() throws IOException {
		long t_start = System.nanoTime();
		for (int i = 0; i < num_runs; i ++) {
			System.out.println(Quickjs.eval(
				"ReactDOMServer.renderToStaticMarkup(React.createElement('body', {bgcolor: 'red'}, ['Hello world']));"
			));
		}
		final long dur_ns = (System.nanoTime() - t_start);
		System.out.printf("Num runs: %d, total runtime: %.2f s, avg: %.2f ms/c\n", num_runs, dur_ns / 1_000_000_000F, dur_ns / 1_000_000F / num_runs);
	}
}
