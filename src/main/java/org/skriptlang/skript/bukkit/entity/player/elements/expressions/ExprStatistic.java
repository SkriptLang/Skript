package org.skriptlang.skript.bukkit.entity.player.elements.expressions;

import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.bukkitutil.EntityUtils;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.entity.EntityData;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.skript.registrations.Classes;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.OfflinePlayer;
import org.bukkit.Statistic;
import org.bukkit.Statistic.Type;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxRegistry;

import java.util.Arrays;

@SuppressWarnings("unchecked")
@Name("Player Statistic")
@Description("""
	Represents a statistic of a player.
	Some statistics require an entity type or item type to be specified.
	See https://minecraft.wiki/w/Statistics for a full list of statistics.
	Using this expression does not call <a href='#EvtPlayerStatisticChange'>Player Statistic Change</a> event.
	""")
@Example("""
	command /basicinfo:
		trigger:
			send "You've killed %player kills statistic of player% players!"
			send "You've broken %mine block statistic using sand of player% sand!"
	""")
@Example("""
	on death:
		 chance of 50%:
			 remove 1 from deaths statistic of player
			 send "You got lucky and didn't ruin your kdr!" to victim
	""")
@Example("""
	on break of tnt:
		chance of 25%:
			 add 2 to mine block statistic using tnt (itemtype) of player
			 send "You gained +2 tnt broken! (25% chance)"
	""")
@Since("INSERT VERSION")
public class ExprStatistic extends PropertyExpression<OfflinePlayer, Integer> {

	public static void register(SyntaxRegistry registry) {
		registry.register(
			SyntaxRegistry.EXPRESSION,
			infoBuilder(
				ExprStatistic.class,
				Integer.class,
				"%statistic% stat[istic] [using %-entitydata/itemtype%]",
				"offlineplayers",
				false
			)
				.supplier(ExprStatistic::new)
				.build()
		);
	}

	private Expression<Statistic> statistic;
	private Expression<?> ofType;

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		if (matchedPattern == 1) {
			statistic = (Expression<Statistic>) expressions[1];
			ofType = expressions[2];
			setExpr((Expression<? extends OfflinePlayer>) expressions[0]);
		} else {
			statistic = (Expression<Statistic>) expressions[0];
			ofType = expressions[1];
			setExpr((Expression<? extends OfflinePlayer>) expressions[2]);
		}
		return true;
	}

	@Override
	protected Integer[] get(Event event, OfflinePlayer[] source) {
		Statistic statistic = this.statistic.getSingle(event);
		if (statistic == null)
			return new Integer[0];
		Object type = ofType != null ? ofType.getSingle(event) : null;
		if (checkTyping(statistic, type))
			return new Integer[0];

		return Arrays.stream(source)
			.map(player -> getStatistic(player, statistic, type))
			.toArray(Integer[]::new);
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		return switch (mode) {
			case SET, ADD, REMOVE, RESET, DELETE -> CollectionUtils.array(Integer.class);
			default -> null;
		};
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		int amount = (mode == ChangeMode.RESET || mode == ChangeMode.DELETE) ? 0 : (Integer) delta[0];
		Statistic statistic = this.statistic.getSingle(event);
		if (statistic == null)
			return;
		Object type = ofType != null ? ofType.getSingle(event) : null;
		if (checkTyping(statistic, type))
			return;

		switch (mode) {
			case SET, RESET, DELETE -> {
				for (OfflinePlayer player : getExpr().getArray(event))
					applyStatistic(player, statistic, amount, type);
			}
			case ADD -> {
				for (OfflinePlayer player : getExpr().getArray(event))
					applyStatistic(player, statistic, getStatistic(player, statistic, type) + amount, type);
			}
			case REMOVE -> {
				for (OfflinePlayer player : getExpr().getArray(event))
					applyStatistic(player, statistic, getStatistic(player, statistic, type) - amount, type);
			}
		}
	}

	/**
	 * Retrieves the value of a specific statistic for a given player.
	 *
	 * @param player The player whose statistic is being retrieved.
	 * @param statistic The statistic itself. See <a href="https://minecraft.wiki/w/Statistics">Statistics</a> for a list of every possible statistic.
	 * @param type The statistic type. Can be ITEM, BLOCK or ENTITY.
	 * @return The value of the specified statistic of the player.
	 */
	private int getStatistic(OfflinePlayer player, Statistic statistic, Object type) {
		Statistic.Type statisticType = statistic.getType();
		if (type instanceof ItemType item && (statisticType == Statistic.Type.ITEM || statisticType == Statistic.Type.BLOCK)) {
			return player.getStatistic(statistic, item.getMaterial());
		} else if (type instanceof EntityData<?> data && statisticType == Statistic.Type.ENTITY) {
			return player.getStatistic(statistic, EntityUtils.toBukkitEntityType(data));
		}

		return player.getStatistic(statistic);
	}

	/**
	 * Applies the given value to the statistic of the player.
	 *
	 * @param player The player whose statistic is being modified.
	 * @param statistic The statistic itself. See <a href="https://minecraft.wiki/w/Statistics">Statistics</a> for a list of every possible statistic.
	 * @param value The value that the statistic should be changed to.
	 * @param type The statistic type. Can be ITEM, BLOCK or ENTITY.
	 */
	private void applyStatistic(OfflinePlayer player, Statistic statistic, Integer value, Object type) {
		Statistic.Type statisticType = statistic.getType();
		value = Math.max(0, value);
		if (type instanceof ItemType item && (statisticType == Statistic.Type.ITEM || statisticType == Statistic.Type.BLOCK)) {
			player.setStatistic(statistic, item.getMaterial(), value);
		} else if (type instanceof EntityData<?> data && statisticType == Statistic.Type.ENTITY) {
			player.setStatistic(statistic, EntityUtils.toBukkitEntityType(data), value);
		} else {
			player.setStatistic(statistic, value);
		}
	}

	/**
	 * Checks if the specified statistic needs a type or not.
	 *
	 *
	 * @param statistic The statistic itself. See <a href="https://minecraft.wiki/w/Statistics">Statistics</a> for a list of every possible statistic.
	 * @param ofType The statistic type. Can be ITEM, BLOCK or ENTITY.
	 * @return True if the statistic with the given type is invalid else false if it is valid.
	 */
	private boolean checkTyping(Statistic statistic, Object ofType) {
		Type statisticType = statistic.getType();

		String statisticString = "The statistic '" + statistic + "'";
		if (ofType == null && statisticType != Type.UNTYPED) {
			if (statisticType == Type.ITEM || statisticType == Type.BLOCK) {
				error(statisticString + " requires an item type to be specified.");
			} else if (statisticType == Type.ENTITY) {
				error(statisticString + " requires an entity data to be specified.");
			}
			return true;
		} else if (this.ofType != null && statisticType == Type.UNTYPED) {
			warning(statisticString + " does not require an entity data or item type to be provided, so it will be ignored.");
		}

		if (ofType instanceof ItemType && statisticType == Type.ENTITY) {
			error(statisticString + " requires an entity data, but '" + Classes.toString(ofType) + "' was provided.");
			return true;
		} else if (ofType instanceof EntityData && (statisticType == Type.ITEM || statisticType == Type.BLOCK)) {
			error(statisticString + " requires an item type, but '" + Classes.toString(ofType) + "' was provided.");
			return true;
		}

		return false;
	}

	@Override
	public Class<Integer> getReturnType() {
		return Integer.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return new SyntaxStringBuilder(event, debug)
			.append("the statistic", statistic, "of", getExpr().getArray(event))
			.appendIf(ofType != null, "using", ofType)
			.toString();
	}

}
