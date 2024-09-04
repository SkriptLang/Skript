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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ch.njol.skript.Skript;
import org.bukkit.boss.BossBar;
import org.bukkit.DyeColor;
import org.bukkit.FireworkEffect;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Item;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Colorable;
import org.bukkit.material.MaterialData;

import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.Color;
import ch.njol.skript.util.SkriptColor;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.jetbrains.annotations.Nullable;

@Name("Color of")
@Description({
	"The <a href='./classes.html#color'>color</a> of an item, can also be used to color chat messages with \"&lt;%color of ...%&gt;this text is colored!\".",
	"Also the bar color of a boss bar."
})
@Examples({
	"on click on wool:",
		"\tmessage \"This wool block is <%color of block%>%color of block%<reset>!\"",
		"\tset the color of the block to black",
	"set {bar} to a new boss bar",
	"set the color of {bar} to red"
})
@Since("1.2")
public class ExprColorOf extends PropertyExpression<Object, Color> {

	static {
		register(ExprColorOf.class, Color.class, "colo[u]r[s]", "blocks/itemtypes/entities/fireworkeffects/bossbars");
	}

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		setExpr(exprs[0]);
		return true;
	}

	@Override
	protected Color[] get(Event e, Object[] source) {
		if (source instanceof FireworkEffect[]) {
			List<Color> colors = new ArrayList<>();

			for (FireworkEffect effect : (FireworkEffect[]) source) {
				effect.getColors().stream()
					.map(SkriptColor::fromBukkitColor)
					.forEach(colors::add);
			}

			if (colors.isEmpty())
				return null;
			return colors.toArray(new Color[0]);
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
	public String toString(@Nullable Event event, boolean debug) {
		return "color of " + getExpr().toString(event, debug);
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		Class<?> returnType = getExpr().getReturnType();

		if (FireworkEffect.class.isAssignableFrom(returnType))
			return CollectionUtils.array(Color[].class);

		if (mode == ChangeMode.SET)
			return CollectionUtils.array(Color.class);
		return null;
	}

	@Override
	public void change(Event e, @Nullable Object[] delta, ChangeMode mode) {

		for (Object object : this.getExpr().getArray(e)) {
			if (object instanceof Item || object instanceof ItemType) {
				if (delta == null || delta.length < 1)
					continue;
				DyeColor color = ((Color) delta[0]).asDyeColor();
				ItemStack stack = object instanceof Item ? ((Item) object).getItemStack() : ((ItemType) object).getRandom();

				if (stack == null)
					continue;

				MaterialData data = stack.getData();

				if (!(data instanceof Colorable))
					continue;

				((Colorable) data).setColor(color);
				stack.setData(data);

				if (object instanceof Item)
					((Item) object).setItemStack(stack);
			} else if (object instanceof Block || object instanceof BossBar || object instanceof Colorable) {
				if (delta == null || delta.length < 1)
					continue;
				DyeColor color = ((Color) delta[0]).asDyeColor();
				Colorable colorable = getColorable(object);

				if (colorable != null) {
					try {
						colorable.setColor(color);
					} catch (UnsupportedOperationException ex) {
						// https://github.com/SkriptLang/Skript/issues/2931
						Skript.error("Tried setting the color of a bed, but this isn't possible in your Minecraft version, " +
							"since different colored beds are different materials. " +
							"Instead, set the block to right material, such as a blue bed."); // Let's just assume it's a bed
					}
				}
			} else if (object instanceof FireworkEffect) {
				Color[] input = delta instanceof Color[]
					? (Color[]) delta
					: Arrays.copyOf(delta, delta.length, Color[].class);
				FireworkEffect effect = ((FireworkEffect) object);
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

	@Nullable
	private Colorable getColorable(Object colorable) {
		if (colorable instanceof BossBar bar) {
			return new Colorable() {
				@Override
				public DyeColor getColor() {
					return ExprBossBar.getDye(bar.getColor());
				}

				@Override
				public void setColor(DyeColor color) {
					bar.setColor(ExprBossBar.getColor(color));
				}
			};
		}
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
