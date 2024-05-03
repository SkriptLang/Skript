package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.bukkit.inventory.meta.ItemMeta;
import org.eclipse.jdt.annotation.Nullable;

public class EffForceEnchantmentGlint extends Effect {

	static {
		Skript.registerEffect(EffForceEnchantmentGlint.class,
			"(force|make) %itemtypes% to (:start|stop|not) glint[ing]"
			);
	}

	private Expression<ItemType> itemtypes;
	private boolean glint;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		itemtypes = (Expression<ItemType>) expressions[0];
		glint = parseResult.hasTag("start");
		return true;
	}

	@Override
	protected void execute(Event event) {
		for (ItemType itemType : this.itemtypes.getArray(event)) {
			ItemMeta meta = itemType.getItemMeta();
			meta.setEnchantmentGlintOverride(glint);
			itemType.setItemMeta(meta);
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "force the " + itemtypes.toString(event, debug) + " to " + (glint ? "start" : "stop") + " glinting";
	}

}
