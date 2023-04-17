/**
 * This file is part of https://github.com/drm/java-quick-js. Refer to the
 * project page for licensing and documentation.
 *
 * (c) Copyright 2023, Gerard van Helden
 */

package nl.melp.qjs;
public class Assert {
	public static void assertEquals(Object x, Object y) {
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

}
