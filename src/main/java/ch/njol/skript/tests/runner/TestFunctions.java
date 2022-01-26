/**
 *   This file is part of Skript.
 *
 *  Skript is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Skript is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Skript.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright Peter Güttinger, SkriptLang team and contributors
 */
package ch.njol.skript.tests.runner;

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
