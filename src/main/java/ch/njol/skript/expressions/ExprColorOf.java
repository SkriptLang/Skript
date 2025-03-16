package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
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
import ch.njol.skript.util.ColorRGB;
import ch.njol.skript.util.SkriptColor;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.DyeColor;
import org.bukkit.FireworkEffect;
import org.bukkit.block.Banner;
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
import org.skriptlang.skript.bukkit.displays.DisplayData;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

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
					.map(ColorRGB::fromBukkitColor)
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
			return getColor(object);
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
		Color[] colors = delta != null ? (Color[]) delta : null;
		Consumer<TextDisplay> displayChanger = getDisplayChanger(mode, colors);
		Consumer<FireworkEffect> fireworkChanger = getFireworkChanger(mode, colors);
		for (Object object : getExpr().getArray(event)) {
			if (object instanceof TextDisplay display) {
				displayChanger.accept(display);
			} else if (object instanceof FireworkEffect effect) {
				fireworkChanger.accept(effect);
			} else if (mode == ChangeMode.SET && (object instanceof Block || object instanceof Colorable)) {
				assert colors[0] != null;
				Colorable colorable = getColorable(object);
				if (colorable != null) {
					try {
						colorable.setColor(colors[0].asDyeColor());
					} catch (UnsupportedOperationException ex) {
						// https://github.com/SkriptLang/Skript/issues/2931
						Skript.error("Tried setting the color of a bed, but this isn't possible in your Minecraft version, " +
							"since different colored beds are different materials. " +
							"Instead, set the block to right material, such as a blue bed."); // Let's just assume it's a bed
					}
				} else {
					if (object instanceof Block block) {
						if (block.getState() instanceof Banner banner)
							banner.setBaseColor(colors[0].asDyeColor());
					}
				}
			} else if (mode == ChangeMode.SET && (object instanceof Item || object instanceof ItemType)) {
				assert colors[0] != null;
				ItemStack stack = object instanceof Item ? ((Item) object).getItemStack() : ((ItemType) object).getRandom();
				if (stack == null)
					continue;
				//noinspection removal
				MaterialData data = stack.getData();
				if (!(data instanceof Colorable colorable))
					continue;
				colorable.setColor(colors[0].asDyeColor());
				//noinspection removal
				stack.setData(data);
				if (object instanceof Item item) {
					item.setItemStack(stack);
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

	private Consumer<TextDisplay> getDisplayChanger(ChangeMode mode, Color @Nullable [] colors) {
		Color color = (colors != null && colors.length == 1) ? colors[0] : null;
		return switch (mode) {
			case RESET -> display -> {
				display.setDefaultBackground(true);
			};
			case SET -> display -> {
				if (color != null) {
					if (display.isDefaultBackground())
						display.setDefaultBackground(false);
					display.setBackgroundColor(color.asBukkitColor());
				}
			};
			default -> display -> {};
		};
	}

	private Consumer<FireworkEffect> getFireworkChanger(ChangeMode mode, Color @Nullable [] colors) {
		return switch (mode) {
			case ADD -> effect -> {
				for (Color color : colors)
					effect.getColors().add(color.asBukkitColor());
			};
			case REMOVE, REMOVE_ALL -> effect -> {
				for (Color color : colors)
					effect.getColors().remove(color.asBukkitColor());
			};
			case DELETE, RESET -> effect -> {
				effect.getColors().clear();
			};
			case SET -> effect -> {
				effect.getColors().clear();
				for (Color color : colors)
					effect.getColors().add(color.asBukkitColor());
			};
			default -> effect -> {};
		};
	}

	private @Nullable Colorable getColorable(Object colorable) {
		if (colorable instanceof Item || colorable instanceof ItemType) {
			ItemStack item = colorable instanceof Item ?
					((Item) colorable).getItemStack() : ((ItemType) colorable).getRandom();

			if (item == null)
				return null;
			//noinspection removal
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

	private @Nullable Color getColor(Object object) {
		Colorable colorable = getColorable(object);
		if (colorable != null) {
			DyeColor dyeColor = colorable.getColor();
			if (dyeColor == null)
				return null;
			return SkriptColor.fromDyeColor(dyeColor);
		}
		if (object instanceof Block block) {
			if (block.getState() instanceof Banner banner)
				return SkriptColor.fromDyeColor(banner.getBaseColor());
		}
		return null;
	}

}
