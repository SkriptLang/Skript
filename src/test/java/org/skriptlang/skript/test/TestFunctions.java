package org.skriptlang.skript.test;

import ch.njol.skript.lang.function.Functions;
import org.skriptlang.skript.addon.SkriptAddon;
import org.skriptlang.skript.common.function.DefaultFunction;

import java.io.File;

/**
 * Functions available only to testing scripts.
 */
public class TestFunctions {

	public static void register(SkriptAddon addon) {
		Functions.register(DefaultFunction.builder(addon, "case_equals", Boolean.class)
			.description("Checks if the contents of a list of strings are strictly equal with case sensitivity.")
			.examples("caseEquals(\"hi\", \"Hi\") = false",
				"caseEquals(\"text\", \"text\", \"text\") = true",
				"caseEquals({some list variable::*})")
			.since("2.5")
			.parameter("strings", String[].class)
			.build(args -> {
				String[] strings = args.get("strings");
				for (int i = 0; i < strings.length - 1; i++) {
					if (!strings[i].equals(strings[i + 1])) {
						return false;
					}
				}
				return true;
			}));

		Functions.register(DefaultFunction.builder(addon, "line_separator", String.class)
			.description("""
				Returns the real (platform-dependent) line separator: \
				typically line feed for UN*X or carriage return line feed for Windows.
				""")
			.examples("broadcast \"hello\" + line_separator() + \"world\"")
			.since("2.10")
			.build(ignored -> System.lineSeparator()));

		Functions.register(DefaultFunction.builder(addon, "file_separator", String.class)
			.description("Returns the real (platform-dependent) file separator: typically / for UN*X or \\ for Windows.")
			.examples("file_separator() = \"/\"")
			.since("2.10")
			.build(ignored -> File.separator));
	}

}
