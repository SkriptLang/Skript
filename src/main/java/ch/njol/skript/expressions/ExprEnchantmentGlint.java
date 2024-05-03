package ch.njol.skript.expressions;

import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.event.Event;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Nullable;

public class ExprEnchantmentGlint extends SimplePropertyExpression<ItemType, Boolean> {

	static {
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
				return CollectionUtils.array(Boolean.class);
			default:
				return null;
		}
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
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
