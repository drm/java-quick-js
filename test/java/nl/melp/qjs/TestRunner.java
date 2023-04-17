package nl.melp.qjs;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

public class TestRunner {
	private static final int debugLevel;
	static {
		int d;
		try {
			d = Integer.parseInt(System.getenv("DEBUG"), 10);
		} catch (NumberFormatException | NullPointerException e ) {
			d = 0;
		}
		debugLevel = d;
	}
	private static Class[] testClasses = new Class[]{
		QuickjsTest.class
	};

	private static List<Method> tests = new LinkedList<>();

	static {
		for (Class<?> c : testClasses) {
			for (Method m : c.getDeclaredMethods()) {
				if (m.getName().startsWith("test") && Modifier.isPublic(m.getModifiers())) {
					tests.add(m);
				}
			}
		}
	}

	public static void main(String[] args) throws InterruptedException {
		if (debugLevel > 1) {
			System.err.println("Waiting a bit to have gdb attached...");
			Thread.sleep(5_000);
			System.err.println("Continuing.");
		}

		Function<Method, Boolean> filter = null;

		if (System.getenv("TESTS") != null) {
			String[] tests = System.getenv("TESTS").split("[ \n\r\t]");
			filter = (Method m) -> {
				for (String ptn : tests) {
					if (m.toString().contains(ptn)) {
						return true;
					}
				}
				return false;
			};
		}

		printAtlevel(0, "Running tests\n");

		int num_fails = 0;
		int num_runs = 0;
		List<Method> failed = new LinkedList<>();
		for (Method m : tests) {
			if (filter == null || filter.apply(m)) {
				if (!invokeTest(m, instantiateTest(m))) {
					num_fails ++;
					failed.add(m);
				}
				num_runs ++;
			}
		}

		printAtlevel(0, "\nDone (%d tests, %d failures).\n\n", num_runs, num_fails);
		if (num_fails > 0) {
			printAtlevel(0, "Failed tests:\n");
			for (Method m : failed) {
				printAtlevel(0, " - %s\n", m);
			}
		}
	}

	private static boolean invokeTest(Method m, Object test) {
		printAtlevel(1, "%s: ", m.getName());

		try {
			try {
				m.invoke(test);
			} catch (InvocationTargetException e) {
				printAtlevel(0, "F");
				if (debugLevel > 0) {
					e.getCause().printStackTrace();
				}

				printAtlevel(1, "Failed: %s\n", e.getCause().getMessage());
				return false;
			}
			printAtlevel(1, "OK\n");
			printAtlevel(0, ".");
			return true;
		} catch (Exception e) {
			printAtlevel(1, "EXCEPTION [" + e + "]\n");
			if (debugLevel > 0) {
				e.printStackTrace();
			}
			printAtlevel(0, "E");
		}
		return false;
	}

	private static void printAtlevel(int level, String theString, Object... args) {
		if (debugLevel == level) {
			System.out.printf(theString, args);
		}
	}

	private static Object instantiateTest(Method m) {
		try {
			return m.getDeclaringClass().getDeclaredConstructor().newInstance();
		} catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
			e.printStackTrace();
			System.err.println("Sorry, some internal f***-up, here. Aborting.");
			System.exit(42);
		}
		return null;
	}
}
