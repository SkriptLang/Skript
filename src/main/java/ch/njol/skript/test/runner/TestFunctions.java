package ch.njol.skript.test.runner;

import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.lang.function.Functions;
import ch.njol.skript.lang.function.Parameter;
import ch.njol.skript.lang.function.SimpleJavaFunction;
import ch.njol.skript.registrations.DefaultClasses;

/**
 * Functions available only to testing scripts.
 */
@SuppressWarnings("null") // DefaultFunctions has this too... not ideal
public class TestFunctions {
	
	static {
		ClassInfo<String> stringClass = DefaultClasses.STRING;
		Parameter<?>[] stringsParam = new Parameter[] {new Parameter<>("strs", stringClass, false, null)};
		
		Functions.registerFunction(new SimpleJavaFunction<Boolean>("caseEquals", stringsParam, DefaultClasses.BOOLEAN, true) {
			@Override
			public Boolean[] executeSimple(final Object[][] params) {
				Object[] strs = params[0];
				for (int i = 0; i < strs.length - 1; i++)
					if (!strs[i].equals(strs[i+1]))
						return new Boolean[] {false};
				return new Boolean[] {true};
			}
		}.description("Checks if the contents of a list of strings are strictly equal with case sensitivity.")
			.examples("caseEquals(\"hi\", \"Hi\") = false",
						"caseEquals(\"text\", \"text\", \"text\") = true", 
						"caseEquals({some list variable::*})")
			.since("2.5"));
	}
	
}
