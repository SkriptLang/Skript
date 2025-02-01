package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.bukkitutil.EntityUtils;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.config.Node;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.entity.EntityData;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;

import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.skript.lang.parser.ParserInstance;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.registrations.Classes;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.OfflinePlayer;
import org.bukkit.Statistic;
import org.bukkit.Statistic.Type;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.log.runtime.*;

@Name("Player Statistics")
@Description({
	"Get or set the statistics of a player.",
	"Some statistics require an entity type or item type to be specified. "
		+ "For example, the 'KILL_ENTITY' statistic requires an entity type. You can see more about this in "
		+ "<a href='https://www.digminecraft.com/getting_started/statistics.php'>the DigMinecraft page about statistics</a>."
})
@Examples({
	"set {_stat} to \"kill entity\" parsed as a statistic",
	"set the statistic {_stat} for a pig of player to 10",
	"add 5 to the statistic {_stat} for a pig of player",
	"broadcast \"You have left the game %statistic \"leave game\" parsed as statistic of player% times!\""
})
@Since("INSERT VERSION")
public class ExprPlayerStatistics extends SimpleExpression<Integer> implements RuntimeErrorProducer {

	static {
		Skript.registerExpression(ExprPlayerStatistics.class, Integer.class, ExpressionType.COMBINED,
			"[the] statistic %statistic% [for %-entitydata/itemtype%] (from|of) %offlineplayers%",
			"%offlineplayers%'[s] statistic %statistic% [for %-entitydata/itemtype%]"
		);
	}

	private Expression<Statistic> statistic;
	private Expression<OfflinePlayer> player;
	private Expression<?> ofType;

	private Node node;

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
		node = getParser().getNode();

		return true;
	}

	@Override
	protected Integer @Nullable [] get(Event event) {
		Statistic statistic = this.statistic.getSingle(event);
		if (statistic == null)
			return null;

		Object ofType = this.ofType == null ? null : this.ofType.getSingle(event);

		if (!shouldContinue(statistic, ofType))
			return null;

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

		if (!shouldContinue(statistic, ofType))
			return;

		int value = delta == null ? 0 : (Integer) delta[0];

		for (OfflinePlayer player : this.player.getArray(event)) {
			switch (mode) {
				case SET -> applyStatistic(player, statistic, value, ofType);
				case ADD -> applyStatistic(player, statistic, getStatistic(player, statistic, ofType) + value, ofType);
				case REMOVE -> applyStatistic(player, statistic, getStatistic(player, statistic, ofType) - value, ofType);
				case DELETE, RESET -> applyStatistic(player, statistic, 0, ofType);
			}
		}
	}

	private int getStatistic(OfflinePlayer player, Statistic statistic, Object type) {
		Type statisticType = statistic.getType();
		if (type instanceof ItemType item && (statisticType == Type.ITEM || statisticType == Type.BLOCK)) {
			return player.getStatistic(statistic, item.getMaterial());
		} else if (type instanceof EntityData<?> data && statisticType == Type.ENTITY) {
			return player.getStatistic(statistic, EntityUtils.toBukkitEntityType(data));
		}

		return player.getStatistic(statistic);
	}

	private void applyStatistic(OfflinePlayer player, Statistic statistic, Integer value, Object type) {
		if (value < 0) {
			error("Cannot set the statistic '" + statistic + "' to '" + value + "' because it is a negative number.");
			return;
		}

		Type statisticType = statistic.getType();

		if (type instanceof ItemType item && (statisticType == Type.ITEM || statisticType == Type.BLOCK)) {
			player.setStatistic(statistic, item.getMaterial(), value);
		} else if (type instanceof EntityData<?> data && statisticType == Type.ENTITY) {
			player.setStatistic(statistic, EntityUtils.toBukkitEntityType(data), value);
		} else {
			player.setStatistic(statistic, value);
		}
	}

	private boolean shouldContinue(Statistic statistic, Object ofType) {
		Type statisticType = statistic.getType();
		if (ofType == null && statisticType != Type.UNTYPED) {
			error("The statistic '" + statistic + "' requires an entity data or item type to be specified.");
			return false;
		} else if (this.ofType != null && statisticType == Type.UNTYPED) {
			warning("The statistic '" + statistic + "' does not require an entity data or item type to be provided, "
				+ "so it will be ignored.");
		}

		if (ofType instanceof ItemType && statisticType == Type.ENTITY) {
			error("The statistic '" + statistic + "' requires an entity data, but '" + Classes.toString(ofType) + "' was provided.");
			return false;
		} else if (ofType instanceof EntityData && (statisticType == Type.ITEM || statisticType == Type.BLOCK)) {
			error("The statistic '" + statistic + "' requires an item type, but '" + Classes.toString(ofType) + "' was provided.");
			return false;
		}

		return true;
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
		return ErrorSource.fromNodeAndElement(node, this);
	}

}
