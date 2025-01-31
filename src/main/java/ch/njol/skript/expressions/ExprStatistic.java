package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.bukkitutil.EntityUtils;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.entity.EntityData;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;

import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.skript.lang.parser.ParserInstance;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.OfflinePlayer;
import org.bukkit.Statistic;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.log.runtime.*;

public class ExprStatistic extends SimpleExpression<Integer> implements RuntimeErrorProducer {

	static {
		Skript.registerExpression(ExprStatistic.class, Integer.class, ExpressionType.COMBINED,
			"[the] statistic %statistic% [for %-entitydata/itemtype%] (from|of) %offlineplayers%",
			"%offlineplayers%'[s] statistic %statistic% [for %-entitydata/itemtype%]"
		);
	}

	private Expression<Statistic> statistic;
	private Expression<OfflinePlayer> player;
	private Expression<?> ofType;

	private ParserInstance parser;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		if (matchedPattern == 0) {
			player = (Expression<OfflinePlayer>) exprs[2];
			statistic = (Expression<Statistic>) exprs[0];
			ofType = exprs[1];
		} else {
			player = (Expression<OfflinePlayer>) exprs[0];
			statistic = (Expression<Statistic>) exprs[1];
			ofType = exprs[2];
		}
		parser = getParser();

		return true;
	}

	@Override
	protected Integer @Nullable [] get(Event event) {
		Statistic statistic = this.statistic.getSingle(event);
		if (statistic == null)
			return null;

		Object ofType = this.ofType == null ? null : this.ofType.getSingle(event);

		if (ofType == null && statistic.getType() != Statistic.Type.UNTYPED) {
			error("The statistic '" + statistic + "' requires an entity data or item type to be specified");
			return null;
		}

		return player.stream(event)
			.map(player -> getStatistic(player, statistic, ofType))
			.toArray(Integer[]::new);
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		return switch (mode) {
			case SET, DELETE, RESET, REMOVE, ADD -> CollectionUtils.array(Integer.class);
			default -> null;
		};
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		Statistic statistic = this.statistic.getSingle(event);
		if (statistic == null)
			return;

		Object ofType = this.ofType == null ? null : this.ofType.getSingle(event);

		if (ofType == null && statistic.getType() != Statistic.Type.UNTYPED) {
			error("The statistic '" + statistic + "' requires an entity data or item type to be specified");
			return;
		}

		int value = delta == null ? 0 : (Integer) delta[0];

		for (OfflinePlayer player : this.player.getArray(event)) {
			int statisticValue = getStatistic(player, statistic, ofType);

			switch (mode) {
				case SET -> applyStatistic(player, statistic, value, ofType);
				case ADD -> applyStatistic(player, statistic, statisticValue + value, ofType);
				case REMOVE -> applyStatistic(player, statistic, statisticValue - value, ofType);
				case DELETE, RESET -> applyStatistic(player, statistic, 0, ofType);
			}
		}
	}

	public int getStatistic(OfflinePlayer player, Statistic statistic, Object type) {
		if (type instanceof ItemType item && (statistic.getType() == Statistic.Type.ITEM
			|| statistic.getType() == Statistic.Type.BLOCK)) {
			return player.getStatistic(statistic, item.getMaterial());
		} else if (type instanceof EntityData<?> data && statistic.getType() == Statistic.Type.ENTITY) {
			return player.getStatistic(statistic, EntityUtils.toBukkitEntityType(data));
		}

		if (this.ofType != null && statistic.getType() == Statistic.Type.UNTYPED)
			warning("The statistic '" + statistic + "' does not require an entity data or item type to be specified, "
				+ "so the specified entity data or item type will be ignored");

		return player.getStatistic(statistic);
	}

	public void applyStatistic(OfflinePlayer player, Statistic statistic, Integer value, Object type) {
		if (value < 0) {
			error("Cannot set a statistic value to a negative number");
			return;
		}

		if (type instanceof ItemType item && (statistic.getType() == Statistic.Type.ITEM
			|| statistic.getType() == Statistic.Type.BLOCK)) {
			player.setStatistic(statistic, item.getMaterial(), value);
		} else if (type instanceof EntityData<?> data && statistic.getType() == Statistic.Type.ENTITY) {
			player.setStatistic(statistic, EntityUtils.toBukkitEntityType(data), value);
		}

		if (this.ofType != null && statistic.getType() == Statistic.Type.UNTYPED)
			warning("The statistic '" + statistic + "' does not require an entity data or item type to be specified, "
				+ "so the specified entity data or item type will be ignored");

		player.setStatistic(statistic, value);
	}

	@Override
	public boolean isSingle() {
		return player.isSingle();
	}

	@Override
	public Class<? extends Integer> getReturnType() {
		return Integer.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		SyntaxStringBuilder builder = new SyntaxStringBuilder(event, debug);

		builder.append("the statistic value of", statistic, "from", player);
		if (ofType != null)
			builder.append("for", ofType);

		return builder.toString();
	}

	@Override
	public @NotNull ErrorSource getErrorSource() {
		return ErrorSource.fromNodeAndElement(parser.getNode(), this);
	}

}
