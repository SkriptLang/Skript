package ch.njol.skript.util.scoreboard;

import ch.njol.skript.Skript;
import ch.njol.skript.util.Utils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Criteria;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.ApiStatus;

import java.util.*;
import java.util.function.Consumer;

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

	public static Collection<Object> getMembers(Team team) {
		return new MemberSet(team);
	}

	private static boolean probablyUUID(String string) {
		if (string.length() == 36) { // well-formed uuid
			return string.charAt(8) == '-'
				&& string.charAt(13) == '-'
				&& string.charAt(18) == '-'
				&& string.charAt(23) == '-';
		} else if (string.length() > 36) { // too long
			return false;
		} // something with exactly four dashes, probably a UUID
		int dash = -1;
		for (int i = 0; i < 4; i++) {
			if ((dash = string.indexOf('-', dash + 1)) < 0)
				return false;
		}
		return string.indexOf('-', dash + 1) == -1;
	}

	public static String getPrefix(Team team) {
		if (team == null || team.getPrefix() == null)
			return null;
		return Utils.replaceChatStyles(team.getPrefix());
	}

	public static String getSuffix(Team team) {
		if (team == null || team.getSuffix() == null)
			return null;
		return Utils.replaceChatStyles(team.getSuffix());
	}

	public static String getTeamPrefix(Player player) {
		Team team = Bukkit.getScoreboardManager().getMainScoreboard().getTeam(player.getName());
		if (team != null)
			return getPrefix(team);
		return null;
	}

	public static String getTeamSuffix(Player player) {
		Team team = Bukkit.getScoreboardManager().getMainScoreboard().getTeam(player.getName());
		if (team != null)
			return getSuffix(team);
		return null;
	}

	protected static class MemberSet extends AbstractSet<Object> {
		protected final Team team;

		public MemberSet(Team team) {
			this.team = team;
		}

		@Override
		public boolean add(Object object) {
			this.team.addEntry(toEntry(object));
			return true;
		}

		@Override
		public Iterator<Object> iterator() {
			Set<String> entries = team.getEntries();
			Iterator<String> backer = entries.iterator();
			class EntryIterator implements Iterator<Object> {

				private String entry;

				@Override
				public boolean hasNext() {
					return backer.hasNext();
				}

				@Override
				public Object next() {
					this.entry = backer.next();
					if (probablyUUID(entry)) {
						return Bukkit.getEntity(UUID.fromString(entry));
					} else {
						OfflinePlayer player = Bukkit.getOfflinePlayerIfCached(entry);
						if (player != null)
							return player;
						// We almost never want to do this, it's our last line of defence
						// against null-ness
						return Bukkit.getOfflinePlayer(entry);
					}
				}

				@Override
				public void remove() {
					team.removeEntry(entry);
				}

			}
			return new EntryIterator();
		}

		@Override
		public int size() {
			return team.getSize();
		}

	}

}
