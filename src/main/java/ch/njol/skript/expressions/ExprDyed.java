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
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.util.Color;
import ch.njol.util.Kleenean;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.eclipse.jdt.annotation.Nullable;

import java.util.regex.Matcher;

@Name("Dyed")
@Description("An expression to return items/entities with a color.")
@Examples({
		"give player leather chestplate dyed red",
		"give player potion of invisibility dyed rgb 200, 70, 88",
		"give player filled map with color rgb(20, 60, 70)"
})
@Since("INSERT VERSION")
public class ExprDyed extends SimpleExpression<ItemType> {

	private static final boolean MAPS_AND_POTIONS_COLORS = Skript.methodExists(PotionMeta.class, "setColor", org.bukkit.Color.class);
	
	static {
		Skript.registerExpression(ExprDyed.class, ItemType.class, ExpressionType.COMBINED, "%itemtypes% (dyed|with color [of]) %color%");
	}
	
	private Expression<ItemType> targets;
	private Expression<Color> color;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] vars, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		targets = (Expression<ItemType>) vars[0];
		color = (Expression<Color>) vars[1];
		return true;
	}
	
	@Override
	@Nullable
	protected ItemType[] get(Event event) {
		Color color = this.color.getSingle(event);
		ItemType[] targets = this.targets.getArray(event);
		org.bukkit.Color bukkitColor;

		if (color == null)
			return new ItemType[0];

		bukkitColor = color.asBukkitColor();
		for (ItemType item : targets) {
			ItemMeta meta = item.getItemMeta();

			if (meta instanceof LeatherArmorMeta) {
				LeatherArmorMeta m = (LeatherArmorMeta) meta;
				m.setColor(bukkitColor);
				item.setItemMeta(m);
			} else if (meta instanceof MapMeta && MAPS_AND_POTIONS_COLORS) {
				MapMeta mapMeta = (MapMeta) meta;
				mapMeta.setColor(bukkitColor);
				item.setItemMeta(mapMeta);
			} else if (meta instanceof PotionMeta && MAPS_AND_POTIONS_COLORS) {
				PotionMeta potionMeta = (PotionMeta) meta;
				potionMeta.setColor(bukkitColor);
				item.setItemMeta(potionMeta);
			} else {
				Material material = item.getMaterial();
				Matcher matcher = ExprColorOf.MATERIAL_COLORS_PATTERN.matcher(material.name());
				if (!matcher.matches())
					continue;
				try {
					Material newItem = Material.valueOf(material.name().replace(matcher.group(1), color.getName()));
					item.setTo(new ItemType(newItem));
				} catch (Exception ignored) {}
			}
		}
		return targets.clone();
	}

	@Override
	public boolean isSingle() {
		return targets.isSingle();
	}

	@Override
	public Class<? extends ItemType> getReturnType() {
		return ItemType.class;
	}
	
	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return targets.toString(event, debug) + " dyed " + color;
	}
	
}
