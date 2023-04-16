package nl.melp;

public class Quickjs {
	static {
		System.loadLibrary("quickjs");
	}
	public static native byte[] eval(byte[] code, byte[] context);
}
