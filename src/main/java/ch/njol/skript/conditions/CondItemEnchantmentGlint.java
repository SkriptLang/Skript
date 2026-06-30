package ch.njol.skript.conditions;

import ch.njol.skript.classes.Changer.ChangeMode;
import org.bukkit.inventory.meta.ItemMeta;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.*;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;

import java.util.Arrays;
import java.util.stream.Stream;

@Name("Item Has Enchantment Glint Override")
@Description("Checks whether an item has the enchantment glint overridden, or is forced to glint or not.")
@Example("""
	if the player's tool has the enchantment glint override
		send "Your tool has the enchantment glint override." to player
	""")
@Example("""
	if {_item} is forced to glint:
		send "This item is forced to glint." to player
	else if {_item} is forced to not glint:
		send "This item is forced to not glint." to player
	else:
		send "This item does not have any glint override." to player
	""")
@RequiredPlugins("Spigot 1.20.5+")
@Since("2.10")
public class CondItemEnchantmentGlint extends PropertyCondition<ItemType> {

	static {
		if (Skript.methodExists(ItemMeta.class, "getEnchantmentGlintOverride")) {
			Skript.registerCondition(CondItemEnchantmentGlint.class, Stream.concat(
				Arrays.stream(getPatterns(PropertyType.HAVE, "enchantment glint overrid(den|e)", "itemtypes")),
				Arrays.stream(getPatterns(PropertyType.BE, "forced to [:not] glint", "itemtypes"))
			).toArray(String[]::new));
		}
	}

	private boolean override;
	private boolean glintNegated;

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		override = matchedPattern <= 1;
		glintNegated = parseResult.hasTag("not");
		//noinspection unchecked
		setExpr((Expression<? extends ItemType>) expressions[0]);
		setNegated(matchedPattern % 2 == 1);
		return true;
	}

	@Override
	public boolean check(ItemType itemType) {
		ItemMeta meta = itemType.getItemMeta();
		// enchantment glint override
		if (override)
			return meta.hasEnchantmentGlintOverride();
		// forced to glint
		if (!meta.hasEnchantmentGlintOverride())
			return false;
		return meta.getEnchantmentGlintOverride() != glintNegated;
	}

	@Override
	public boolean acceptChange(ChangeMode mode) {
		return mode == ChangeMode.SET;
	}

	@Override
	protected void change(ItemType itemType, boolean glint, ChangeMode mode) {
		ItemMeta meta = itemType.getItemMeta();
		if (override) {
			meta.setEnchantmentGlintOverride(glint ? true : null);
		} else {
			meta.setEnchantmentGlintOverride(glint != glintNegated);
		}
		itemType.setItemMeta(meta);
	}

	@Override
	protected String getPropertyName() {
		if (override)
			return "enchantment glint overridden";
		return "forced to " + (glintNegated ? "not " : "") + "glint";
	}

}
