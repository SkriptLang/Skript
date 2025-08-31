package org.skriptlang.skript.common.conditions;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.LiteralUtils;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.comparator.Comparators;
import org.skriptlang.skript.lang.comparator.Relation;
import org.skriptlang.skript.lang.properties.Property;
import org.skriptlang.skript.lang.properties.PropertyHandler.ContainsHandler;
import org.skriptlang.skript.lang.properties.PropertyUtils;
import org.skriptlang.skript.lang.properties.PropertyUtils.PropertyMap;

public class PropCondContains extends Condition {

	static {
		Skript.registerCondition(PropCondContains.class,
			"property %inventories% (has|have) %itemtypes% [in [(the[ir]|his|her|its)] inventory]",
			"property %inventories% (doesn't|does not|do not|don't) have %itemtypes% [in [(the[ir]|his|her|its)] inventory]",
			"property %inventories/strings/objects% contain[(1¦s)] %itemtypes/strings/objects%",
			"property %inventories/strings/objects% (doesn't|does not|do not|don't) contain %itemtypes/strings/objects%");
	}

	private Expression<?> haystack;
	private Expression<?> needles;
	private PropertyMap<ContainsHandler<?, ?>> properties;

	boolean explicitSingle = false;

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		this.haystack = PropertyUtils.asProperty(Property.CONTAINS, expressions[0]);
		if (haystack == null) {
			Skript.error("The expression " + expressions[0] + " returns types that do not contain anything.");
			return false;
		}
		// determine if the expression truly has a name property

		properties = PropertyUtils.getPossiblePropertyInfos(Property.CONTAINS, haystack);
		if (properties.isEmpty()) {
			Skript.error("The expression " + haystack + " returns types that do not contain anything.");
			return false; // no name property found
		}

		this.needles = LiteralUtils.defendExpression(expressions[1]);
		explicitSingle = matchedPattern == 2 && parseResult.mark != 1 || haystack.isSingle();

		if (explicitSingle) {
			// determine possible needle types
			Class<?>[][] elementTypes = getElementTypes(properties);
			var needleReturnTypes = needles.possibleReturnTypes();
			// if no needle types are compatible with the element types, error
			if (!determineTypeCompatibility(needleReturnTypes, elementTypes)) {
				Skript.error("'" + haystack + "'  cannot contain " + Classes.toString(needleReturnTypes, false));
				return false;
			}
		}

		return LiteralUtils.canInitSafely(haystack, needles);
	}

	private static boolean determineTypeCompatibility(Class<?>[] needleReturnTypes, Class<?>[][] elementTypes) {
		for (Class<?> needleType : needleReturnTypes) {
			for (Class<?>[] haystackType : elementTypes) {
				for (Class<?> allowedNeedleType : haystackType) {
					if (allowedNeedleType.isAssignableFrom(needleType)) {
						return true;
					}
				}
			}
		}
		return false;
	}

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
		if (explicitSingle) {
			// use properties
			return SimpleExpression.check(haystacks, (haystack) -> {
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

		// compare the haystacks themselves to the needles
		} else {
			return this.needles.check(event, o1 -> {
				for (Object o2 : haystacks) {
					if (Comparators.compare(o1, o2) == Relation.EQUAL)
						return true;
				}
				return false;
			}, isNegated());
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "x contains y";
	}
}
