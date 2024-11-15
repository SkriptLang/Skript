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
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import org.skriptlang.skript.bukkit.displays.DisplayData;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.Color;
import ch.njol.skript.util.ColorRGB;
import ch.njol.skript.util.SkriptColor;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.DyeColor;
import org.bukkit.FireworkEffect;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.TextDisplay;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Colorable;
import org.bukkit.material.MaterialData;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

@Name("Color of")
@Description({
	"The <a href='./classes.html#color'>color</a> of an item, entity, block, firework effect, or text display.",
	"This can also be used to color chat messages with \"&lt;%color of ...%&gt;this text is colored!\".",
	"Do note that firework effects support setting, adding, removing, resetting, and deleting; text displays support " +
	"setting and resetting; and items, entities, and blocks only support setting, and only for very few items/blocks."
})
@Examples({
	"on click on wool:",
		"\tmessage \"This wool block is <%color of block%>%color of block%<reset>!\"",
		"\tset the color of the block to black"
})
@Since("1.2, INSERT VERSION (displays)")
public class ExprColorOf extends PropertyExpression<Object, Color> {

	static {
		String types = "blocks/itemtypes/entities/fireworkeffects";
		if (Skript.isRunningMinecraft(1, 19, 4))
			types += "/displays";
		register(ExprColorOf.class, Color.class, "colo[u]r[s]", types);
	}

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		setExpr(exprs[0]);
		return true;
	}

	@Override
	protected Color[] get(Event event, Object[] source) {
		if (source instanceof FireworkEffect[]) {
			List<Color> colors = new ArrayList<>();
			for (FireworkEffect effect : (FireworkEffect[]) source) {
				effect.getColors().stream()
					.map(SkriptColor::fromBukkitColor)
					.forEach(colors::add);
			}
			return colors.toArray(new Color[0]);
		}
		return get(source, object -> {
			if (object instanceof Display) {
				if (!(object instanceof TextDisplay display))
					return null;
				if (display.isDefaultBackground())
					return ColorRGB.fromBukkitColor(DisplayData.DEFAULT_BACKGROUND_COLOR);
				org.bukkit.Color bukkitColor = display.getBackgroundColor();
				if (bukkitColor == null)
					return null;
				return ColorRGB.fromBukkitColor(bukkitColor);
			}
			Colorable colorable = getColorable(object);
			if (colorable == null)
				return null;
			DyeColor dyeColor = colorable.getColor();
			if (dyeColor == null)
				return null;
			return SkriptColor.fromDyeColor(dyeColor);
		});
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		Class<?> returnType = getExpr().getReturnType();

		if (returnType.isAssignableFrom(FireworkEffect.class))
			return CollectionUtils.array(Color[].class);

		// double assignable checks are to allow both parent and child types, since variables return Object
		// This does mean we have to be more stringent in checking the validity of the change mode in change() itself.
		if ((returnType.isAssignableFrom(Display.class) || Display.class.isAssignableFrom(returnType)) && (mode == ChangeMode.RESET || mode == ChangeMode.SET))
			return CollectionUtils.array(Color.class);

		// the following only support SET
		if (mode != ChangeMode.SET)
			return null;
		if (returnType.isAssignableFrom(Entity.class)
			|| Entity.class.isAssignableFrom(returnType)
			|| returnType.isAssignableFrom(Block.class)
			|| Block.class.isAssignableFrom(returnType)
			|| returnType.isAssignableFrom(ItemType.class)
		) {
			return CollectionUtils.array(Color.class);
		}
		return null;
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		Color color = null;
		if (delta != null)
			color = (Color) delta[0];
		for (Object object : getExpr().getArray(event)) {
			if (object instanceof Item || object instanceof ItemType) {
				if (mode != ChangeMode.SET)
					return;
				assert color != null;
				ItemStack stack = object instanceof Item ? ((Item) object).getItemStack() : ((ItemType) object).getRandom();
				if (stack == null)
					continue;

				MaterialData data = stack.getData();
				if (!(data instanceof Colorable))
					continue;

				((Colorable) data).setColor(color.asDyeColor());
				stack.setData(data);

				if (object instanceof Item item)
					item.setItemStack(stack);
			} else if (object instanceof Block || object instanceof Colorable) {
				if (mode != ChangeMode.SET)
					return;
				Colorable colorable = getColorable(object);
				assert color != null;

				if (colorable != null) {
					try {
						colorable.setColor(color.asDyeColor());
					} catch (UnsupportedOperationException ex) {
						// https://github.com/SkriptLang/Skript/issues/2931
						Skript.error("Tried setting the color of a bed, but this isn't possible in your Minecraft version, " +
							"since different colored beds are different materials. " +
							"Instead, set the block to right material, such as a blue bed."); // Let's just assume it's a bed
					}
				}
			} else if (object instanceof TextDisplay display) {
				switch (mode) {
					case RESET -> display.setDefaultBackground(true);
					case SET -> {
						assert color != null;
						if (display.isDefaultBackground())
							display.setDefaultBackground(false);
						display.setBackgroundColor(color.asBukkitColor());
					}
				}
			} else if (object instanceof FireworkEffect effect) {
				Color[] input = (Color[]) delta;
				switch (mode) {
					case ADD:
						for (Color c : input)
							effect.getColors().add(c.asBukkitColor());
						break;
					case REMOVE:
					case REMOVE_ALL:
						for (Color c : input)
							effect.getColors().remove(c.asBukkitColor());
						break;
					case DELETE:
					case RESET:
						effect.getColors().clear();
						break;
					case SET:
						effect.getColors().clear();
						for (Color c : input)
							effect.getColors().add(c.asBukkitColor());
						break;
					default:
						break;
				}
			}
		}
	}

	@Override
	public Class<? extends Color> getReturnType() {
		return Color.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "color of " + getExpr().toString(event, debug);
	}

	@Nullable
	private Colorable getColorable(Object colorable) {
		if (colorable instanceof Item || colorable instanceof ItemType) {
			ItemStack item = colorable instanceof Item ?
					((Item) colorable).getItemStack() : ((ItemType) colorable).getRandom();

			if (item == null)
				return null;
			MaterialData data = item.getData();
			if (data instanceof Colorable)
				return (Colorable) data;
		} else if (colorable instanceof Block) {
			BlockState state = ((Block) colorable).getState();
			if (state instanceof Colorable)
				return (Colorable) state;
		} else if (colorable instanceof Colorable) {
			return (Colorable) colorable;
		}
		return null;
	}

}
