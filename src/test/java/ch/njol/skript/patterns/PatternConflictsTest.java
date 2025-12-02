package ch.njol.skript.patterns;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptAPIException;
import ch.njol.skript.conditions.CondCompare;
import ch.njol.skript.conditions.CondContains;
import ch.njol.skript.conditions.CondDate;
import ch.njol.skript.conditions.CondIsLoaded;
import ch.njol.skript.conditions.CondScriptLoaded;
import ch.njol.skript.effects.EffScriptFile;
import ch.njol.skript.effects.EffWorldLoad;
import ch.njol.skript.expressions.*;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.Section;
import ch.njol.skript.lang.Statement;
import ch.njol.skript.test.runner.SkriptJUnitTest;
import ch.njol.util.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.junit.Assert;
import org.junit.Test;
import org.skriptlang.skript.bukkit.potion.elements.conditions.CondHasPotion;
import org.skriptlang.skript.lang.structure.Structure;
import org.skriptlang.skript.registration.SyntaxInfo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class PatternConflictsTest extends SkriptJUnitTest {

	private static void compare(Set<String> got, Set<String> expect) {
		if (expect.equals(got))
			return;

		Set<String> gotCopy = new HashSet<>(got);
		gotCopy.removeAll(expect);
		if (!gotCopy.isEmpty()) {
			Assert.fail("Unexpected combinations: " + gotCopy);
		}
		Set<String> expectCopy = new HashSet<>(expect);
		expectCopy.removeAll(got);
		if (!expectCopy.isEmpty()) {
			Assert.fail("Combinations not found: " + expectCopy);
		}
	}

	private static Set<String> getCombinations(String pattern) {
		return PatternCompiler.compile(pattern, new AtomicInteger()).getAllCombinations(true);
	}

	@Test
	public void test() {
		compare(
			getCombinations("[all [of the]|the] entities [of %-world%]"),
			Set.of(
				"all entities", "all entities of %*%",
				"all of the entities", "all of the entities of %*%",
				"the entities", "the entities of %*%",
				"entities", "entities of %*%"
			)
		);

		compare(
			getCombinations("[all [of the]|the] [:typed] entities [of %-world%]"),
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

		compare(
			getCombinations("stop (all:all sound[s]|sound[s] %-strings%) [(in [the]|from) %-soundcategory%] [(from playing to|for) %players%]"),
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

		compare(
			getCombinations("[the] [high:(tall|high)|(low|normal)] fall damage sound[s] [from [[a] height [of]] %-number%] of %livingentities%"),
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

		compare(
			getCombinations("[on] [:uncancelled|:cancelled|any:(any|all)] <.+> [priority:with priority (:(lowest|low|normal|high|highest|monitor))]"),
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

		compare(
			getCombinations("(open|show) ((0¦(crafting [table]|workbench)|1¦chest|2¦anvil|3¦hopper|4¦dropper|5¦dispenser) (view|window|inventory|)|%-inventory/inventorytype%) (to|for) %players%"),
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

	/**
	 * Enum to determine what element type a class falls into.
	 */
	private enum ElementType {
		STRUCTURE, STATEMENT, EXPRESSION;

		/**
		 * Gets the {@link ElementType} that {@code elementClass} falls into.
		 * @param elementClass The {@link Class} to check.
		 * @return The {@link ElementType}.
		 */
		private static ElementType getType(Class<?> elementClass) {
			if (Structure.class.isAssignableFrom(elementClass)) {
				return STRUCTURE;
			} else if (Statement.class.isAssignableFrom(elementClass) || Section.class.isAssignableFrom(elementClass)) {
				return STATEMENT;
			} else if (Expression.class.isAssignableFrom(elementClass)) {
				return EXPRESSION;
			}
			throw new IllegalArgumentException("The class '" + elementClass.getSimpleName() + "' does not fall into a type");
		}
	}

	/**
	 * Record for a logged pattern combination mainly for ensuring if it truly conflicts.
	 * @param combination The logged pattern combination.
	 * @param pattern The pattern the combination came from.
	 * @param elementClass The {@link Class} the pattern is registered to.
	 * @param elementType The {@link ElementType} of the {@code elementClass}.
	 */
	private record Combination(String combination, String pattern, Class<?> elementClass, ElementType elementType) {

		/**
		 * Whether this {@link Combination} truly conflicts with another {@link Combination}.
		 * @param other The other {@link Combination}.
		 * @return {@code true} if it conflicts, otherwise {@code false}.
		 */
		private boolean conflicts(Combination other) {
			return combination.equals(other.combination)
				&& elementType.equals(other.elementType)
				&& !elementClass.equals(other.elementClass);
		}

		@Override
		public boolean equals(Object object) {
			if (!(object instanceof Combination other))
				return false;
			return combination.equals(other.combination)
				&& elementType.equals(other.elementType)
				&& elementClass.equals(other.elementClass);
		}

	}

	private static class Exclusion {

		private final Set<Class<?>> classes;
		private final @Nullable String patternCombination;

		/**
		 * Constructs a new {@link Exclusion} that will exclude any conflicting combination
		 * as long as the only classes involved in the conflict are {@code classes}.
		 * @param classes The {@link Class}es to check for.
		 */
		private Exclusion(Class<?>... classes) {
			this(null, classes);
		}

		/**
		 * Constructs a new {@link Exclusion} that will exclude the conflicting {@code patternCombination}
		 * as long as the only classes involved in the conflict are {@code classes}.
		 * @param patternCombination The restricted combination.
		 * @param classes The {@link Class}es to check for.
		 */
		private Exclusion(@Nullable String patternCombination, Class<?>... classes) {
			this.patternCombination = patternCombination;
			this.classes = Set.of(classes);
		}

		/**
		 * Whether this {@link Exclusion} excludes the conflict by checking if the {@link Class}es from
		 * {@code combinations} are only {@link #classes}.
		 * @param combinations The {@link Combination}s to check.
		 * @return {@code true} if the conflict can be excluded, otherwise {@code false}.
		 */
		private boolean exclude(List<Combination> combinations) {
			if (combinations.isEmpty())
				return false;
			Set<Class<?>> combinationClasses = combinations.stream()
				.map(Combination::elementClass)
				.collect(Collectors.toSet());
            return combinationClasses.equals(classes);
        }

	}

	/**
	 * Whether the info messages from the process of {@link #testConflicts()} should broadcast
	 * via {@link Skript#adminBroadcast(String)}.
	 */
	public static boolean BROADCAST = false;

	private static final Set<Exclusion> EXCLUSIONS = new HashSet<>();

	static {
		// Usage of these depend on an Experiment being enabled
		EXCLUSIONS.add(new Exclusion(ExprScriptsOld.class, ExprScripts.class));

		// Intentional - Sovde
		EXCLUSIONS.add(new Exclusion("vector from %*%", ExprVectorOfLocation.class, ExprVectorFromDirection.class));

		// TODO - Fix these conflicts
		// Exclusions by amount of conflicts
		// 1 conflict
		EXCLUSIONS.add(new Exclusion("formatted %*%", ExprFormatDate.class, ExprColoured.class));
		EXCLUSIONS.add(new Exclusion("unload %*%", EffScriptFile.class, EffWorldLoad.class));
		EXCLUSIONS.add(new Exclusion("the %*% of %*%", ExprArmorSlot.class, ExprEntities.class));
		EXCLUSIONS.add(new Exclusion("%*% of %*%", ExprArmorSlot.class, ExprEntities.class, ExprXOf.class));

		// 2 conflicts
		EXCLUSIONS.add(new Exclusion(ExprNewBannerPattern.class, ExprFireworkEffect.class));
		EXCLUSIONS.add(new Exclusion(ExprInventoryAction.class, ExprClicked.class));
		EXCLUSIONS.add(new Exclusion(ExprEntities.class, ExprValueWithin.class));

		// 4 conflicts
		EXCLUSIONS.add(new Exclusion(ExprEntitySound.class, ExprBlockSound.class));

		// 5 conflicts
		EXCLUSIONS.add(new Exclusion(ExprEntities.class, ExprSets.class));

		// 6 conflicts
		EXCLUSIONS.add(new Exclusion(ExprEntities.class, ExprItemsIn.class));
		// CondContains takes precedence over CondHasPotion in the case of "{_x} has {_y}"
		EXCLUSIONS.add(new Exclusion(CondContains.class, CondHasPotion.class));

		// 8 conflicts
		EXCLUSIONS.add(new Exclusion(CondScriptLoaded.class, CondIsLoaded.class));
		EXCLUSIONS.add(new Exclusion(CondDate.class, CondCompare.class));
	}

	private void info(String message) {
		Skript.debug(message);
		if (BROADCAST)
			Skript.adminBroadcast(message);
	}

	@Test
	public void testConflicts() {
		Map<String, List<Combination>> registeredCombinations = new HashMap<>();

		Collection<SyntaxInfo<?>> elements = Skript.instance().syntaxRegistry().elements();
		info("Running Conflicts Test");
		info("Total Elements: " + elements.size());
		int patternCounter = 0;
		int combinationCounter = 0;
		for (SyntaxInfo<?> syntaxInfo : elements) {
			Collection<String> patterns = syntaxInfo.patterns();
			Class<?> elementClass = syntaxInfo.type();
			ElementType elementType = ElementType.getType(elementClass);

			for (String pattern : patterns) {
				patternCounter++;
				for (String patternCombination : getCombinations(pattern)) {
					combinationCounter++;
					Combination combination = new Combination(patternCombination, pattern, elementClass, elementType);
					registeredCombinations.computeIfAbsent(patternCombination, list -> new ArrayList<>()).add(combination);
				}
			}
		}
		info("Total Patterns: " + patternCounter);
		info("Total Combinations: " + combinationCounter);

		Set<String> conflicts = new HashSet<>();
		int filterCombinationCounter = 0;
		int filterConflictCounter = 0;
		for (Entry<String, List<Combination>> entry : registeredCombinations.entrySet()) {
			List<Combination> combinations = entry.getValue();
			int size = combinations.size();
			if (size <= 1)
				continue;
			// Filter out combinations that can't conflict due to different element types
			boolean[] hasConflict = new boolean[size];
			for (int firstIndex = 0; firstIndex < size - 1; firstIndex++) {
				if (hasConflict[firstIndex])
					continue;
				for (int secondIndex = firstIndex + 1; secondIndex < size; secondIndex++) {
					if (combinations.get(firstIndex).conflicts(combinations.get(secondIndex))) {
						hasConflict[firstIndex] = true;
						hasConflict[secondIndex] = true;
					}
				}
			}

			int index = 0;
			for (Iterator<Combination> iterator = combinations.iterator(); iterator.hasNext();) {
				iterator.next();
				if (!hasConflict[index]) {
					filterCombinationCounter++;
					iterator.remove();
				}
				index++;
			}

			if (combinations.size() <= 1) {
				filterConflictCounter++;
			} else {
				conflicts.add(entry.getKey());
			}
		}
		info("Total Filtered Combinations: " + filterCombinationCounter);
		info("Total Filtered Conflicts: " + filterConflictCounter);

		if (conflicts.isEmpty())
			return;

		// Check exclusions
		int excludedCounter = 0;
		for (Exclusion exclusion : EXCLUSIONS) {
			if (conflicts.isEmpty())
				break;
			String exclusionPattern = exclusion.patternCombination;
			if (exclusionPattern != null) {
				if (!conflicts.contains(exclusionPattern))
					continue;
				if (exclusion.exclude(registeredCombinations.get(exclusionPattern))) {
					conflicts.remove(exclusionPattern);
					excludedCounter++;
				}
			} else {
				for (Iterator<String> iterator = conflicts.iterator(); iterator.hasNext();) {
					String string = iterator.next();
					if (exclusion.exclude(registeredCombinations.get(string))) {
						iterator.remove();
						excludedCounter++;
					}
				}
			}
		}
		info("Total Excluded Conflicts: " + excludedCounter);
		if (conflicts.isEmpty())
			return;

		List<String> errors = new ArrayList<>();
		for (String string : conflicts) {
			List<String> names = registeredCombinations.get(string).stream()
				.map(combination -> "Class: " + combination.elementClass.getSimpleName() + " - Pattern: " + combination.pattern)
				.toList();
			String error = "The pattern combination '" + string + "' conflicts in: \n\t\t\t" + StringUtils.join(names, "\n\t\t\t");
			errors.add(error);
		}
		errors.add("Total Conflicts: " + errors.size());
		throw new SkriptAPIException(StringUtils.join(errors, "\n\t"));
	}

}
