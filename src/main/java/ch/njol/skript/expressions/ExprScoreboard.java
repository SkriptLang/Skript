package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.bukkit.scoreboard.Scoreboard;
import org.jetbrains.annotations.Nullable;

// TODO doc
public class ExprScoreboard extends SimpleExpression<Scoreboard> {

	static {
		Skript.registerExpression(ExprScoreboard.class, Scoreboard.class, ExpressionType.SIMPLE,
			"[the] [main|server] scoreboard",
			"[a] new scoreboard"
		);
	}

	private boolean main;

	@Override
	public boolean init(Expression<?>[] expressions, int pattern, Kleenean delayed, ParseResult result) {
		this.main = pattern == 0;
		return true;
	}

	@Override
	protected @Nullable Scoreboard[] get(Event event) {
		if (main)
			return CollectionUtils.array(Bukkit.getScoreboardManager().getMainScoreboard());
		return CollectionUtils.array(Bukkit.getScoreboardManager().getNewScoreboard());
	}

	@Override
	public Class<? extends Scoreboard> getReturnType() {
		return Scoreboard.class;
	}

	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		if (main)
			return "the main scoreboard";
		return "a new scoreboard";
	}

	/**
	 * Converts something into its string entry representation on a scoreboard.
	 * Entities use their UUID. (Offline) players use their name.
	 * @param object Either an entity or an offline player
	 * @return The string entry name
	 */
	public static String toEntry(Object object) {
		if (object instanceof OfflinePlayer)
			return ((OfflinePlayer) object).getName();
		if (object instanceof Entity)
			return ((Entity) object).getUniqueId().toString();
		assert object instanceof String; // May be a user text input or a bad value, but attempt it anyway
		return String.valueOf(object); // It's fairly harmless, even if it's wrong
	}

}
