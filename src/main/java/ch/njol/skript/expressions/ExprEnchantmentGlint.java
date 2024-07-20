package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.event.Event;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Nullable;

@Name("Enchantment Glint")
@Description({
	"Sets the 'enchantment_glint_override' on items.",
	"If true, the item will glint, even without enchantments.",
	"if false, the item will not glint, even with enchantments.",
	"If cleared, the glint enforcement will be cleared."
})
@Examples({
	"set the enchantment glint of player's tool to true",
	"set the enchantment glint of {_items::*} to false",
	"clear the enchantment glint of player's tool"
})
@RequiredPlugins("Spigot 1.20.5+")
@Since("INSERT VERSION")
public class ExprEnchantmentGlint extends SimplePropertyExpression<ItemType, Boolean> {

	static {
		if (Skript.isRunningMinecraft(1, 20, 5))
			register(ExprEnchantmentGlint.class, Boolean.class, "enchantment glint", "itemtypes");
	}

	@Override
	@Nullable
	public Boolean convert(ItemType item) {
		ItemMeta meta = item.getItemMeta();
		if (!meta.hasEnchantmentGlintOverride())
			return null;
		// Spigot claims this does not return null, hence we return null ourselves
		return meta.getEnchantmentGlintOverride();
	}

	@Override
	@Nullable
	public Class<?>[] acceptChange(ChangeMode mode) {
		switch (mode) {
			case SET:
			case DELETE:
			case RESET:
				return CollectionUtils.array(Boolean.class);
			default:
				return null;
		}
	}

	@Override
	public void change(Event event, @Nullable Object[] delta, ChangeMode mode) {
		switch (mode) {
			case SET:
				if (!(delta[0] instanceof Boolean))
					return;
				for (ItemType itemType : getExpr().getArray(event)) {
					ItemMeta meta = itemType.getItemMeta();
					meta.setEnchantmentGlintOverride((Boolean) delta[0]);
					itemType.setItemMeta(meta);
				}
				break;
			case DELETE:
			case RESET:
				for (ItemType itemType : getExpr().getArray(event)) {
					ItemMeta meta = itemType.getItemMeta();
					meta.setEnchantmentGlintOverride(null);
					itemType.setItemMeta(meta);
				}
		}
	}

	@Override
	public Class<? extends Boolean> getReturnType() {
		return Boolean.class;
	}

	@Override
	protected String getPropertyName() {
		return "enchantment glint";
	}

}
