package ch.njol.skript.util.scoreboard;

import org.bukkit.scoreboard.Criteria;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

/**
 * A holder for {@link org.bukkit.scoreboard.Criteria} for versions where it is unavailable.
 */
public class Criterion implements CharSequence {

	private final String pattern;
	private final String name;
	private final @Nullable Object handle;

	public Criterion(String pattern, String name) {
		this(pattern, name, null);
	}

	public Criterion(String pattern, String name, @Nullable Object handle) {
		this.pattern = pattern;
		this.name = name;
		this.handle = handle;
	}

	public String pattern() {
		return pattern;
	}

	public String name() {
		return name;
	}

	public @NotNull Set<Objective> getObjectives(Scoreboard scoreboard) {
		if (handle != null) {
			return scoreboard.getObjectivesByCriteria((Criteria) handle);
		}
		return scoreboard.getObjectivesByCriteria(name);
	}

	public void registerObjective(Scoreboard scoreboard, String name) {
		if (handle != null)
			scoreboard.registerNewObjective(name, (Criteria) handle, name);
		else
			scoreboard.registerNewObjective(name, this.name);
	}

	@Override
	public int length() {
		return pattern.length();
	}

	@Override
	public char charAt(int index) {
		return pattern.charAt(index);
	}

	@NotNull
	@Override
	public CharSequence subSequence(int start, int end) {
		return pattern.subSequence(start, end);
	}

	@Override
	public @NotNull String toString() {
		return pattern;
	}

}
