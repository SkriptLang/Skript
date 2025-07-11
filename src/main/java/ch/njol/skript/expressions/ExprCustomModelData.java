package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.*;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.Color;
import ch.njol.skript.util.ColorRGB;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.components.CustomModelDataComponent;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.IntFunction;

@Name("Custom Model Data")
@Description({
	"Get/set the custom model data of an item. Using just `custom model data` will return an integer. Items without model data will return 0.",
	"Since 1.21.4, custom model data instead consists of a list of numbers (floats), a list of booleans (flags), a list of strings, and a list of colours. " +
	"Accessing and modifying these lists can be done type-by-type, or all at once with `complete custom model data`. " +
	"This is the more accurate and recommended method of using custom model data."
})
@Example("""
	set custom model data of player's tool to 3
	set {_model} to custom model data of player's tool
	""")
@Example("""
	set custom model data colours of {_flag} to red, white, and blue
	add 10.5 to the model data floats of {_flag}
	""")
@Example("""
	set the full custom model data of {_item} to 10, "sword", and rgb(100, 200, 30)
	""")
@RequiredPlugins("Minecraft 1.21.4+ (floats/flags/strings/colours/full model data)")
@Since({"2.5", "2.12 (floats/flags/strings/colours/full model data)"})
public class ExprCustomModelData extends PropertyExpression<ItemType, Object> {

	private static final boolean USE_NEW_CMD = Skript.classExists("org.bukkit.inventory.meta.components.CustomModelDataComponent");

	static {
		if (USE_NEW_CMD) {
			List<String> patterns = new ArrayList<>();
			patterns.addAll(Arrays.asList(PropertyExpression.getPatterns("[custom] model data", "itemtypes")));
			patterns.addAll(Arrays.asList(PropertyExpression.getPatterns("[custom] model data (1:floats|2:flags|3:strings|4:colo[u]rs)", "itemtype")));
			patterns.addAll(Arrays.asList(PropertyExpression.getPatterns("(5:(complete|full)) [custom] model data", "itemtype")));
			Skript.registerExpression(ExprCustomModelData.class, Object.class, ExpressionType.PROPERTY, patterns.toArray(String[]::new));
		} else {
			register(ExprCustomModelData.class, Object.class, "[custom] model data", "itemtypes");
		}
	}

	private enum CMDType {
		SINGLE_INT(Integer.class),
		FLOATS(Float.class),
		FLAGS(Boolean.class),
		STRINGS(String.class),
		COLORS(Color.class),
		ALL(Float.class, Boolean.class, String.class, Color.class);

		private final Class<?>[] types;

		CMDType(Class<?>... returns) {
			this.types = returns;
		}
	}

	private CMDType dataType;
	private Class<?> returnType;

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		dataType = CMDType.values()[parseResult.mark];
		returnType = Classes.getSuperClassInfo(dataType.types).getC();
		//noinspection unchecked
		setExpr((Expression<? extends ItemType>) expressions[0]);
		return true;
	}


	@Override
	@SuppressWarnings("UnstableApiUsage")
	protected Object[] get(Event event, ItemType[] source) {
		for (ItemType from : source) {
			ItemMeta meta = from.getItemMeta();
			if (dataType == CMDType.SINGLE_INT) {
				if (meta.hasCustomModelData() && (!USE_NEW_CMD || !meta.getCustomModelDataComponent().getFloats().isEmpty()))
					return new Integer[]{meta.getCustomModelData()};
				return new Integer[]{0};
			}

			CustomModelDataComponent component = meta.getCustomModelDataComponent();
			IntFunction<Object[]> arrayConstructor = n -> (Object[]) Array.newInstance(getReturnType(), n);
			return switch (dataType) {
				case SINGLE_INT -> throw new IllegalStateException("Unreachable state for SINGLE_INT!");
				case FLOATS -> component.getFloats().toArray(arrayConstructor);
				case FLAGS -> component.getFlags().toArray(arrayConstructor);
				case STRINGS -> component.getStrings().toArray(arrayConstructor);
				case COLORS ->
					component.getColors().stream().map(ColorRGB::fromBukkitColor).toList().toArray(ColorRGB[]::new);
				case ALL -> {
					List<Object> data = new ArrayList<>();
					data.addAll(component.getFloats());
					data.addAll(component.getFlags());
					data.addAll(component.getStrings());
					data.addAll(component.getColors().stream().map(ColorRGB::fromBukkitColor).toList());
					yield data.toArray(arrayConstructor);
				}
			};
		}
		return (Object[]) Array.newInstance(getReturnType(), 0);
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		return switch (mode) {
			case ADD, REMOVE, SET, DELETE, RESET -> {
				// convert to array types to allow plural changes.
				Class<?>[] arrayClasses = new Class[dataType.types.length];
				for (int i = 0; i < dataType.types.length; i++) {
					arrayClasses[i] = dataType.types[i].arrayType();
				}
				yield arrayClasses;
			}
			default -> null;
		};
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		for (ItemType item : getExpr().getArray(event)) {
			ItemMeta meta = item.getItemMeta();
			switch (dataType) {
				case SINGLE_INT -> {
					long deltaValue = delta == null ? 0 : ((Number) delta[0]).intValue();
					changeOld(meta, mode, deltaValue);
				}
				case FLAGS, COLORS, STRINGS, FLOATS -> changeSingleType(meta, mode, delta);
				case ALL -> changeAll(meta, mode, delta);
			}
			item.setItemMeta(meta);
		}
	}

	/**
	 * Changes the custom model data of an ItemMeta using <=1.21.3 methods.
	 * @param meta The meta to change
	 * @param mode The mode to change with
	 * @param delta The delta value
	 */
	private void changeOld(ItemMeta meta, ChangeMode mode, long delta) {
		// shortcut for delete/reset
		if (mode == ChangeMode.DELETE || mode == ChangeMode.RESET) {
			meta.setCustomModelData(null);
			return;
		}
		long oldData = 0;
		if (meta.hasCustomModelData()) {
			//noinspection UnstableApiUsage
			if (!USE_NEW_CMD || !meta.getCustomModelDataComponent().getFloats().isEmpty())
				oldData = meta.getCustomModelData();
		}

		switch (mode) {
			case REMOVE, REMOVE_ALL:
				delta = -delta;
			case ADD:
				delta = oldData + delta;
				meta.setCustomModelData((int) delta);
				break;
			case SET:
				meta.setCustomModelData((int) delta);
				break;
		}
	}

	/**
	 * Changes a single type of custom model data of an ItemMeta.
	 * @param meta The meta to change
	 * @param mode The mode to change with
	 * @param delta The values to add/remove/set
	 */
	@SuppressWarnings("UnstableApiUsage")
	private <T> void changeSingleType(ItemMeta meta, ChangeMode mode, T @Nullable [] delta) {
		if (delta == null && mode != ChangeMode.DELETE && mode != ChangeMode.RESET)
			return;

		CustomModelDataComponent component = meta.getCustomModelDataComponent();
		// create the list from existing data
		// we can be sure the values are of the proper types
		//noinspection unchecked
		List<T> data = new ArrayList<>((List<T>) switch (dataType) {
			case FLOATS -> component.getFloats();
			case FLAGS -> component.getFlags();
			case STRINGS -> component.getStrings();
			case COLORS -> component.getColors().stream().map(ColorRGB::fromBukkitColor).toList();
			default -> throw new IllegalStateException("Wrong changemode for changeSingleType");
		});

		// edit the list
		switch (mode) {
			case REMOVE -> data.removeAll(Arrays.asList(delta));
			case ADD -> data.addAll(Arrays.asList(delta));
			case SET -> data = Arrays.asList(delta);
			case RESET, DELETE -> data.clear();
		}

		// edit the component
		switch (dataType) {
			case FLOATS -> //noinspection unchecked
				component.setFloats((List<Float>) data);
			case FLAGS -> //noinspection unchecked
				component.setFlags((List<Boolean>) data);
			case STRINGS -> //noinspection unchecked
				component.setStrings((List<String>) data);
			case COLORS -> component.setColors(data.stream().map(colorRGB -> ((ColorRGB) colorRGB).asBukkitColor()).toList());
		}
		meta.setCustomModelDataComponent(component);
	}

	/**
	 * Changes all the types of custom model data of an ItemMeta.
	 * @param meta The meta to change
	 * @param mode The mode to change with
	 * @param delta The values to add/remove/set
	 */
	@SuppressWarnings("UnstableApiUsage")
	private void changeAll(ItemMeta meta, ChangeMode mode, Object @Nullable [] delta) {
		// shortcut for delete/reset
		if (mode == ChangeMode.DELETE || mode == ChangeMode.RESET) {
			meta.setCustomModelDataComponent(null);
			return;
		}

		if (delta == null)
			return;

		CustomModelDataComponent component = meta.getCustomModelDataComponent();
		List<Float> floats = new ArrayList<>(component.getFloats());
		List<Boolean> flags = new ArrayList<>(component.getFlags());
		List<String> strings = new ArrayList<>(component.getStrings());
		List<Color> colors = new ArrayList<>(component.getColors().stream().map(ColorRGB::fromBukkitColor).toList());

		// sort delta into the necessary lists
		switch (mode) {
			case REMOVE:
				for (Object deltaValue : delta) {
					if (deltaValue instanceof Float) {
						floats.remove(deltaValue);
					} else if (deltaValue instanceof Boolean) {
						flags.remove(deltaValue);
					} else if (deltaValue instanceof String) {
						strings.remove(deltaValue);
					} else if (deltaValue instanceof Color) {
						colors.remove(deltaValue);
					}
				}
				break;
			case SET:
				floats.clear();
				flags.clear();
				strings.clear();
				colors.clear();
			case ADD:
				for (Object deltaValue : delta) {
					if (deltaValue instanceof Float aFloat) {
						floats.addLast(aFloat);
					} else if (deltaValue instanceof Boolean aBoolean) {
						flags.addLast(aBoolean);
					} else if (deltaValue instanceof String string) {
						strings.addLast(string);
					} else if (deltaValue instanceof Color color) {
						colors.addLast(color);
					}
				}
				break;
		}
		// reconstruct the component
		component.setFloats(floats);
		component.setFlags(flags);
		component.setStrings(strings);
		component.setColors(colors.stream().map(Color::asBukkitColor).toList());
		meta.setCustomModelDataComponent(component);
	}

	@Override
	public boolean isSingle() {
		return dataType == CMDType.SINGLE_INT && getExpr().isSingle();
	}

	@Override
	public Class<?> getReturnType() {
		return returnType;
	}

	@Override
	public Class<?>[] possibleReturnTypes() {
		return dataType.types;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return switch (dataType) {
			case ALL -> "complete custom model data";
			case FLOATS -> "custom model data floats";
			case FLAGS -> "custom model data flags";
			case STRINGS -> "custom model data strings";
			case COLORS -> "custom model data colors";
			case SINGLE_INT -> "custom model data";
		} + " of " + getExpr().toString(event, debug);
	}

}
