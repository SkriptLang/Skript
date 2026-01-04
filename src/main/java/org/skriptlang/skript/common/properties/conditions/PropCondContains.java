package org.skriptlang.skript.common.properties.conditions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RelatedProperty;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.skript.lang.VerboseAssert;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.LiteralUtils;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.docs.Origin;
import org.skriptlang.skript.lang.comparator.Comparators;
import org.skriptlang.skript.lang.comparator.Relation;
import org.skriptlang.skript.lang.converter.Converters;
import org.skriptlang.skript.lang.properties.Property;
import org.skriptlang.skript.lang.properties.PropertyBaseSyntax;
import org.skriptlang.skript.lang.properties.PropertyMap;
import org.skriptlang.skript.lang.properties.handlers.ContainsHandler;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

import java.util.Arrays;
import java.util.Map;
import java.util.StringJoiner;

@Name("Contains (Property)")
@Description("""
	Checks whether a type or list contains certain elements.
	When checking if a list contains a specific element, use '{list::*} contains {x}'.
	When checking if a single type contains something, use `player's inventory contains {x}`.
	When checking if many types contain something, use '{inventories::*} contain {x}` \
	or `contents of {inventories::*} contain {x}`.
	""")
@Example("block contains 20 cobblestone")
@Example("player has 4 flint and 2 iron ingots")
@Example("{list::*} contains 5")
@Example("names of {list::*} contain \"prefix\"")
@Example("contents of the inventories of all players contain 1 stick")
@RelatedProperty("contains")
public class PropCondContains extends Condition implements PropertyBaseSyntax<ContainsHandler<?,?>>, VerboseAssert {

	public static void register(SyntaxRegistry registry, Origin origin) {
		registry.register(SyntaxRegistry.CONDITION, SyntaxInfo.builder(PropCondContains.class)
			.origin(origin)
			.addPatterns(
				"%objects% contain[1:s] %objects%",
				"%objects% (1:doesn't|1:does not|do not|don't) contain %objects%",
				"contents of %objects% contain %objects%",
				"contents of %objects% (do not|don't) contain %objects%",
				"%inventories% (has|have) %itemtypes% [in [(the[ir]|his|her|its)] inventory]",
				"%inventories% (doesn't|does not|do not|don't) have %itemtypes% [in [(the[ir]|his|her|its)] inventory]"
			)
			.supplier(PropCondContains::new)
			.build());
	}

	/*
	# singular haystack (containment first, direct if no property)
	x contain[s] A # -> containment if x/y/z has contains property, fallback to direct [existing behavior]
	x, y, or z contain[s] A # -> containment if x/y/z has contains property, fallback to direct [existing behavior]

	# contents of (always containment)
	contents of x, y, and/or z contain[s] A # -> containment [new behavior]

	# list or plural expression[s] (concatenate inputs into 1 list)
	x, y, and all zs contain A # -> containment if x/y/z has contains property, fallback to direct [existing behavior]
	x, y, and all zs contains A # -> direct [existing behavior]


	init:
		determine method:
			1) containment with direct fallback
				- isSingle() = true OR `contain`
			2) direct only
				- isSingle() = false AND `contains`
			3) containment only
				- `contents of`

		if containment will be used:
			1) do normal properties checks
			2) if any of the returned types from the haystack cannot be coerced, revert to direct if allowed, or parse error.

	check:
		determine types of haystack:
			1) if all have contains property
				- proceed with containment if allowed
			2) else
				- fall back to direct if allowed
	 */

	private Expression<?> haystack;
	private Expression<?> needles;
	private PropertyMap<ContainsHandler<?, ?>> properties;

	boolean allowContainmentCheck = false;
	boolean allowDirectCheck = false;

	int matchedPattern;

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		this.haystack = LiteralUtils.defendExpression(expressions[0]);
		this.needles = LiteralUtils.defendExpression(expressions[1]);
		this.matchedPattern = matchedPattern;
		if (!LiteralUtils.canInitSafely(haystack, needles))
			return false;
		allowContainmentCheck = parseResult.mark != 1 || haystack.isSingle();
		allowDirectCheck = matchedPattern < 2;
		setNegated(matchedPattern % 2 == 1);

		if (allowContainmentCheck) {
			// determine properties
			var tempHaystack = PropertyBaseSyntax.asProperty(Property.CONTAINS, expressions[0]);
			if (tempHaystack == null) {
				// attempt direct contains
				return initDirect(getBadTypesErrorMessage(expressions[0]));
			}
			// determine if the expression truly has a contains property
			properties = PropertyBaseSyntax.getPossiblePropertyInfos(Property.CONTAINS, tempHaystack);
			if (properties.isEmpty()) {
				// attempt direct contains
				return initDirect(getBadTypesErrorMessage(tempHaystack));
			}

			// determine possible needle types
			Class<?>[][] elementTypes = getElementTypes(properties);
			Class<?>[] elementTypeSet = Arrays.stream(elementTypes)
					.flatMap(Arrays::stream)
					.distinct()
					.toArray(Class[]::new);
			// if Object.class is encountered, remove all others
			for (Class<?> type : elementTypeSet) {
				if (type == Object.class) {
					elementTypeSet = new Class[]{Object.class};
					break;
				}
			}

			// attempt to convert needles
			//noinspection unchecked,rawtypes
			var convertedNeedles = needles.getConvertedExpression((Class[]) elementTypeSet);
			if (convertedNeedles == null) {
				// attempt direct contains
				return initDirect("'" + tempHaystack + "'  cannot contain " + Classes.toString(Arrays.stream(needles.possibleReturnTypes()).map(Classes::getSuperClassInfo).toArray(), false));
			}
			return LiteralUtils.canInitSafely(haystack, needles);
		} else {
			return initDirect(null);
		}
	}

	/**
	 * check whether the types are safe for direct comparison. Disables containment comparison, so should only be run if
	 * containment is not wanted or if it's known to be unsafe to do so.
	 * @param error The error to print if init fails. If null, a default error will be printed.
	 * @return whether intialization succeeded
	 */
	private boolean initDirect(@Nullable String error) {
		// check if types are reasonable:
		boolean validType = false;
		nextType:
		for (Class<?> haystackType : haystack.possibleReturnTypes()) {
			for (Class<?> needleType : needles.possibleReturnTypes()) {
				if (haystackType == Object.class || needleType == Object.class || Comparators.comparatorExists(haystackType, needleType)) {
					validType = true;
					break nextType;
				}
			}
		}
		if (!validType) {
			Skript.error(error != null ? error : "'" + haystack.toString() + "' cannot contain " + Classes.toString(needles.possibleReturnTypes(), false));
			return false;
		}
		allowContainmentCheck = false;
		return LiteralUtils.canInitSafely(haystack, needles);
	}

	/**
	 * gets all the possible element types given the properties found in the property map.
	 * @param properties The known valid properties.
	 * @return The element types.
	 */
	private Class<?>[][] getElementTypes(PropertyMap<ContainsHandler<?, ?>> properties) {
		return properties.values().stream()
			.map((propertyInfo) -> propertyInfo.handler().elementTypes())
			.toArray(Class<?>[][]::new);
	}

	@Override
	public boolean check(Event event) {
		Object[] haystacks = haystack.getAll(event);
		boolean haystackAnd = haystack.getAnd();
		Object[] needles = this.needles.getAll(event);
		boolean needlesAnd = this.needles.getAnd();
		if (haystacks.length == 0) {
			return isNegated();
		}

		// We should compare the contents of the haystacks to the needles
		if (allowContainmentCheck) {
			return checkContainment(haystacks, haystackAnd, needles, needlesAnd);
		// compare the haystacks themselves to the needles
		} else {
			return checkDirect(haystacks, needles, needlesAnd);
		}
	}

	/**
	 * Direct comparison check. For use in checking if a list contains an iten.
	 * @param haystacks The list of objects to check in.
	 * @param needles The objects to check for.
	 * @param needlesAnd Whether the objects should all be present or just one.
	 * @return The result of the contains check.
	 */
	private boolean checkDirect(Object[] haystacks, Object[] needles, boolean needlesAnd) {
		return SimpleExpression.check(needles, o1 -> {
			for (Object o2 : haystacks) {
				if (Comparators.compare(o1, o2) == Relation.EQUAL)
					return true;
			}
			return false;
		}, isNegated(), needlesAnd);
	}

	/**
	 * Converts an input into the target values, unless it's already a valid propertied type.
	 * @param input The input object to convert.
	 * @param targets The classes to convert to.
	 * @return The converted object, or null if conversion failed.
	 */
	private @Nullable Object convert(Object input, Class<?>... targets) {
		Class<?> type = input.getClass();
		// direct assignment
		if (properties.getHandler(type) != null)
			return input;
		// conversion
		return Converters.convert(input, targets);
	}

	/**
	 * Containment comparison check. For use in checking items themselves contain an item, using the contains property.
	 * @param haystacks The list of objects to check.
	 * @param needles The objects to check for.
	 * @param needlesAnd Whether the objects should all be present or just one.
	 * @return The result of the contains check.
	 */
	private boolean checkContainment(Object[] haystacks, boolean haystackAnd, Object[] needles, boolean needlesAnd) {
		// Attempt to convert all the types into property-having types
		boolean allHaveProperty = true;
		Class<?>[] targetTypes = properties.entrySet().stream()
				.filter(entry -> entry.getValue() != null)
				.map(Map.Entry::getKey)
				.toArray(Class[]::new);
		var convertedHaystacks = new Object[haystacks.length];
		for (int i = 0; i < haystacks.length; i++) {
			convertedHaystacks[i] = convert(haystacks[i], targetTypes);
			if (convertedHaystacks[i] == null) {
				allHaveProperty = false;
				break;
			}
		}

		// if something's missing a property, revert to direct comparison if allowed.
		if (!allHaveProperty) {
			if (allowDirectCheck)
				return checkDirect(haystacks, needles, needlesAnd);
			return isNegated();
		}

		// use properties
		return SimpleExpression.check(convertedHaystacks, (haystack) -> {
			// for each haystack, determine property
			//noinspection unchecked
			var handler = (ContainsHandler<Object, Object>) properties.getHandler(haystack.getClass());
			if (handler == null) {
				return false;
			}
			// if found, use it to check against needles
			return SimpleExpression.check(needles, (needle) ->
					handler.canContain(needle.getClass())
						&& handler.contains(haystack, needle),
				false, needlesAnd);
		}, isNegated(), haystackAnd);
	}

	@Override
	public @NotNull Property<ContainsHandler<?, ?>> getProperty() {
		return Property.CONTAINS;
	}

	@Override
	public String getExpectedMessage(Event event) {
		StringJoiner joiner = new StringJoiner(" ");
		joiner.add("to");
		if (isNegated()) {
			joiner.add("not");
		}
		joiner.add("find %s".formatted(VerboseAssert.getExpressionValue(needles, event)));
		return joiner.toString();
	}

	@Override
	public String getReceivedMessage(Event event) {
		StringJoiner joiner = new StringJoiner(" ");
		if (!isNegated()) {
			joiner.add("no");
		} else {
			joiner.add("a");
		}
		joiner.add("match in %s".formatted(VerboseAssert.getExpressionValue(haystack, event)));
		return joiner.toString();
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		var builder = new SyntaxStringBuilder(event, debug);
		switch (matchedPattern) {
			case 1, 2 -> {
				builder.append(haystack);
				if (isNegated())
					builder.append(allowContainmentCheck ? "don't" : "doesn't");
				builder.append(allowContainmentCheck ? "contain" : "contains")
						.append(needles);
			}
			case 3, 4 -> {
				builder.append("contents of", haystack);
				if (isNegated())
					builder.append("don't");
				builder.append("contain", needles);
			}
			case 5, 6 -> {
				builder.append(haystack);
				if (isNegated())
					builder.append("don't");
				builder.append("have", needles);
			}
		}
		return builder.toString();
	}

}
