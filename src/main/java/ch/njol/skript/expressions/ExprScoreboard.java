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
import org.bukkit.scoreboard.Criteria;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

// TODO doc
public class ExprScoreboard extends SimpleExpression<Scoreboard> {

	public static final boolean ARE_CRITERIA_AVAILABLE; // todo remove in 2.10?

	static {
		ARE_CRITERIA_AVAILABLE = Skript.classExists("org.bukkit.scoreboard.Criteria");
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
	@ApiStatus.Internal
	public static String toEntry(Object object) {
		if (object instanceof OfflinePlayer)
			return ((OfflinePlayer) object).getName();
		if (object instanceof Entity)
			return ((Entity) object).getUniqueId().toString();
		assert object instanceof String; // May be a user text input or a bad value, but attempt it anyway
		return String.valueOf(object); // It's fairly harmless, even if it's wrong
	}

	/**
	 * Gets objectives based on string (or Criteria) criteria
	 */
	@ApiStatus.Internal
	public static Collection<Objective> getObjectivesByCriteria(Scoreboard scoreboard, Object... criteria) {
		Set<Objective> objectives = new HashSet<>();
		if (ARE_CRITERIA_AVAILABLE) {
			for (Object criterion : criteria) {
				if (criterion instanceof Criteria)
					objectives.addAll(scoreboard.getObjectivesByCriteria((Criteria) criterion));
			}
		} else {
			for (Object criterion : criteria) {
				if (criterion != null)
					objectives.addAll(scoreboard.getObjectivesByCriteria(String.valueOf(criterion)));
			}

		}
		return objectives;
	}

}
