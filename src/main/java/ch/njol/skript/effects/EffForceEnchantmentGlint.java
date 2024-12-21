package ch.njol.skript.effects;

import org.bukkit.event.Event;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.doc.*;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;

@Name("Force Enchantment Glint")
@Description("Forces the items to glint or not, or removes its existing enchantment glint enforcement.")
@Examples({
	"force {_items::*} to glint",
	"force the player's tool to stop glinting"
})
@RequiredPlugins("Spigot 1.20.5+")
@Since("INSERT VERSION")
public class EffForceEnchantmentGlint extends Effect {

	static {
		if (Skript.methodExists(ItemMeta.class, "setEnchantmentGlintOverride", Boolean.class))
			Skript.registerEffect(EffForceEnchantmentGlint.class,
					"(force|make) %itemtypes% [to] [start] glint[ing]",
					"(force|make) %itemtypes% [to] (not|stop) glint[ing]",
					"(clear|delete|reset) [the] enchantment glint override of %itemtypes%",
					"(clear|delete|reset) %itemtypes%'s enchantment glint override");
	}

	private Expression<ItemType> itemTypes;
	private int pattern;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		itemTypes = (Expression<ItemType>) expressions[0];
		pattern = matchedPattern;
		return true;
	}

	@Override
	protected void execute(Event event) {
		for (ItemType itemType : itemTypes.getArray(event)) {
			ItemMeta meta = itemType.getItemMeta();
			Boolean glint;
			if (pattern == 0) {
				// Pattern: forced to glint
				glint = true;
			} else if (pattern == 1) {
				// Pattern: forced to not glint
				glint = false;
			} else {
				// Pattern: Clear glint override
				glint = null;
			}
			meta.setEnchantmentGlintOverride(glint);
			itemType.setItemMeta(meta);
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		// Pattern: Clear glint override
		if (pattern > 1)
			return "clear the enchantment glint override of " + itemTypes.toString(event, debug);
		return "force " + itemTypes.toString(event, debug) + " to " + (pattern == 0 ? "start" : "stop") + " glinting";
	}

}
