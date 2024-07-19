package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Nullable;

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
		if (Skript.isRunningMinecraft(1, 20, 5))
			Skript.registerEffect(EffForceEnchantmentGlint.class,
					"(force|make) %itemtypes% [to] [start] glint[ing]",
					"(force|make) %itemtypes% [to] (not|stop) glint[ing]",
					"(clear|delete) [the] enchantment glint override of %itemtypes%",
					"(clear|delete) %itemtypes%'s enchantment glint override");
	}

	@SuppressWarnings("NotNullFieldNotInitialized")
	private Expression<ItemType> itemtypes;
	private int pattern;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		itemtypes = (Expression<ItemType>) expressions[0];
		pattern = matchedPattern;
		return true;
	}

	@Override
	protected void execute(Event event) {
		for (ItemType itemType : itemtypes.getArray(event)) {
			ItemMeta meta = itemType.getItemMeta();
			Boolean glint;
			// Pattern: forced to glint
			if (pattern == 0) {
				glint = true;
			// Pattern: forced to not glint
			} else if (pattern == 1) {
				glint = false;
			// Pattern: Clear glint override
			} else {
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
			return "clear the enchantment glint override of " + itemtypes.toString(event, debug);
		return "force the " + itemtypes.toString(event, debug) + " to " + (pattern == 0 ? "start" : "stop") + " glinting";
	}

}
