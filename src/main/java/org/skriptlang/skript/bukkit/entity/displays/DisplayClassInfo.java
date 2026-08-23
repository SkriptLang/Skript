package org.skriptlang.skript.bukkit.entity.displays;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.data.DefaultChangers;
import ch.njol.skript.expressions.base.EventValueExpression;
import ch.njol.skript.util.Color;
import ch.njol.skript.util.ColorRGB;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.entity.Display;
import org.bukkit.entity.TextDisplay;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import org.skriptlang.skript.lang.properties.Property;
import org.skriptlang.skript.lang.properties.handlers.base.ExpressionPropertyHandler;

@ApiStatus.Internal
public class DisplayClassInfo extends ClassInfo<Display> {

	public DisplayClassInfo() {
		super(Display.class, "display");
		this.user("displays?")
			.name("Display Entity")
			.description("A text, block or item display entity.")
			.since("2.10")
			.defaultExpression(new EventValueExpression<>(Display.class))
			.changer(DefaultChangers.nonLivingEntityChanger)
			.property(Property.SCALE,
				"The scale multipliers to use for a displays. The x, y, and z scales of the display will be multiplied by the respective components of the vector.",
				Skript.instance(),
				new DisplayScaleHandler())
			.property(Property.COLOR,
				"The background color of a text display. Other display types do not have a color. Can be set or reset.",
				Skript.instance(),
				new DisplayColorHandler());
	}

	private static class DisplayScaleHandler implements ExpressionPropertyHandler<Display, Vector> {
		//<editor-fold desc="scale property for displays" defaultstate="collapsed">
		@Override
		public @NotNull Vector convert(Display display) {
			return Vector.fromJOML(display.getTransformation().getScale());
		}

		@Override
		public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
			return switch (mode) {
				case SET, RESET -> CollectionUtils.array(Vector.class);
				default -> null;
			};
		}

		@Override
		public void change(Display display, Object @Nullable [] delta, ChangeMode mode) {
			Vector3f vector = null;
			if (mode == ChangeMode.RESET)
				vector = new Vector3f(1F, 1F, 1F);
			if (delta != null)
				vector = ((Vector) delta[0]).toVector3f();
			if (vector == null || !vector.isFinite())
				return;
			Transformation transformation = display.getTransformation();
			Transformation change = new Transformation(
				transformation.getTranslation(),
				transformation.getLeftRotation(),
				vector,
				transformation.getRightRotation());
			display.setTransformation(change);
		}

		@Override
		public @NotNull Class<Vector> returnType() {
			return Vector.class;
		}
		//</editor-fold>
	}

	public static class DisplayColorHandler implements ExpressionPropertyHandler<Display, Color> {
		//<editor-fold desc="color property for displays" defaultstate="collapsed">
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
		//</editor-fold>
	}

}
