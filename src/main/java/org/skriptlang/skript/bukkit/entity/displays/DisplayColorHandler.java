package org.skriptlang.skript.bukkit.entity.displays;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.util.Color;
import ch.njol.skript.util.ColorRGB;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.entity.Display;
import org.bukkit.entity.TextDisplay;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.properties.handlers.base.ExpressionPropertyHandler;

@ApiStatus.Internal
public class DisplayColorHandler implements ExpressionPropertyHandler<Display, Color> {

	@Override
	public @Nullable Color convert(Display display) {
		if (!(display instanceof TextDisplay textDisplay))
			return null;
		if (textDisplay.isDefaultBackground())
			return ColorRGB.fromBukkitColor(DisplayData.DEFAULT_BACKGROUND_COLOR);
		org.bukkit.Color bukkitColor = textDisplay.getBackgroundColor();
		if (bukkitColor == null)
			return null;
		return ColorRGB.fromBukkitColor(bukkitColor);
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		return switch (mode) {
			case SET, RESET -> CollectionUtils.array(Color.class);
			default -> null;
		};
	}

	@Override
	public void change(Display display, Object @Nullable [] delta, ChangeMode mode) {
		if (!(display instanceof TextDisplay textDisplay))
			return;
		if (mode == ChangeMode.RESET) {
			textDisplay.setDefaultBackground(true);
			return;
		}
		if (delta == null || delta.length == 0)
			return;
		if (textDisplay.isDefaultBackground())
			textDisplay.setDefaultBackground(false);
		textDisplay.setBackgroundColor(((Color) delta[0]).asBukkitColor());
	}

	@Override
	public @NotNull Class<Color> returnType() {
		return Color.class;
	}

}
