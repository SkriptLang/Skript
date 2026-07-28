package ch.njol.skript.structures;

import ch.njol.skript.Skript;
import ch.njol.skript.config.EntryNode;
import ch.njol.skript.config.Node;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.StringUtils;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.entry.EntryContainer;
import org.skriptlang.skript.lang.script.Script;
import org.skriptlang.skript.lang.script.ScriptData;
import org.skriptlang.skript.lang.structure.Structure;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;

@Name("Options")
@Description({
	"Options are used for replacing parts of a script with something else.",
	"For example, an option may represent a message that appears in multiple locations.",
	"Take a look at the example below that showcases this.",
	"Options defined in one script are shared with, and can be used by, every other loaded script " +
		"(under the default, synchronous loading order, a script can only use options that were " +
		"declared in a script that loads before it - e.g. one that sorts earlier alphabetically)."
})
@Example("""
	options:
		no_permission: You're missing the required permission to execute this command!

	command /ping:
		permission: command.ping
		permission message: {@no_permission}
		trigger:
			message "Pong!"

	command /pong:
		permission: command.pong
		permission message: {@no_permission}
		trigger:
			message "Ping!"
	""")
@Since("1.0")
public class StructOptions extends Structure {

	public static final Priority PRIORITY = new Priority(100);

	static {
		Skript.registerStructure(StructOptions.class, "options");
	}

	@Override
	public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult, @Nullable EntryContainer entryContainer) {
		// noinspection ConstantConditions - entry container cannot be null as this structure is not simple
		SectionNode node = entryContainer.getSource();
		node.convertToEntries(-1);
		loadOptions(node, "", getParser().getCurrentScript().getData(OptionsData.class, OptionsData::new).options);
		return true;
	}

	private void loadOptions(SectionNode sectionNode, String prefix, Map<String, String> options) {
		for (Node node : sectionNode) {
			if (node instanceof EntryNode) {
				options.put(prefix + node.getKey(), ((EntryNode) node).getValue());
			} else if (node instanceof SectionNode) {
				loadOptions((SectionNode) node, prefix + node.getKey() + ".", options);
			} else {
				Skript.error("Invalid line in options");
			}
		}
	}

	@Override
	public boolean load() {
		return true;
	}

	@Override
	public void unload() {
		getParser().getCurrentScript().removeData(OptionsData.class);
	}

	@Override
	public Priority getPriority() {
		return PRIORITY;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "options";
	}

	public static final class OptionsData implements ScriptData {

		/**
		 * Shared across every script that has been loaded (in this loading session), rather than kept
		 * per-script. This is what allows an {@code options:} section declared in one .sk file to be used
		 * with {@code {@option}} syntax in any other .sk file, as long as that other file is (re)loaded
		 * after the options were registered.
		 * <p>
		 * A {@link ConcurrentHashMap} is used since, depending on server configuration, scripts may be
		 * parsed on multiple threads concurrently (see {@code script loader thread size} in the config).
		 */
		private static final Map<String, String> options = new ConcurrentHashMap<>();

		/**
		 * Replaces all options in the provided String using the shared, global option mappings.
		 * @param string The String to replace options in.
		 * @return A String with all options replaced, or the original String if no options are defined.
		 */
		@SuppressWarnings("ConstantConditions") // no way to get null as callback does not return null anywhere
		public String replaceOptions(String string) {
			return StringUtils.replaceAll(string, "\\{@(.+?)\\}", m -> {
				String option = options.get(m.group(1));
				if (option == null) {
					Skript.error("undefined option " + m.group());
					return m.group();
				}
				return Matcher.quoteReplacement(option);
			});
		}

		/**
		 * @return An unmodifiable version of the shared, global option mappings.
		 */
		public Map<String, String> getOptions() {
			return Collections.unmodifiableMap(options);
		}

	}

}
