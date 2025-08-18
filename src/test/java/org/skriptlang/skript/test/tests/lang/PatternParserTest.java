package org.skriptlang.skript.test.tests.lang;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptAPIException;
import ch.njol.skript.test.runner.SkriptJUnitTest;
import ch.njol.util.StringUtils;
import org.junit.Assert;
import org.junit.Test;
import org.skriptlang.skript.lang.util.PatternParser;
import org.skriptlang.skript.registration.SyntaxInfo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PatternParserTest extends SkriptJUnitTest {

	private static String regexPattern(String pattern) {
		return pattern.replaceAll("%\\S+%", "%*%")
			.replaceAll("[a-zA-Z0-9]+:", "")
			.replaceAll(":", "")
			.replaceAll("[0-9]+¦", "");
	}

	@Test
	public void test() {
		Assert.assertEquals(
			new PatternParser(regexPattern("[all [of the]|the] entities [of %-world%]")).getCombinations(),
			Set.of(
				"all entities", "all entities of %*%",
				"all of the entities", "all of the entities of %*%",
				"the entities", "the entities of %*%",
				"entities", "entities of %*%"
			)
		);

		Assert.assertEquals(
			new PatternParser(regexPattern("[all [of the]|the] [:typed] entities [of %-world%]")).getCombinations(),
			Set.of(
				"all typed entities", "all typed entities of %*%",
				"all entities", "all entities of %*%",
				"all of the typed entities", "all of the typed entities of %*%",
				"all of the entities", "all of the entities of %*%",
				"the typed entities", "the typed entities of %*%",
				"the entities", "the entities of %*%",
				"typed entities", "typed entities of %*%",
				"entities", "entities of %*%"
			)
		);

	}

	@Test
	public void testPatterns() {
		Map<String, Set<Class<?>>> registeredPatterns = new HashMap<>();
		Set<String> hasMultiple = new HashSet<>();

		Collection<SyntaxInfo<?>> elements = Skript.instance().syntaxRegistry().elements();
		Skript.adminBroadcast("Total elements: " + elements.size());
		int elementCounter = 0;
		int patternCounter = 0;
		int combinationCounter = 0;
		for (SyntaxInfo<?> syntaxInfo : elements) {
			Collection<String> patterns = syntaxInfo.patterns();
			Class<?> elementClass = syntaxInfo.type();

			elementCounter++;
			Skript.adminBroadcast("Element Counter: " + elementCounter);
			for (String pattern : patterns) {
				patternCounter++;
				Skript.adminBroadcast("Pattern Counter: " + patternCounter);
				Skript.adminBroadcast("Pattern: " + pattern);
				PatternParser parser = new PatternParser(regexPattern(pattern));
				for (String combination : parser.getCombinations()) {
					combinationCounter++;
					Skript.adminBroadcast("Combination Counter: " + combinationCounter);
					registeredPatterns.computeIfAbsent(combination, set -> new HashSet<>()).add(elementClass);
					if (registeredPatterns.get(combination).size() > 2)
						hasMultiple.add(combination);
				}
			}
		}

		if (hasMultiple.isEmpty())
			return;

		List<String> errors = new ArrayList<>();
		for (String string : hasMultiple) {
			List<String> names = registeredPatterns.get(string).stream()
				.map(Class::getCanonicalName)
				.toList();
			String error = "The pattern '" + string + "' conflicts in: " + StringUtils.join(names, ", ", ", and ");
			errors.add(error);
		}
		throw new SkriptAPIException(StringUtils.join(errors, "\n"));
	}

}
