package ch.njol.skript.conditions;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptConfig;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SimplifiedCondition;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.VerboseAssert;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.lang.util.common.AnyContains;
import ch.njol.skript.util.LiteralUtils;
import ch.njol.util.Kleenean;
import ch.njol.util.StringUtils;
import org.bukkit.event.Event;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.common.properties.conditions.PropCondContains;
import org.skriptlang.skript.lang.comparator.Comparators;
import org.skriptlang.skript.lang.comparator.Relation;
import org.skriptlang.skript.lang.converter.Converters;

import java.util.Arrays;
import java.util.Objects;
import java.util.StringJoiner;

/**
 * @deprecated This is being removed in favor of {@link PropCondContains}
 */
@Name("Contains")
@Description("Checks whether an inventory contains an item, a text contains another piece of text, "
	+ "a container contains something, "
	+ "or a list (e.g. {list variable::*} or 'drops') contains another object.")
@Example("block contains 20 cobblestone")
@Example("player has 4 flint and 2 iron ingots")
@Example("{list::*} contains 5")
@Since("1.0")
@Deprecated(since="2.13", forRemoval = true)
public class CondContains extends Condition implements VerboseAssert {

	static {
		if (!SkriptConfig.useTypeProperties.value())
			Skript.registerCondition(CondContains.class,
				"%inventories% (has|have) %itemtypes% [in [(the[ir]|his|her|its)] inventory]",
				"%inventories% (doesn't|does not|do not|don't) have %itemtypes% [in [(the[ir]|his|her|its)] inventory]",
				"%inventories/strings/objects% contain[(1¦s)] %itemtypes/strings/objects%",
				"%inventories/strings/objects% (doesn't|does not|do not|don't) contain %itemtypes/strings/objects%"
			);
	}

	/**
	 * The type of check to perform
	 */
	private enum CheckType {
		STRING, INVENTORY, OBJECTS, UNKNOWN, CONTAINER
	}

	private Expression<?> containers;
	private Expression<?> items;

	private boolean explicitSingle;
	private CheckType checkType;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		containers = LiteralUtils.defendExpression(exprs[0]);
		items = LiteralUtils.defendExpression(exprs[1]);

		explicitSingle = matchedPattern == 2 && parseResult.mark != 1 || containers.isSingle();

		if (matchedPattern <= 1) {
			checkType = CheckType.INVENTORY;
		} else {
			checkType = CheckType.UNKNOWN;
		}

		this.setNegated(matchedPattern % 2 == 1);
		return LiteralUtils.canInitSafely(containers, items);
	}

	@Override
	public boolean check(Event event) {
		CheckType checkType = this.checkType;

		Object[] containerValues = containers.getAll(event);

		if (containerValues.length == 0)
			return isNegated();

		// Change checkType according to values
		if (checkType == CheckType.UNKNOWN) {
			if (Arrays.stream(containerValues)
				.allMatch(Inventory.class::isInstance)) {
				checkType = CheckType.INVENTORY;
			} else if (explicitSingle
				&& Arrays.stream(containerValues)
				.allMatch(object -> object instanceof AnyContains<?>
					|| Converters.converterExists(object.getClass(), AnyContains.class))) {
				checkType = CheckType.CONTAINER;
			} else if (explicitSingle
				&& Arrays.stream(containerValues)
				.allMatch(String.class::isInstance)) {
				checkType = CheckType.STRING;
			} else {
				checkType = CheckType.OBJECTS;
			}
		}

		return switch (checkType) {
			case INVENTORY -> SimpleExpression.check(containerValues, o -> {
				Inventory inventory = (Inventory) o;

				return items.check(event, o1 -> {
					if (o1 instanceof ItemType type) {
						return type.isContainedIn(inventory);
					} else if (o1 instanceof ItemStack stack) {
						return inventory.containsAtLeast(stack, stack.getAmount());
					} else if (o1 instanceof Inventory) {
						return Objects.equals(inventory, o1);
					}
					return false;
				});
			}, isNegated(), containers.getAnd());
			case STRING -> {
				boolean caseSensitive = SkriptConfig.caseSensitive.value();

				yield SimpleExpression.check(containerValues, o -> {
					String string = (String) o;

					return items.check(event, o1 -> {
						if (o1 instanceof String text) {
							return StringUtils.contains(string, text, caseSensitive);
						} else {
							return false;
						}
					});
				}, isNegated(), containers.getAnd());
			}
			case CONTAINER -> SimpleExpression.check(containerValues, object -> {
				AnyContains container;
				if (object instanceof AnyContains<?>) {
					container = (AnyContains) object;
				} else {
					container = Converters.convert(object, AnyContains.class);
				}
				if (container == null)
					return false;
				return items.check(event, container::checkSafely);
			}, isNegated(), containers.getAnd());
			default -> {
				assert checkType == CheckType.OBJECTS;
				yield items.check(event, o1 -> {
					for (Object o2 : containerValues) {
						if (Comparators.compare(o1, o2) == Relation.EQUAL)
							return true;
					}
					return false;
				}, isNegated());
			}
		};
	}

	@Override
	public String getExpectedMessage(Event event) {
		StringJoiner joiner = new StringJoiner(" ");
		joiner.add("to");
		if (isNegated()) {
			joiner.add("not");
		}
		joiner.add("find %s".formatted(VerboseAssert.getExpressionValue(items, event)));
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
		joiner.add("match in %s".formatted(VerboseAssert.getExpressionValue(containers, event)));
		return joiner.toString();
	}

	@Override
	public Condition simplify() {
		if (containers instanceof Literal<?> && items instanceof Literal<?>)
			return SimplifiedCondition.fromCondition(this);
		return this;
	}

	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return containers.toString(e, debug) + (isNegated() ? " doesn't contain " : " contains ") + items.toString(e, debug);
	}

}
