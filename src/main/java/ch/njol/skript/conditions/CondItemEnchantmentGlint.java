package ch.njol.skript.conditions;

import org.bukkit.inventory.meta.ItemMeta;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.*;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;

@Name("Item Has Enchantment Glint Override")
@Description("Checks whether an item has the enchantment glint overridden, or is forced to glint or not.")
@Examples({
	"if the player's tool has the enchantment glint override",
	"\tsend \"Your tool has the enchantment glint override.\" to player",
	"",
	"if {_item} is forced to glint:",
	"\tsend \"This item is forced to glint.\" to player",
	"else if {_item} is forced to not glint:",
	"\tsend \"This item is forced to not glint.\" to player",
	"else:",
	"\tsend \"This item does not have any glint override.\" to player"
})
@RequiredPlugins("Spigot 1.20.5+")
@Since("INSERT VERSION")
public class CondItemEnchantmentGlint extends PropertyCondition<ItemType> {

	static {
		if (Skript.methodExists(ItemMeta.class, "getEnchantmentGlintOverride")) {
			register(CondItemEnchantmentGlint.class, PropertyType.HAVE, "enchantment glint overrid(den|e)", "itemtypes");
			register(CondItemEnchantmentGlint.class, PropertyType.BE, "forced to [:not] glint", "itemtypes");
		}
	}

	private int matchedPattern;

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		this.matchedPattern = matchedPattern;
		return super.init(expressions, matchedPattern, isDelayed, parseResult);
	}

	@Override
	public boolean check(ItemType itemType) {
		ItemMeta meta = itemType.getItemMeta();
		// enchantment glint override
		if (matchedPattern == 0)
			return meta.hasEnchantmentGlintOverride();
		// forced to glint
		if (!meta.hasEnchantmentGlintOverride())
			return false;
		return meta.getEnchantmentGlintOverride();
	}

	@Override
	protected String getPropertyName() {
		if (matchedPattern == 0)
			return "enchantment glint overridden";
		return "forced to " + (isNegated() ? "not " : "") + "glint";
	}

}
