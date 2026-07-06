package ch.njol.skript.command;

import ch.njol.skript.SkriptConfig;
import ch.njol.skript.util.Date;
import ch.njol.skript.util.Timespan.TimePeriod;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Builder for multiline effect commands.
 */
public class MultiEffectBuilder {

	private static final Map<CommandSender, MultiEffectBuilder> BUILDERS = new HashMap<>();

	/**
	 * Whether there is a current {@link MultiEffectBuilder} for {@code sender}.
	 * @param sender The {@link CommandSender} to check for.
	 * @return {@code true} if one exists, otherwise {@code false}.
	 */
	static boolean hasBuilder(CommandSender sender) {
		return BUILDERS.containsKey(sender);
	}

	/**
	 * Gets the current or constructs a new {@link MultiEffectBuilder} for {@code sender}.
	 * @param sender The {@link CommandSender} to get the builder for.
	 * @return The {@link MultiEffectBuilder}.
	 */
	static MultiEffectBuilder getBuilder(CommandSender sender) {
		if (BUILDERS.containsKey(sender)) {
			MultiEffectBuilder builder = BUILDERS.get(sender);
			long timeout = SkriptConfig.multilineEffectTimeout.value().getAs(TimePeriod.SECOND);
			if (timeout > 0 && builder.lastUsed.difference(Date.now()).getAs(TimePeriod.SECOND) < timeout)
				return builder;
		}
		MultiEffectBuilder builder = new MultiEffectBuilder(sender);
		BUILDERS.put(sender, builder);
		return builder;
	}

	/**
	 * Removes the {@link MultiEffectBuilder} linked to {@code sender}.
	 * @param sender The {@link CommandSender} to remove the builder from.
	 */
	static void removeBuilder(CommandSender sender) {
		BUILDERS.remove(sender);
	}

	private final CommandSender sender;
	private final List<String> lines = new ArrayList<>();
	private Date lastUsed = null;

	private MultiEffectBuilder(CommandSender sender) {
		this.sender = sender;
	}

	/**
	 * @return Returns all the lines currently stored in {@code this}.
	 */
	public List<String> getLines() {
		return lines;
	}

	/**
	 * Adds {@code line} to the stored lines of {@code this}.
	 * @param line The line to add.
	 */
	public void addLine(String line) {
		lastUsed = Date.now();
		lines.add(line);
	}

	/**
	 * @return Returns all lines joined into one string with the delimiter of "\n".
	 */
	public String joinLines() {
		return joinLines(0, "\t");
	}

	/**\
	 * Returns all lines joined into one {@link String} and indented with {@code tab} by {@code indent} times.
	 * @param indent The amount to indent each line by.
	 * @param tab The {@link String} to use for indentation.
	 * @return The combined {@link String}.
	 */
	public String joinLines(int indent, String tab) {
		return String.join("\n", lines.stream()
			.map(string -> tab.repeat(indent) + string.replaceAll("\t", tab))
			.toList());
	}

	/**
	 * Builds a {@link MultiEffectCommandEvent} and removes {@code this} from the active builders.
	 * @return The {@link MultiEffectCommandEvent}.
	 */
	public MultiEffectCommandEvent build() {
		removeBuilder(sender);
		return new MultiEffectCommandEvent(sender, joinLines());
	}

}
