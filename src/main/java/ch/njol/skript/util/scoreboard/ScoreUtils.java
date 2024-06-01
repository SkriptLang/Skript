package ch.njol.skript.util.scoreboard;

import ch.njol.skript.Skript;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Entity;
import org.bukkit.scoreboard.Criteria;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.jetbrains.annotations.ApiStatus;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class ScoreUtils {

	public static final boolean ARE_CRITERIA_AVAILABLE; // todo remove in 2.10?

	static {
		ARE_CRITERIA_AVAILABLE = Skript.classExists("org.bukkit.scoreboard.Criteria");
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
	 * Gets objectives based on criteria.
	 */
	@ApiStatus.Internal
	public static Collection<Objective> getObjectivesByCriteria(Scoreboard scoreboard, Criterion... criteria) {
		Set<Objective> objectives = new HashSet<>();
		for (Criterion criterion : criteria) {
			if (criterion != null)
				objectives.addAll(criterion.getObjectives(scoreboard));
		}
		return objectives;
	}

}
