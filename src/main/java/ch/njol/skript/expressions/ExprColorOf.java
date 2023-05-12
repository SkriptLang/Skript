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
import ch.njol.skript.util.SkriptColor;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.DyeColor;
import org.bukkit.FireworkEffect;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockDataMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.material.Colorable;
import org.eclipse.jdt.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Name("Color of")
@Description("The <a href='./classes.html#color'>color</a> of an item, can also be used to color chat messages with \"&lt;%color of ...%&gt;this text is colored!\".")
@Examples({
	"on click on wool:",
		"\tmessage \"This wool block is <%colour of block%>%colour of block%<reset>!\"",
		"\tset the colour of the block to black"
})
@Since("1.2, INSERT VERSION (potions, maps and leather armor, fix block colors)")
@Keywords("colour")
public class ExprColorOf extends PropertyExpression<Object, Color> {

	private static final boolean MAPS_AND_POTIONS_COLORS = Skript.methodExists(PotionMeta.class, "setColor", org.bukkit.Color.class);
	private static final Pattern MATERIAL_COLORS_PATTERN;

	static {
		DyeColor[] dyeColors = DyeColor.values();
		StringBuilder colors = new StringBuilder();
		for (int i = 0; i < dyeColors.length; i++) {
			colors.append(dyeColors[i].name()).append(i + 1 != dyeColors.length ? "|" : "");
		}
		MATERIAL_COLORS_PATTERN = Pattern.compile("^(" + colors + ")_.+");

		register(ExprColorOf.class, Color.class, "colo[u]r[s]", "blocks/itemtypes/entities/fireworkeffects");
	}
	
	@Override
	@SuppressWarnings("null")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		setExpr(exprs[0]);
		return true;
	}
	
	@Override
	@SuppressWarnings("null")
	protected Color[] get(Event e, Object[] source) {
		if (source instanceof FireworkEffect[]) {
			List<Color> colors = new ArrayList<>();
			
			for (FireworkEffect effect : (FireworkEffect[]) source) {
				effect.getColors().stream()
					.map(SkriptColor::fromBukkitOrRgbColor)
					.forEach(colors::add);
			}
			
			if (colors.size() == 0)
				return null;
			return colors.toArray(new Color[0]);
		}

		if (source instanceof ItemType[]) {
			List<Color> colors = new ArrayList<>();
			for (ItemType item : (ItemType[]) source) {
				ItemMeta meta = item.getItemMeta();

				if (meta instanceof LeatherArmorMeta) {
					LeatherArmorMeta m = (LeatherArmorMeta) meta;
					colors.add(SkriptColor.fromBukkitOrRgbColor(m.getColor()));
				} else if (MAPS_AND_POTIONS_COLORS) {
					if (meta instanceof MapMeta) {
						MapMeta m = (MapMeta) meta;
						if (m.getColor() != null)
							colors.add(SkriptColor.fromBukkitOrRgbColor(m.getColor()));
					} else if (meta instanceof PotionMeta) {
						PotionMeta m = (PotionMeta) meta;
						if (m.getColor() != null)
							colors.add(SkriptColor.fromBukkitOrRgbColor(m.getColor()));
					}
				}
			}
			return colors.toArray(new Color[0]);
		}

		if (source instanceof Block[]) {
			for (Block block : (Block[]) source) {
				Material material = block.getType();
				Matcher matcher = MATERIAL_COLORS_PATTERN.matcher(material.name());
				if (matcher.matches() && matcher.group(1) != null) {
					return new SkriptColor[]{SkriptColor.fromName(matcher.group(1).toLowerCase(Locale.ENGLISH))};
				}
			}
		}

		return get(source, o -> {
			Colorable colorable = getColorable(o);

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
	public String toString(@Nullable Event e, boolean debug) {
		return "color of " + getExpr().toString(e, debug);
	}

	@Override
	@Nullable
	public Class<?>[] acceptChange(ChangeMode mode) {
		switch (mode) {
			case ADD:
			case REMOVE:
			case REMOVE_ALL:
				return null;
		}

		Class<?> returnType = getExpr().getReturnType();

		if (FireworkEffect.class.isAssignableFrom(returnType))
			return CollectionUtils.array(Color[].class);

		if (mode != ChangeMode.SET && !getExpr().isSingle())
			return null;

		if (Entity.class.isAssignableFrom(returnType))
			return CollectionUtils.array(Color.class);
		else if (Block.class.isAssignableFrom(returnType))
			return CollectionUtils.array(Color.class);
		if (ItemType.class.isAssignableFrom(returnType))
			return CollectionUtils.array(Color.class);
		return null;
	}

	@Override
	public void change(Event event, @Nullable Object[] delta, ChangeMode mode) {
		DyeColor color = (mode == ChangeMode.DELETE || mode == ChangeMode.RESET) ? null : ((Color) delta[0]).asDyeColor();;

		for (Object obj : getExpr().getArray(event)) {
			if (obj instanceof Item || obj instanceof ItemType) {
				ItemStack stack = obj instanceof Item ? ((Item) obj).getItemStack() : ((ItemType) obj).getRandom();

				if (stack == null)
					continue;

				BlockData data = stack.getType().createBlockData();

				if (!(obj instanceof Colorable)) { // Items such as Leather armor, potions and maps
					ItemType item = (ItemType) obj;
					org.bukkit.Color c = (mode == ChangeMode.DELETE || mode == ChangeMode.RESET) ? null : ((Color) delta[0]).asBukkitColor();
					ItemMeta meta = item.getItemMeta();

					if (meta instanceof LeatherArmorMeta) {
						LeatherArmorMeta leatherArmorMeta = (LeatherArmorMeta) meta;
						leatherArmorMeta.setColor(c);
						item.setItemMeta(leatherArmorMeta);
					} else if (MAPS_AND_POTIONS_COLORS) {
						if (meta instanceof MapMeta) {
							MapMeta mapMeta = (MapMeta) meta;
							mapMeta.setColor(c);
							item.setItemMeta(mapMeta);
						} else if (meta instanceof PotionMeta) {
							PotionMeta potionMeta = (PotionMeta) meta;
							potionMeta.setColor(c);
							item.setItemMeta(potionMeta);
						}
					}
				}

				if (!(data instanceof Colorable))
					continue;

				((Colorable) data).setColor(color);
//				stack.setData(data);
				((BlockDataMeta) stack).setBlockData(data);

				if (obj instanceof Item)
					((Item) obj).setItemStack(stack);
			} else if (obj instanceof Block || obj instanceof Colorable) {
				Colorable colorable = getColorable(obj);

				if (colorable != null) {
					try {
						colorable.setColor(color);
					} catch (Exception ex) {
						if (ex instanceof UnsupportedOperationException) {
							// https://github.com/SkriptLang/Skript/issues/2931
							Skript.error("Tried setting the colour of a bed, but this isn't possible in your Minecraft version, " +
								"since different coloured beds are different materials. " +
								"Instead, set the block to right material, such as a blue bed."); // Let's just assume it's a bed
						}
//						else if (ex instanceof NullPointerException) { } // Some of Colorable subclasses do not accept null as a color
					}
				}
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

	@Nullable
	private Colorable getColorable(Object colorable) {
		if (colorable instanceof Item || colorable instanceof ItemType) {
			ItemStack item = colorable instanceof Item ?
					((Item) colorable).getItemStack() : ((ItemType) colorable).getRandom();

			if (item == null)
				return null;

			BlockData data = item.getType().createBlockData();

			if (data instanceof Colorable)
				return (Colorable) data;
		} else if (colorable instanceof Colorable) {
			return (Colorable) colorable;
		}
		return null;
	}

}
