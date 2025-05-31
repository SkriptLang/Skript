package ch.njol.skript.events;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxStringBuilder;
import org.bukkit.event.Event;
import org.bukkit.event.inventory.BrewEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class EvtBrewingComplete extends SkriptEvent {

	static {
		Skript.registerEvent("Brewing Complete", EvtBrewingComplete.class, BrewEvent.class,
				"brew[ing] [complet[ed|ion]] [(of|for) %-itemtypes%]",
				"brew[ing] [finish[ed]] [(of|for) %-itemtypes%]")
			.description("Called when a brewing stand finishes brewing the ingredient and changes the potions.")
			.examples(
				"on brew:",
					"\tbroadcast event-items",
				"on brewing of speed potion:"
			)
			.since("INSERT VERSION");
	}

	private @Nullable Literal<ItemType> items;

	@Override
	public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) {
		//noinspection unchecked
		items = (Literal<ItemType>) args[0];
		return true;
	}

	@Override
	public boolean check(Event event) {
		if (!(event instanceof BrewEvent brewEvent))
			return false;
		if (items == null)
			return true;

		List<ItemStack> itemStacks = brewEvent.getResults();
		for (ItemType itemType : items.getArray()) {
			for (ItemStack itemStack : itemStacks) {
				if (itemType.isOfType(itemStack))
					return true;
			}
		}
		return false;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		SyntaxStringBuilder builder = new SyntaxStringBuilder(event, debug);
		builder.append("brewing complete");
		if (items != null)
			builder.append("for", items);
		return builder.toString();
	}

}
