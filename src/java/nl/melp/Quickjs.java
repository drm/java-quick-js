package nl.melp;

import java.nio.charset.StandardCharsets;

public class Quickjs {
	static {
		System.loadLibrary("quickjs");
	}

	public static String eval(String code) {
//		System.out.println("----");
//		System.out.println(code);
//		System.out.println("----");

		final byte[] bytes = _eval(code.getBytes(StandardCharsets.UTF_8));
		if (bytes != null) {
			return new String(bytes, StandardCharsets.UTF_8);
		}
		return null;
	}
	public static native byte[] _eval(byte[] code);
}
