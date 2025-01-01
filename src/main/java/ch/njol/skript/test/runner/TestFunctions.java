package ch.njol.skript.test.runner;

import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.lang.function.Functions;
import ch.njol.skript.lang.function.Parameter;
import ch.njol.skript.lang.function.SimpleJavaFunction;
import ch.njol.skript.registrations.DefaultClasses;

import java.io.File;

/**
 * Functions available only to testing scripts.
 */
public class TestFunctions {

	static {
		if (TestMode.ENABLED) // Prevent accidental registration if something visits this class
			registerTestFunctions();
	}

	private static void registerTestFunctions() {

		ClassInfo<String> stringClass = DefaultClasses.STRING;
		Parameter<?>[] stringsParam = new Parameter[] {new Parameter<>("strs", stringClass, false, null)};

		Functions.registerFunction(new SimpleJavaFunction<>("case_equals", stringsParam, DefaultClasses.BOOLEAN,
			true) {
			@Override
			public Boolean[] executeSimple(final Object[][] params) {
				Object[] strs = params[0];
				for (int i = 0; i < strs.length - 1; i++)
					if (!strs[i].equals(strs[i + 1]))
						return new Boolean[] {false};
				return new Boolean[] {true};
			}
		}.description("Checks if the contents of a list of strings are strictly equal with case sensitivity.")
			.examples("caseEquals(\"hi\", \"Hi\") = false",
				"caseEquals(\"text\", \"text\", \"text\") = true",
				"caseEquals({some list variable::*})")
			.since("2.5"));

		Functions.registerFunction(new SimpleJavaFunction<>("line_separator", new Parameter[0],
			DefaultClasses.STRING, true) {
			@Override
			public String[] executeSimple(Object[][] params) {
				return new String[] {System.lineSeparator()};
			}
		}.description("""
				Returns the real (platform-dependent) line separator:
				typically line feed for UN*X or carriage return line feed for Windows.""")
			.examples("broadcast \"hello\" + line_separator() + \"world\"")
			.since("INSERT VERSION"));

		Functions.registerFunction(new SimpleJavaFunction<>("file_separator", new Parameter[0],
			DefaultClasses.STRING, true) {
			@Override
			public String[] executeSimple(Object[][] params) {
				return new String[] {File.separator};
			}
		}.description("Returns the real (platform-dependent) file separator: typically / for UN*X or \\ for Windows.")
			.examples("file_separator() = \"/\"")
			.since("INSERT VERSION"));

	}

}
