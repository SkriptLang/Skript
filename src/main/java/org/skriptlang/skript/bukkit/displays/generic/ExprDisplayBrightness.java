package org.skriptlang.skript.bukkit.displays.generic;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import ch.njol.util.Math2;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.entity.Display;
import org.bukkit.entity.Display.Brightness;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

import java.util.ArrayList;
import java.util.List;

@Name("Display Brightness")
@Description({
	"Returns or changes the brightness override of <a href='classes.html#display'>displays</a>.",
	"Unmodified displays will not have a brightness override value set. Resetting or deleting this value will remove the override.",
	"Use the 'block' or 'sky' options to get/change specific values or get both values as a list by using neither option.",
	"NOTE: setting only one of the sky/block light overrides of a display without an existing override will set both sky and block light to the given value. " +
	"Make sure to set both block and sky levels to your desired values for the best results. " +
	"Likewise, you can only clear the brightness override, you cannot clear/reset the sky/block values individually."
})
@Examples({
	"set sky light override of the last spawned text display to 7",
	"subtract 3 from the block light level override of the last spawned text display",
	"if sky light level override of {_display} is 5:",
		"\tclear brightness override of {_display}"
})
@Since("INSERT VERSION")
public class ExprDisplayBrightness extends SimpleExpression<Integer> {

	static {
		Skript.registerExpression(ExprDisplayBrightness.class, Integer.class, ExpressionType.PROPERTY,
				"[the] [:block|:sky] (light [level]|brightness) override[s] of %displays%",
				"%displays%'[s] [:block|:sky] (light [level]|brightness) override[s]");
	}

	private @UnknownNullability Expression<Display> displays;
	private boolean blockLight;
	private boolean skyLight;

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		blockLight = parseResult.hasTag("block");
		skyLight = parseResult.hasTag("sky");
		//noinspection unchecked
		displays = (Expression<Display>) expressions[0];
		return true;
	}

	@Override
	protected Integer @Nullable [] get(Event event) {
		List<Integer> values = new ArrayList<>();
		if (skyLight) {
			for (Display display : displays.getArray(event)) {
				Brightness brightness = display.getBrightness();
				if (brightness == null)
					continue;
				values.add(brightness.getSkyLight());
			}
		} else if (blockLight) {
			for (Display display : displays.getArray(event)) {
				Brightness brightness = display.getBrightness();
				if (brightness == null)
					continue;
				values.add(brightness.getBlockLight());
			}
		} else {
			for (Display display : displays.getArray(event)) {
				Brightness brightness = display.getBrightness();
				if (brightness == null)
					continue;
				values.add(brightness.getBlockLight());
				values.add(brightness.getSkyLight());
			}
		}
		return values.toArray(new Integer[0]);
	}

	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		if (skyLight || blockLight) {
			return switch (mode) {
				case ADD, REMOVE, SET -> CollectionUtils.array(Number.class);
				default -> null;
			};
		} else {
			return switch (mode) {
				case SET, RESET, DELETE -> CollectionUtils.array(Number.class);
				default -> null;
			};
		}
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		if (skyLight || blockLight) {
			int level = delta == null ? 0 : ((Number) delta[0]).intValue();
			switch (mode) {
				case REMOVE:
					level = -level;
					// $FALL-THROUGH$
				case ADD:
					for (Display display : displays.getArray(event)) {
						Brightness brightness = display.getBrightness();
						if (brightness == null) {
							int clamped = Math2.fit(0, level, 15);
							display.setBrightness(new Brightness(clamped, clamped));
						} else if (skyLight) {
							int clamped = Math2.fit(0, level + brightness.getSkyLight(), 15);
							display.setBrightness(new Brightness(brightness.getBlockLight(), clamped));
						} else {
							int clamped = Math2.fit(0, level + brightness.getBlockLight(), 15);
							display.setBrightness(new Brightness(clamped, brightness.getSkyLight()));
						}
					}
					break;
				case SET:
					for (Display display : displays.getArray(event)) {
						Brightness brightness = display.getBrightness();
						int clamped = Math2.fit(0, level, 15);
						if (brightness == null) {
							display.setBrightness(new Brightness(clamped, clamped));
						} else if (skyLight) {
							display.setBrightness(new Brightness(brightness.getBlockLight(), clamped));
						} else {
							display.setBrightness(new Brightness(clamped, brightness.getSkyLight()));
						}
					}
					break;
			}
		} else {
			Brightness change = null;
			if (delta != null) {
				int value = Math2.fit(0, ((Number) delta[0]).intValue(), 15);
				change = new Brightness(value, value);
			}
			for (Display display : displays.getArray(event))
				display.setBrightness(change);
		}
	}

	@Override
	public boolean isSingle() {
		return (skyLight || blockLight) && displays.isSingle();
	}

	@Override
	public Class<? extends Integer> getReturnType() {
		return Integer.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		if (skyLight) {
			return "sky light override of " + displays.toString(event, debug);
		} else if (blockLight) {
			return "block light override of " + displays.toString(event, debug);
		} else {
			return "brightness override of " + displays.toString(event, debug);
		}
	}

}
