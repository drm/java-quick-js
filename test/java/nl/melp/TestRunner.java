package nl.melp;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.LinkedList;
import java.util.List;

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

	public static void main(String[] args) {
		printAtlevel(0, "Running tests\n");
		for (Method m : tests) {
			invokeTest(m, instantiateTest(m));
		}
		printAtlevel(0, "Done.\n\n");
	}

	private static void invokeTest(Method m, Object test) {
		printAtlevel(1, m.getClass().getCanonicalName() + "." + m.getName() + ": ");

		try {
			m.invoke(test);
			printAtlevel(1, "OK\n");
		} catch (Exception e) {
			printAtlevel(1, "EXCEPTION [" + e + "]\n");
			throw new RuntimeException(e);
		}
	}

	private static void printAtlevel(int level, String theString, Object... args) {
		if (debugLevel >= level) {
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
