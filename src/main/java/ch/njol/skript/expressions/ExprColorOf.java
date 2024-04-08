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
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Keywords;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.Color;
import ch.njol.skript.util.ColorRGB;
import ch.njol.skript.util.SkriptColor;
import ch.njol.skript.util.slot.Slot;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.DyeColor;
import org.bukkit.FireworkEffect;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Item;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.material.Colorable;
import org.bukkit.material.MaterialData;
import org.eclipse.jdt.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Name("Color of")
@Description("The <a href='./classes.html#color'>color</a> of an item, can also be used to color chat messages with \"&lt;%color of player's tool%&gt;this text is colored!\".")
@Examples({
	"on click on wool:",
		"\tmessage \"This wool block is <%colour of block%>%colour of block%<reset>!\"",
		"\tset the colour of the block to black"
})
@Since("1.2, INSERT VERSION (potions, maps and leather armor, block colors for MC 1.14+)")
@Keywords("colour")
public class ExprColorOf extends PropertyExpression<Object, Color> {

	private static final boolean MAPS_AND_POTIONS_COLORS = Skript.methodExists(PotionMeta.class, "setColor", org.bukkit.Color.class);
	private static final DyeColor DEFAULT_MATERIAL_COLOR = DyeColor.WHITE;
	public static final Pattern MATERIAL_COLORS_PATTERN;

	static {
		DyeColor[] dyeColors = DyeColor.values();
		StringBuilder colors = new StringBuilder();
		for (int i = 0; i < dyeColors.length; i++) {
			colors.append(dyeColors[i].name()).append(i + 1 != dyeColors.length ? "|" : "");
		}
		MATERIAL_COLORS_PATTERN = Pattern.compile("^(" + colors + ")_.+");
		register(ExprColorOf.class, Color.class, "colo[u]r[s]", "blocks/itemtypes/entities/fireworkeffects/slots");
	}
	
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		setExpr(exprs[0]);
		return true;
	}
	
	@Override
	protected Color[] get(Event event, Object[] source) {
		// TODO FIX
		// this approach has couple issues, users can't use multiple types in source like
		// 'broadcast colors of ((trailing burst firework colored blue and red) and targeted block)' and second
		// this check doesn't work with variables as it's not converted yet
		if (source instanceof FireworkEffect[]) {
			List<Color> colors = new ArrayList<>();

			for (FireworkEffect effect : (FireworkEffect[]) source) {
				effect.getColors().stream()
					.map(ColorRGB::fromBukkitOrRgbColor)
					.forEach(colors::add);
			}

			if (colors.size() == 0)
				return null;
			return colors.toArray(new Color[0]);
		}

		return get(source, obj -> {
			// ItemType/Item/Slot
			if (obj instanceof ItemType || obj instanceof Item || obj instanceof Slot) {
				ItemMeta meta;
				Material material;
				if (obj instanceof ItemType) {
					meta = ((ItemType) obj).getItemMeta();
					material = ((ItemType) obj).getMaterial();
				} else if (obj instanceof Item) {
					meta = ((Item) obj).getItemStack().getItemMeta();
					material = ((Item) obj).getItemStack().getType();
				} else {
					 if (((Slot) obj).getItem() == null)
						 return null;
					meta = ((Slot) obj).getItem().getItemMeta();
					material = ((Slot) obj).getItem().getType();
				}

				if (meta == null)
					return null;

				if (meta instanceof LeatherArmorMeta) {
					LeatherArmorMeta leatherArmorMeta = (LeatherArmorMeta) meta;
					return ColorRGB.fromBukkitOrRgbColor(leatherArmorMeta.getColor());
				} else if (meta instanceof MapMeta && MAPS_AND_POTIONS_COLORS) {
						MapMeta mapMeta = (MapMeta) meta;
						if (mapMeta.getColor() != null)
							return ColorRGB.fromBukkitOrRgbColor(mapMeta.getColor());
				} else if (meta instanceof PotionMeta && MAPS_AND_POTIONS_COLORS) {
					PotionMeta potionMeta = (PotionMeta) meta;
					if (potionMeta.getColor() != null)
						return ColorRGB.fromBukkitOrRgbColor(potionMeta.getColor());
				} else {
					return getMaterialColor(material);
				}
			}
			// Blocks
			if (obj instanceof Block) {
				return getMaterialColor(((Block) obj).getType());
			}

			// Colorable
			Colorable colorable = getColorable(obj);
			if (colorable == null)
				return null;

			DyeColor dyeColor = colorable.getColor();
			if (dyeColor == null)
				return null;
			return SkriptColor.fromDyeColor(dyeColor);
		});
	}

	@Override
	public Class<? extends Color> getReturnType() {
		return Color.class;
	}

	@Override
	@Nullable
	public Class<?>[] acceptChange(ChangeMode mode) {
		Class<?> returnType = getExpr().getReturnType();

		if (FireworkEffect.class.isAssignableFrom(returnType))
			return CollectionUtils.array(Color[].class);

		switch (mode) { // items/blocks/entities don't accept these change modes
			case ADD:
			case REMOVE:
			case REMOVE_ALL:
				return null;
		}

		if (mode != ChangeMode.SET && !getExpr().isSingle())
			return null;

		return CollectionUtils.array(Color.class);
	}

	@Override
	public void change(Event event, @Nullable Object[] delta, ChangeMode mode) {
		DyeColor originalColor = (mode == ChangeMode.DELETE || mode == ChangeMode.RESET) ? null : ((Color) delta[0]).asDyeColor();;

		for (Object obj : getExpr().getArray(event)) {
			DyeColor color = originalColor; // reset
			if (obj instanceof Item || obj instanceof ItemType || obj instanceof Slot) {
				ItemStack stack;
				if (obj instanceof ItemType) {
					stack = ((ItemType) obj).getRandom();
				} else if (obj instanceof Item) {
					stack = ((Item) obj).getItemStack();
				} else {
					stack = ((Slot) obj).getItem();
				}

				if (stack == null)
					continue;

				if (!(obj instanceof Colorable)) {
					org.bukkit.Color bukkitColor = (mode == ChangeMode.DELETE || mode == ChangeMode.RESET) ? null : ((Color) delta[0]).asBukkitColor();
					ItemMeta meta = stack.getItemMeta();
					if (meta instanceof LeatherArmorMeta) {
						LeatherArmorMeta leatherArmorMeta = (LeatherArmorMeta) meta;
						leatherArmorMeta.setColor(bukkitColor);
						stack.setItemMeta(leatherArmorMeta);
					} else if (meta instanceof MapMeta && MAPS_AND_POTIONS_COLORS) {
						MapMeta mapMeta = (MapMeta) meta;
						mapMeta.setColor(bukkitColor);
						stack.setItemMeta(mapMeta);
					} else if (meta instanceof PotionMeta && MAPS_AND_POTIONS_COLORS) {
						PotionMeta potionMeta = (PotionMeta) meta;
						potionMeta.setColor(bukkitColor);
						stack.setItemMeta(potionMeta);
					} else {
						if (color == null)
							color = DEFAULT_MATERIAL_COLOR;
						Material newItem = setMaterialColor(stack.getType(), color);
						if (newItem == null)
							continue;
						ItemMeta oldItemMeta = stack.getItemMeta();
						stack.setType(newItem);
						stack.setItemMeta(oldItemMeta);
						if (obj instanceof ItemType) {
							ItemType newItemType = new ItemType(newItem);
							newItemType.setItemMeta(oldItemMeta);
							((ItemType) obj).setTo(newItemType);
						} else if (obj instanceof Item) {
							((Item) obj).setItemStack(stack);
						} else if (obj instanceof Slot)
							((Slot) obj).setItem(stack);
					}
				}
			} else if (obj instanceof Colorable) {
				Colorable colorable = getColorable(obj);

				if (colorable != null) {
					try {
						colorable.setColor(color);
					} catch (Exception ex) {
						if (ex instanceof UnsupportedOperationException) {
							// https://github.com/SkriptLang/Skript/issues/2931
							Skript.error(
								"Tried setting the colour of a bed, but this isn't possible in your Minecraft version, " +
								"since different coloured beds are different materials. " +
								"Instead, set the block to right material, such as a blue bed."
							); // Let's just assume it's a bed
						}
//						else if (ex instanceof NullPointerException) { } // Some of Colorable subclasses do not accept null as a color
					}
				}
			} else if (obj instanceof Block) {
				Block block = (Block) obj;
				if (color == null)
					color = DEFAULT_MATERIAL_COLOR;
				Material newBlock = setMaterialColor(block.getType(), color);
				if (newBlock == null)
					continue;
				block.setType(newBlock);
			} else if (obj instanceof FireworkEffect) {
				Color[] input = (Color[]) delta;
				FireworkEffect effect = ((FireworkEffect) obj);
				switch (mode) {
					case ADD:
						for (Color inputColor : input)
							effect.getColors().add(inputColor.asBukkitColor());
						break;
					case REMOVE:
					case REMOVE_ALL:
						for (Color inputColor : input)
							effect.getColors().remove(inputColor.asBukkitColor());
						break;
					case DELETE:
					case RESET:
						effect.getColors().clear();
						break;
					case SET:
						effect.getColors().clear();
						for (Color inputColor : input)
							effect.getColors().add(inputColor.asBukkitColor());
						break;
					default:
						break;
				}
			}
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "color of " + getExpr().toString(event, debug);
	}

	@Nullable
	private Colorable getColorable(Object colorable) {
		if (colorable instanceof Item || colorable instanceof ItemType || colorable instanceof Slot) {
			ItemStack item;
			if (colorable instanceof ItemType) {
				item = ((ItemType) colorable).getRandom();
			} else if (colorable instanceof Item) {
				item = ((Item) colorable).getItemStack();
			} else {
				item = ((Slot) colorable).getItem();
			}

			if (item == null)
				return null;

			MaterialData data = item.getData();
			if (data instanceof Colorable)
				return (Colorable) data;
		} else if (colorable instanceof Colorable) {
			return (Colorable) colorable;
		}
		return null;
	}

	private @Nullable SkriptColor getMaterialColor(Material material) {
		Matcher matcher = MATERIAL_COLORS_PATTERN.matcher(material.name());
		if (matcher.matches()) {
			return SkriptColor.fromDyeColor(DyeColor.valueOf(matcher.group(1)));
		}
		return null;
	}

	private @Nullable Material setMaterialColor(Material material, DyeColor color) {
		Matcher matcher = MATERIAL_COLORS_PATTERN.matcher(material.name());
		if (matcher.matches()) {
			try {
				return Material.valueOf(material.name().replace(matcher.group(1), color.name()));
			} catch (Exception ignored) {}
		}
		return null;
	}

}
