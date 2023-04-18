package nl.melp.qjs;

import nl.melp.qjs.Qjs.Context;
import nl.melp.qjs.Qjs.Runtime;

import java.nio.file.Path;
import java.util.Scanner;

@SuppressWarnings("unused")
public class Regression {
	public void testNonExistentFileReturnsNull() throws Exception {
		try (Runtime rt = Qjs.createRuntime()) {
			try (Context ctx = rt.createContext()) {
				// threw SIGSEGV prior to the fix.
				Assert.assertEquals(null, ctx.evalPath(Path.of("does not exist")));
			}
		}
	}
}
