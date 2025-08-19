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
		return pattern.replaceAll("%[^%()\\[\\]|*]+%", "%*%") // Replaces expressions, except literals
			.replaceAll("[a-zA-Z0-9]+:", "") // Replaces parse tags with leading ID 'any:'
			.replaceAll(":", "") // Replaces ':' for parse tags with trailing ID ':any'
			.replaceAll("[0-9]+¦", ""); // Replaces parse marks '1¦'
	}

	private static void compare(Set<String> got, Set<String> expect) {
		if (!expect.equals(got)) {
			Set<String> gotCopy = new HashSet<>(Set.copyOf(got));
			gotCopy.removeAll(expect);
			if (!gotCopy.isEmpty()) {
				Assert.fail("Unexpected combinations: " + gotCopy);
			}
			Set<String> expectCopy = new HashSet<>(Set.copyOf(expect));
			expectCopy.removeAll(got);
			if (!expectCopy.isEmpty()) {
				Assert.fail("Combinations not found: " + expectCopy);
			}
		}
	}

	@Test
	public void test() {
		Assert.assertEquals(
			regexPattern("[all [of the]|the] entities [of %-world%]"),
			"[all [of the]|the] entities [of %*%]"
		);
		compare(
			new PatternParser(regexPattern("[all [of the]|the] entities [of %-world%]")).getCombinations(),
			Set.of(
				"all entities", "all entities of %*%",
				"all of the entities", "all of the entities of %*%",
				"the entities", "the entities of %*%",
				"entities", "entities of %*%"
			)
		);

		Assert.assertEquals(
			regexPattern("[all [of the]|the] [:typed] entities [of %-world%]"),
			"[all [of the]|the] [typed] entities [of %*%]"
		);
		compare(
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

		Assert.assertEquals(
			regexPattern("stop (all:all sound[s]|sound[s] %-strings%) [(in [the]|from) %-soundcategory%] [(from playing to|for) %players%]"),
			"stop (all sound[s]|sound[s] %*%) [(in [the]|from) %*%] [(from playing to|for) %*%]"
		);
		compare(
			new PatternParser(regexPattern("stop (all:all sound[s]|sound[s] %-strings%) [(in [the]|from) %-soundcategory%] [(from playing to|for) %players%]"))
				.getCombinations(),
			Set.of(
				"stop all sound", "stop all sound in %*%", "stop all sound in %*% from playing to %*%", "stop all sound in %*% for %*%",
				"stop all sound in the %*%", "stop all sound in the %*% from playing to %*%", "stop all sound in the %*% for %*%",
				"stop all sound from %*%", "stop all sound from %*% from playing to %*%", "stop all sound from %*% for %*%",
				"stop all sound from playing to %*%", "stop all sound for %*%",

				"stop all sounds", "stop all sounds in %*%", "stop all sounds in %*% from playing to %*%", "stop all sounds in %*% for %*%",
				"stop all sounds in the %*%", "stop all sounds in the %*% from playing to %*%", "stop all sounds in the %*% for %*%",
				"stop all sounds from %*%", "stop all sounds from %*% from playing to %*%", "stop all sounds from %*% for %*%",
				"stop all sounds from playing to %*%", "stop all sounds for %*%",

				"stop sound %*%", "stop sound %*% in %*%", "stop sound %*% in %*% from playing to %*%", "stop sound %*% in %*% for %*%",
				"stop sound %*% in the %*%", "stop sound %*% in the %*% from playing to %*%", "stop sound %*% in the %*% for %*%",
				"stop sound %*% from %*%", "stop sound %*% from %*% from playing to %*%", "stop sound %*% from %*% for %*%",
				"stop sound %*% from playing to %*%", "stop sound %*% for %*%",

				"stop sounds %*%", "stop sounds %*% in %*%", "stop sounds %*% in %*% from playing to %*%", "stop sounds %*% in %*% for %*%",
				"stop sounds %*% in the %*%", "stop sounds %*% in the %*% from playing to %*%", "stop sounds %*% in the %*% for %*%",
				"stop sounds %*% from %*%", "stop sounds %*% from %*% from playing to %*%", "stop sounds %*% from %*% for %*%",
				"stop sounds %*% from playing to %*%", "stop sounds %*% for %*%"
			)
		);

		Assert.assertEquals(
			regexPattern("[the] [high:(tall|high)|(low|normal)] fall damage sound[s] [from [[a] height [of]] %-number%] of %livingentities%"),
			"[the] [(tall|high)|(low|normal)] fall damage sound[s] [from [[a] height [of]] %*%] of %*%"
		);
		compare(
			new PatternParser(regexPattern("[the] [high:(tall|high)|(low|normal)] fall damage sound[s] [from [[a] height [of]] %-number%] of %livingentities%"))
				.getCombinations(),
			Set.of(
				"the tall fall damage sound of %*%", "the tall fall damage sound from %*% of %*%",
				"the tall fall damage sound from a height %*% of %*%", "the tall fall damage sound from a height of %*% of %*%",
				"the tall fall damage sound from height %*% of %*%", "the tall fall damage sound from height of %*% of %*%",

				"the tall fall damage sounds of %*%", "the tall fall damage sounds from %*% of %*%",
				"the tall fall damage sounds from a height %*% of %*%", "the tall fall damage sounds from a height of %*% of %*%",
				"the tall fall damage sounds from height %*% of %*%", "the tall fall damage sounds from height of %*% of %*%",

				"the high fall damage sound of %*%", "the high fall damage sound from %*% of %*%",
				"the high fall damage sound from a height %*% of %*%", "the high fall damage sound from a height of %*% of %*%",
				"the high fall damage sound from height %*% of %*%", "the high fall damage sound from height of %*% of %*%",

				"the high fall damage sounds of %*%", "the high fall damage sounds from %*% of %*%",
				"the high fall damage sounds from a height %*% of %*%", "the high fall damage sounds from a height of %*% of %*%",
				"the high fall damage sounds from height %*% of %*%", "the high fall damage sounds from height of %*% of %*%",

				"the low fall damage sound of %*%", "the low fall damage sound from %*% of %*%",
				"the low fall damage sound from a height %*% of %*%", "the low fall damage sound from a height of %*% of %*%",
				"the low fall damage sound from height %*% of %*%", "the low fall damage sound from height of %*% of %*%",

				"the low fall damage sounds of %*%", "the low fall damage sounds from %*% of %*%",
				"the low fall damage sounds from a height %*% of %*%", "the low fall damage sounds from a height of %*% of %*%",
				"the low fall damage sounds from height %*% of %*%", "the low fall damage sounds from height of %*% of %*%",

				"the normal fall damage sound of %*%", "the normal fall damage sound from %*% of %*%",
				"the normal fall damage sound from a height %*% of %*%", "the normal fall damage sound from a height of %*% of %*%",
				"the normal fall damage sound from height %*% of %*%", "the normal fall damage sound from height of %*% of %*%",

				"the normal fall damage sounds of %*%", "the normal fall damage sounds from %*% of %*%",
				"the normal fall damage sounds from a height %*% of %*%", "the normal fall damage sounds from a height of %*% of %*%",
				"the normal fall damage sounds from height %*% of %*%", "the normal fall damage sounds from height of %*% of %*%",

				"the fall damage sound of %*%", "the fall damage sound from %*% of %*%",
				"the fall damage sound from a height %*% of %*%", "the fall damage sound from a height of %*% of %*%",
				"the fall damage sound from height %*% of %*%", "the fall damage sound from height of %*% of %*%",

				"the fall damage sounds of %*%", "the fall damage sounds from %*% of %*%",
				"the fall damage sounds from a height %*% of %*%", "the fall damage sounds from a height of %*% of %*%",
				"the fall damage sounds from height %*% of %*%", "the fall damage sounds from height of %*% of %*%",

				"tall fall damage sound of %*%", "tall fall damage sound from %*% of %*%",
				"tall fall damage sound from a height %*% of %*%", "tall fall damage sound from a height of %*% of %*%",
				"tall fall damage sound from height %*% of %*%", "tall fall damage sound from height of %*% of %*%",

				"tall fall damage sounds of %*%", "tall fall damage sounds from %*% of %*%",
				"tall fall damage sounds from a height %*% of %*%", "tall fall damage sounds from a height of %*% of %*%",
				"tall fall damage sounds from height %*% of %*%", "tall fall damage sounds from height of %*% of %*%",

				"high fall damage sound of %*%", "high fall damage sound from %*% of %*%",
				"high fall damage sound from a height %*% of %*%", "high fall damage sound from a height of %*% of %*%",
				"high fall damage sound from height %*% of %*%", "high fall damage sound from height of %*% of %*%",

				"high fall damage sounds of %*%", "high fall damage sounds from %*% of %*%",
				"high fall damage sounds from a height %*% of %*%", "high fall damage sounds from a height of %*% of %*%",
				"high fall damage sounds from height %*% of %*%", "high fall damage sounds from height of %*% of %*%",

				"low fall damage sound of %*%", "low fall damage sound from %*% of %*%",
				"low fall damage sound from a height %*% of %*%", "low fall damage sound from a height of %*% of %*%",
				"low fall damage sound from height %*% of %*%", "low fall damage sound from height of %*% of %*%",

				"low fall damage sounds of %*%", "low fall damage sounds from %*% of %*%",
				"low fall damage sounds from a height %*% of %*%", "low fall damage sounds from a height of %*% of %*%",
				"low fall damage sounds from height %*% of %*%", "low fall damage sounds from height of %*% of %*%",

				"normal fall damage sound of %*%", "normal fall damage sound from %*% of %*%",
				"normal fall damage sound from a height %*% of %*%", "normal fall damage sound from a height of %*% of %*%",
				"normal fall damage sound from height %*% of %*%", "normal fall damage sound from height of %*% of %*%",

				"normal fall damage sounds of %*%", "normal fall damage sounds from %*% of %*%",
				"normal fall damage sounds from a height %*% of %*%", "normal fall damage sounds from a height of %*% of %*%",
				"normal fall damage sounds from height %*% of %*%", "normal fall damage sounds from height of %*% of %*%",

				"fall damage sound of %*%", "fall damage sound from %*% of %*%",
				"fall damage sound from a height %*% of %*%", "fall damage sound from a height of %*% of %*%",
				"fall damage sound from height %*% of %*%", "fall damage sound from height of %*% of %*%",

				"fall damage sounds of %*%", "fall damage sounds from %*% of %*%",
				"fall damage sounds from a height %*% of %*%", "fall damage sounds from a height of %*% of %*%",
				"fall damage sounds from height %*% of %*%", "fall damage sounds from height of %*% of %*%"
			)
		);

		Assert.assertEquals(
			regexPattern("[on] [:uncancelled|:cancelled|any:(any|all)] <.+> [priority:with priority (:(lowest|low|normal|high|highest|monitor))]"),
			"[on] [uncancelled|cancelled|(any|all)] <.+> [with priority ((lowest|low|normal|high|highest|monitor))]"
		);
		compare(
			new PatternParser(regexPattern("[on] [:uncancelled|:cancelled|any:(any|all)] <.+> [priority:with priority (:(lowest|low|normal|high|highest|monitor))]"))
				.getCombinations(),
			Set.of(
				"on <.+>", "on <.+> with priority lowest", "on <.+> with priority low",
				"on <.+> with priority normal", "on <.+> with priority high",
				"on <.+> with priority highest", "on <.+> with priority monitor",

				"on uncancelled <.+>", "on uncancelled <.+> with priority lowest", "on uncancelled <.+> with priority low",
				"on uncancelled <.+> with priority normal", "on uncancelled <.+> with priority high",
				"on uncancelled <.+> with priority highest", "on uncancelled <.+> with priority monitor",

				"on cancelled <.+>", "on cancelled <.+> with priority lowest", "on cancelled <.+> with priority low",
				"on cancelled <.+> with priority normal", "on cancelled <.+> with priority high",
				"on cancelled <.+> with priority highest", "on cancelled <.+> with priority monitor",

				"on any <.+>", "on any <.+> with priority lowest", "on any <.+> with priority low",
				"on any <.+> with priority normal", "on any <.+> with priority high",
				"on any <.+> with priority highest", "on any <.+> with priority monitor",

				"on all <.+>", "on all <.+> with priority lowest", "on all <.+> with priority low",
				"on all <.+> with priority normal", "on all <.+> with priority high",
				"on all <.+> with priority highest", "on all <.+> with priority monitor",

				"<.+>", "<.+> with priority lowest", "<.+> with priority low",
				"<.+> with priority normal", "<.+> with priority high",
				"<.+> with priority highest", "<.+> with priority monitor",

				"uncancelled <.+>", "uncancelled <.+> with priority lowest", "uncancelled <.+> with priority low",
				"uncancelled <.+> with priority normal", "uncancelled <.+> with priority high",
				"uncancelled <.+> with priority highest", "uncancelled <.+> with priority monitor",

				"cancelled <.+>", "cancelled <.+> with priority lowest", "cancelled <.+> with priority low",
				"cancelled <.+> with priority normal", "cancelled <.+> with priority high",
				"cancelled <.+> with priority highest", "cancelled <.+> with priority monitor",

				"any <.+>", "any <.+> with priority lowest", "any <.+> with priority low",
				"any <.+> with priority normal", "any <.+> with priority high",
				"any <.+> with priority highest", "any <.+> with priority monitor",

				"all <.+>", "all <.+> with priority lowest", "all <.+> with priority low",
				"all <.+> with priority normal", "all <.+> with priority high",
				"all <.+> with priority highest", "all <.+> with priority monitor"
			)
		);

		Assert.assertEquals(
			regexPattern("(open|show) ((0¦(crafting [table]|workbench)|1¦chest|2¦anvil|3¦hopper|4¦dropper|5¦dispenser) (view|window|inventory|)|%-inventory/inventorytype%) (to|for) %players%"),
			"(open|show) (((crafting [table]|workbench)|chest|anvil|hopper|dropper|dispenser) (view|window|inventory|)|%*%) (to|for) %*%"
		);
		compare(
			new PatternParser(regexPattern("(open|show) ((0¦(crafting [table]|workbench)|1¦chest|2¦anvil|3¦hopper|4¦dropper|5¦dispenser) (view|window|inventory|)|%-inventory/inventorytype%) (to|for) %players%"))
				.getCombinations(),
			Set.of(
				"open crafting to %*%", "open crafting view to %*%", "open crafting window to %*%",
				"open crafting inventory to %*%", "open crafting for %*%", "open crafting view for %*%",
				"open crafting window for %*%", "open crafting inventory for %*%",

				"open crafting table to %*%", "open crafting table view to %*%", "open crafting table window to %*%",
				"open crafting table inventory to %*%", "open crafting table for %*%", "open crafting table view for %*%",
				"open crafting table window for %*%", "open crafting table inventory for %*%",

				"open workbench to %*%", "open workbench view to %*%", "open workbench window to %*%",
				"open workbench inventory to %*%", "open workbench for %*%", "open workbench view for %*%",
				"open workbench window for %*%", "open workbench inventory for %*%",

				"open chest to %*%", "open chest view to %*%", "open chest window to %*%",
				"open chest inventory to %*%", "open chest for %*%", "open chest view for %*%",
				"open chest window for %*%", "open chest inventory for %*%",

				"open anvil to %*%", "open anvil view to %*%", "open anvil window to %*%",
				"open anvil inventory to %*%", "open anvil for %*%", "open anvil view for %*%",
				"open anvil window for %*%", "open anvil inventory for %*%",

				"open hopper to %*%", "open hopper view to %*%", "open hopper window to %*%",
				"open hopper inventory to %*%", "open hopper for %*%", "open hopper view for %*%",
				"open hopper window for %*%", "open hopper inventory for %*%",

				"open dropper to %*%", "open dropper view to %*%", "open dropper window to %*%",
				"open dropper inventory to %*%", "open dropper for %*%", "open dropper view for %*%",
				"open dropper window for %*%", "open dropper inventory for %*%",

				"open dispenser to %*%", "open dispenser view to %*%", "open dispenser window to %*%",
				"open dispenser inventory to %*%", "open dispenser for %*%", "open dispenser view for %*%",
				"open dispenser window for %*%", "open dispenser inventory for %*%",

				"open %*% to %*%", "open %*% for %*%",

				"show crafting to %*%", "show crafting view to %*%", "show crafting window to %*%",
				"show crafting inventory to %*%", "show crafting for %*%", "show crafting view for %*%",
				"show crafting window for %*%", "show crafting inventory for %*%",

				"show crafting table to %*%", "show crafting table view to %*%", "show crafting table window to %*%",
				"show crafting table inventory to %*%", "show crafting table for %*%", "show crafting table view for %*%",
				"show crafting table window for %*%", "show crafting table inventory for %*%",

				"show workbench to %*%", "show workbench view to %*%", "show workbench window to %*%",
				"show workbench inventory to %*%", "show workbench for %*%", "show workbench view for %*%",
				"show workbench window for %*%", "show workbench inventory for %*%",

				"show chest to %*%", "show chest view to %*%", "show chest window to %*%",
				"show chest inventory to %*%", "show chest for %*%", "show chest view for %*%",
				"show chest window for %*%", "show chest inventory for %*%",

				"show anvil to %*%", "show anvil view to %*%", "show anvil window to %*%",
				"show anvil inventory to %*%", "show anvil for %*%", "show anvil view for %*%",
				"show anvil window for %*%", "show anvil inventory for %*%",

				"show hopper to %*%", "show hopper view to %*%", "show hopper window to %*%",
				"show hopper inventory to %*%", "show hopper for %*%", "show hopper view for %*%",
				"show hopper window for %*%", "show hopper inventory for %*%",

				"show dropper to %*%", "show dropper view to %*%", "show dropper window to %*%",
				"show dropper inventory to %*%", "show dropper for %*%", "show dropper view for %*%",
				"show dropper window for %*%", "show dropper inventory for %*%",

				"show dispenser to %*%", "show dispenser view to %*%", "show dispenser window to %*%",
				"show dispenser inventory to %*%", "show dispenser for %*%", "show dispenser view for %*%",
				"show dispenser window for %*%", "show dispenser inventory for %*%",

				"show %*% to %*%", "show %*% for %*%"
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

		hasMultiple.remove("<.+>"); // Remove regex
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
