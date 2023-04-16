package nl.melp;

import java.nio.charset.StandardCharsets;

@SuppressWarnings("unused")
public class QuickjsTest {

	private static void assertEqual(Object x, Object y) {
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
			throw new AssertionError("");
		}
	}

	public void testJni() {
//		assertEqual(null, Quickjs.eval(new byte[]{}, new byte[]{}));
		assertEqual(null, Quickjs.eval("Hello".getBytes(StandardCharsets.UTF_8), new byte[]{}));
	}
}
