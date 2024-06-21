/**
 *   This file is part of Skript.
 *
 *  Skript is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Skript is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Skript.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright Peter Güttinger, SkriptLang team and contributors
 */
package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.util.slot.Slot;
import ch.njol.util.coll.CollectionUtils;
import org.jetbrains.annotations.Nullable;

@Name("Damage Value/Durability")
@Description("The damage value/durability of an item.")
@Examples({
	"set damage value of player's tool to 10",
	"reset the durability of {_item}",
	"set durability of player's held item to 0"
})
@Since("1.2, 2.7 (durability reversed)")
public class ExprDurability extends SimplePropertyExpression<Object, Integer> {

	// Minecraft 1.20.5+ item component "max_damage" (custom item durability)
	private static final boolean HAS_MAX_DAMAGE = Skript.methodExists(Damageable.class, "hasMaxDamage");

	private boolean durability;

	static {
		register(ExprDurability.class, Integer.class, "(damage[s] [value[s]]|1:durabilit(y|ies))", "itemtypes/slots");
	}

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		durability = parseResult.mark == 1;
		return super.init(exprs, matchedPattern, isDelayed, parseResult);
	}

	@Override
	@Nullable
	public Integer convert(Object object) {
		ItemType itemType = asItemType(object);
		if (itemType == null)
			return null;
		ItemMeta meta = itemType.getItemMeta();
		return meta instanceof Damageable ? convertToDamage(itemType, ((Damageable) meta).getDamage()) : 0;
	}

	@Override
	@Nullable
	public Class<?>[] acceptChange(ChangeMode mode) {
		switch (mode) {
			case SET:
			case ADD:
			case REMOVE:
			case DELETE:
			case RESET:
				return CollectionUtils.array(Number.class);
		}
		return null;
	}

	@Override
	public void change(Event event, @Nullable Object[] delta, ChangeMode mode) {
		int change = delta == null ? 0 : ((Number) delta[0]).intValue();
		if (mode == ChangeMode.REMOVE)
			change = -change;
		for (Object object : getExpr().getArray(event)) {
			ItemType itemType = asItemType(object);
			if (itemType == null)
				continue;

			ItemMeta meta = itemType.getItemMeta();
			if (!(meta instanceof Damageable))
				continue;
			Damageable damageable = (Damageable) meta;

			switch (mode) {
				case ADD:
				case REMOVE:
					int current = convertToDamage(itemType, damageable.getDamage());
					damageable.setDamage(convertToDamage(itemType, current + change));
					break;
				case SET:
					damageable.setDamage(convertToDamage(itemType, change));
					break;
				case DELETE:
				case RESET:
					damageable.setDamage(0);
			}

			itemType.setItemMeta(meta);
			if (object instanceof Slot)
				((Slot) object).setItem(itemType.getRandom());
		}
	}

	private int convertToDamage(ItemType itemType, int value) {
		if (!durability)
			return value;
		Damageable itemMeta = (Damageable) itemType.getItemMeta();

		int maxDurability;
		if (HAS_MAX_DAMAGE && itemMeta.hasMaxDamage())
			maxDurability = itemMeta.getMaxDamage();
		else
			maxDurability = itemType.getMaterial().getMaxDurability();

		if (maxDurability == 0)
			return 0;
		return maxDurability - value;
	}

	@Override
	public Class<? extends Integer> getReturnType() {
		return Integer.class;
	}

	@Override
	public String getPropertyName() {
		return durability ? "durability" : "damage";
	}

	@Nullable
	private static ItemType asItemType(Object object) {
		if (object instanceof ItemType)
			return (ItemType) object;
		ItemStack itemStack = ((Slot) object).getItem();
		if (itemStack == null)
			return null;
		return new ItemType(itemStack);
	}

}
