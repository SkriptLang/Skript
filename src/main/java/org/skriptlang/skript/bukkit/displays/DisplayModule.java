package org.skriptlang.skript.bukkit.displays;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.EnumClassInfo;
import ch.njol.skript.classes.data.DefaultChangers;
import ch.njol.skript.expressions.base.EventValueExpression;
import ch.njol.skript.registrations.Classes;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.TextDisplay;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import org.skriptlang.skript.lang.converter.Converter;
import org.skriptlang.skript.lang.converter.Converters;
import org.skriptlang.skript.lang.properties.Property;
import org.skriptlang.skript.lang.properties.handlers.base.ExpressionPropertyHandler;

import java.io.IOException;

public class DisplayModule {

	public static void load() throws IOException {
		// abort if no class exists
		if (!Skript.classExists("org.bukkit.entity.Display"))
			return;

		// load classes (todo: replace with registering methods after regitration api
		Skript.getAddonInstance().loadClasses("org.skriptlang.skript.bukkit", "displays");

		// Classes

		Classes.registerClass(new ClassInfo<>(Display.class, "display")
			.user("displays?")
			.name("Display Entity")
			.description("A text, block or item display entity.")
			.since("2.10")
			.defaultExpression(new EventValueExpression<>(Display.class))
			.changer(DefaultChangers.nonLivingEntityChanger)

			.property(Property.SCALE,
				"The scale multipliers to use for a displays. The x, y, and z scales of the display will be multiplied by the respective components of the vector.",
				Skript.instance(),
				//<editor-fold desc="scale handler" default-state=collapsed>
				new ExpressionPropertyHandler<Display, Vector>() {
					@Override
					public @NotNull Vector convert(Display propertyHolder) {
						return Vector.fromJOML(propertyHolder.getTransformation().getScale());
					}

					@Override
					public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
						return switch (mode) {
							case SET, RESET -> CollectionUtils.array(Vector.class);
							default -> null;
						};
					}

					@Override
					public void change(Display propertyHolder, Object @Nullable [] delta, ChangeMode mode) {
						Vector3f vector = null;
						if (mode == ChangeMode.RESET)
							vector = new Vector3f(1F, 1F, 1F);
						if (delta != null)
							vector = ((Vector) delta[0]).toVector3f();
						if (vector == null || !vector.isFinite())
							return;
						Transformation transformation = propertyHolder.getTransformation();
						Transformation change = new Transformation(
								transformation.getTranslation(),
								transformation.getLeftRotation(),
								vector,
								transformation.getRightRotation());
						propertyHolder.setTransformation(change);
					}

					@Override
					public @NotNull Class<Vector> returnType() {
						return Vector.class;
					}
				}
				//</editor-fold>
			));

		Classes.registerClass(new EnumClassInfo<>(Display.Billboard.class, "billboard", "billboards")
			.user("billboards?")
			.name("Display Billboard")
			.description("Represents the billboard setting of a display.")
			.since("2.10"));

		Classes.registerClass(new EnumClassInfo<>(TextDisplay.TextAlignment.class, "textalignment", "text alignments")
			.user("text ?alignments?")
			.name("Display Text Alignment")
			.description("Represents the text alignment setting of a text display.")
			.since("2.10"));

		Classes.registerClass(new EnumClassInfo<>(ItemDisplay.ItemDisplayTransform.class, "itemdisplaytransform", "item display transforms")
			.user("item ?display ?transforms?")
			.name("Item Display Transforms")
			.description("Represents the transform setting of an item display.")
			.since("2.10"));

		Converters.registerConverter(Entity.class, Display.class,
				entity -> entity instanceof Display display ? display : null,
				Converter.NO_RIGHT_CHAINING);
	}

}
