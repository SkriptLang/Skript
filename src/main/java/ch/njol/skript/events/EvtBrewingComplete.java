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
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class EvtBrewingComplete extends SkriptEvent {

	static {
		Skript.registerEvent("Brewing Complete", EvtBrewingComplete.class, BrewEvent.class,
				"brew[ing] [complet[ed|ion]|finish[ed]] [(of|for) %-itemtypes/potioneffecttypes%]")
			.description("Called when a brewing stand finishes brewing an ingredient and changes the potions.")
			.examples("""
				on brew:
					broadcast event-item
				on brewing of speed potion:
				on brew finished for speed 2 potion:
				""")
			.examples(
				"on brew:",
					"\tbroadcast event-items",
				"on brewing of speed potion:"
			)
			.since("INSERT VERSION");
	}

	private @Nullable Literal<?> types;

	@Override
	public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) {
		types = args[0];
		return true;
	}

	@Override
	public boolean check(Event event) {
		if (!(event instanceof BrewEvent brewEvent))
			return false;
		if (types == null)
			return true;

		List<ItemStack> itemStacks = brewEvent.getResults();
		for (Object object : types.getArray()) {
			if (object instanceof ItemType itemType) {
				for (ItemStack itemStack : itemStacks) {
					if (itemType.isOfType(itemStack))
						return true;
				}
			} else if (object instanceof PotionEffectType potionEffectType) {
				for (ItemStack itemStack : itemStacks) {
					if (itemStack.getItemMeta() instanceof PotionMeta potionMeta) {
						for (PotionEffect potionEffect : potionMeta.getAllEffects()) {
							if (potionEffect.getType() == potionEffectType)
								return true;
						}
					}
				}
			}
		}
		return false;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		SyntaxStringBuilder builder = new SyntaxStringBuilder(event, debug);
		builder.append("brewing complete");
		if (types != null)
			builder.append("for", types);
		return builder.toString();
	}

}
