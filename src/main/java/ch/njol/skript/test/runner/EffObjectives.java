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
package ch.njol.skript.test.runner;

import java.util.Arrays;
import java.util.List;

import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.NoDoc;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;

@Name("Objectives")
@Description("An effect to setup required objectives for JUnit tests to complete.")
@NoDoc
public class EffObjectives extends Effect  {

	static {
		if (TestMode.ENABLED)
			Skript.registerEffect(EffObjectives.class,
					"ensure [[junit] test] %string% completes [(objective|trigger)[s]] %strings%",
					"complete [(objective|trigger)[s]] %strings% (for|on) [[junit] test] %string%"
			);
	}

	private static final Multimap<String, String> requirements = HashMultimap.create();
	private static final Multimap<String, String> completeness = HashMultimap.create();

	private Expression<String> junit, objectives;
	private boolean setup;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		objectives = (Expression<String>) exprs[matchedPattern ^ 1];
		junit = (Expression<String>) exprs[matchedPattern];
		setup = matchedPattern == 0;
		return true;
	}

	@Override
	protected void execute(Event event) {
		String junit = this.junit.getSingle(event);
		assert junit != null;
		String[] objectives = this.objectives.getArray(event);
		assert objectives.length > 0;
		if (setup) {
			requirements.putAll(junit, Lists.newArrayList(objectives));
		} else {
			completeness.putAll(junit, Lists.newArrayList(objectives));
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		if (setup)
			return "ensure junit test " + junit.toString(event, debug) + " completes objectives " + objectives.toString(event, debug);
		return "complete objectives " + objectives.toString(event, debug) + " on junit test " + junit.toString(event, debug);
	}

	/**
	 * Complete a JUnit objective on the Java side.
	 * 
	 * @param test The test name used in the complete objectives string effect.
	 * @param objectives The objectives setup in the complete objectives string effect
	 */
	public static void complete(String test, String... objectives) {
		completeness.putAll(test, Lists.newArrayList(objectives));
	}

	/**
	 * Check if the currently running JUnit test has passed all
	 * it's required objectives that the script test setup.
	 * 
	 * @return boolean true if the test passed.
	 */
	public static boolean isJUnitComplete() {
		assert !completeness.isEmpty() || !requirements.isEmpty();
		return completeness.equals(requirements);
	}

	/**
	 * Fails the JUnit testing system if any JUnit tests did not complete their checks.
	 */
	public static void fail() {
		for (String test : requirements.keySet()) {
			if (!completeness.containsKey(test)) {
				TestTracker.JUnitTestFailed("JUnit: '" + test + "'", "didn't complete any objectives.");
				continue;
			}
			List<String> failures = Lists.newArrayList(requirements.get(test));
			failures.removeAll(completeness.get(test));
			if (!failures.isEmpty())
				TestTracker.JUnitTestFailed("JUnit: '" + test + "'", "failed objectives: " + Arrays.toString(failures.toArray(new String[0])));
		}
	}

}
