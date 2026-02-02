package ch.njol.skript.structures;

import ch.njol.skript.Skript;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.OptionRegistry;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.entry.EntryContainer;
import org.skriptlang.skript.lang.script.Script;
import org.skriptlang.skript.lang.script.ScriptData;
import org.skriptlang.skript.lang.structure.Structure;

import java.util.Map;

@Name("Options")
@Description({
	"Options are used for replacing parts of a script with something else.",
	"For example, an option may represent a message that appears in multiple locations.",
	"Take a look at the example below that showcases this."
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
		Script script = getParser().getCurrentScript();
		// noinspection ConstantConditions - entry container cannot be null as this structure is not simple
		SectionNode node = entryContainer.getSource();
		node.convertToEntries(-1);
		OptionRegistry optionRegistry = Skript.instance().registry(OptionRegistry.class);
		optionRegistry.loadLocalOptions(script, node);
		script.getData(OptionsData.class, () -> new OptionsData(script));
		return true;
	}

	@Override
	public boolean load() {
		return true;
	}

	@Override
	public void unload() {
		Script script = getParser().getCurrentScript();
		OptionRegistry optionRegistry = Skript.instance().registry(OptionRegistry.class);
		optionRegistry.deleteLocalOptions(script);
		script.removeData(OptionsData.class);
	}

	@Override
	public Priority getPriority() {
		return PRIORITY;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "options";
	}

	/**
	 * @deprecated Use {@code Skript.instance().registry(OptionRegistry.class)} instead.
	 */
	@Deprecated(since = "INSERT VERSION", forRemoval = true)
	public static final class OptionsData implements ScriptData {

		private final Script script;

		public OptionsData(Script script) {
			this.script = script;
		}

		/**
		 * Replaces all options in the provided String using the options of this data.
		 *
		 * @param string The String to replace options in.
		 * @return A String with all options replaced, or the original String if the provided Script has no options.
		 * @deprecated Use <code>Skript.instance().registry(OptionRegistry.class).replaceOptions()</code> instead
		 */
		@Deprecated(since = "INSERT VERSION", forRemoval = true)
		public String replaceOptions(String string) {
			return Skript.instance().registry(OptionRegistry.class).replaceOptions(script, string);
		}

		/**
		 * @return An unmodifiable version of this data's option mappings.
		 * @deprecated Use <code>Skript.instance().registry(OptionRegistry.class).getLocalOptions()</code> instead
		 */
		@Deprecated(since = "INSERT VERSION", forRemoval = true)
		public Map<String, String> getOptions() {
			return Skript.instance().registry(OptionRegistry.class).getLocalOptions(script);
		}

	}

}
