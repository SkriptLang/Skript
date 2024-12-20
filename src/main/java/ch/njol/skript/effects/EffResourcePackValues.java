package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.sections.EffSecSendResourcePack.ResourcePackEvent;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

@Name("Resource Pack Values")
@Description({
	"Optional values to be used when sending a resource pack. " +
	"These can only be used in a Resource Pack Effect Section",
	"",
	"The UUID is used to identify the resource pack. Useful when removing packs from players.",
	"UUIDs must be valid using the following format: ",
	"\"00000000-0000-0000-0000-000000000000\"",
	"",
	"The hash is used for caching, so that the player doesn't have to re-download the resource pack each time they join. ",
	"The hash must be SHA-1, you can get the SHA-1 hash of your resource pack using " +
    "<a href=\\\"https://emn178.github.io/online-tools/sha1_checksum.html\\\">this online tool</a>.",
	"",
	"The prompt can be used to display a custom message when a players is prompted" +
	"to download the sent resource pack",
	"",
	"The force option, forces the player to accept the request",
})
@Examples({
	"on join:",
		"\tsend the resource pack from \"URL\" to the player:",
			"\t\tset the resource pack uuid to \"00000000-0000-0000-0000-000000000001\"",
			"\t\tset the resource pack hash to \"Hash\"",
			"\t\tset the resource pack prompt to \"Please Download\"",
			"\t\tforce the player to accept"
})
@Since("INSERT VERSION")
public class EffResourcePackValues extends Effect {

	static {
		Skript.registerEffect(EffResourcePackValues.class,
			"set [the] [[resource] [pack]] [uu]id to %string%",
			"set [the] [[resource] [pack]] hash to %string%",
			"set [the] [[resource] [pack]] prompt [message] to %string%",
			"force [the] player[s] to accept"
		);
	}

	private @UnknownNullability Expression<String> value;
	private int pattern;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		if (!getParser().isCurrentEvent(ResourcePackEvent.class)) {
			Skript.error("This can only be used in the Resource Pack Effect Section");
			return false;
		}
		pattern = matchedPattern;
		if (exprs.length == 1) {
			value = (Expression<String>) exprs[0];
		}
		return true;
	}

	@Override
	protected void execute(Event event) {
		ResourcePackEvent resourcePackEvent = (ResourcePackEvent) event;
		String value = this.value == null ? null : this.value.getSingle(event);
		if (this.value != null && value == null)
			return;
		switch (pattern) {
			case 0 -> resourcePackEvent.setId(value);
			case 1 -> resourcePackEvent.setHash(value);
			case 2 -> resourcePackEvent.setPrompt(value);
			case 3 -> resourcePackEvent.setForce(true);
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		String value = this.value == null ? null : this.value.toString(event, debug);
		return switch (pattern) {
			case 0 -> "set the resource pack id to " + value;
			case 1 -> "set the resource pack hash to " + value;
			case 2 -> "set the resource pack prompt to " + value;
			case 3 -> "force the player to accept";
			default -> "invalid/missing pattern string";
		};
	}


}
