package org.skriptlang.skript.bukkit.entity.displays;

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
import org.skriptlang.skript.addon.AddonModule;
import org.skriptlang.skript.addon.HierarchicalAddonModule;
import org.skriptlang.skript.addon.SkriptAddon;
import org.skriptlang.skript.bukkit.entity.displays.elements.expressions.*;
import org.skriptlang.skript.bukkit.entity.displays.item.elements.expressions.ExprItemDisplayTransform;
import org.skriptlang.skript.bukkit.entity.displays.text.elements.conditions.CondTextDisplayHasDropShadow;
import org.skriptlang.skript.bukkit.entity.displays.text.elements.conditions.CondTextDisplaySeeThroughBlocks;
import org.skriptlang.skript.bukkit.entity.displays.text.elements.effects.EffTextDisplayDropShadow;
import org.skriptlang.skript.bukkit.entity.displays.text.elements.effects.EffTextDisplaySeeThroughBlocks;
import org.skriptlang.skript.bukkit.entity.displays.text.elements.expressions.ExprTextDisplayAlignment;
import org.skriptlang.skript.bukkit.entity.displays.text.elements.expressions.ExprTextDisplayLineWidth;
import org.skriptlang.skript.bukkit.entity.displays.text.elements.expressions.ExprTextDisplayOpacity;
import org.skriptlang.skript.lang.converter.Converter;
import org.skriptlang.skript.lang.converter.Converters;
import org.skriptlang.skript.lang.properties.Property;
import org.skriptlang.skript.lang.properties.handlers.base.ExpressionPropertyHandler;

public class DisplayModule extends HierarchicalAddonModule {

	public DisplayModule(AddonModule parentModule) {
		super(parentModule);
	}

	@Override
	protected void initSelf(SkriptAddon addon) {
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

	@Override
	protected void loadSelf(SkriptAddon addon) {
		register(addon,
			DisplayData::register,

			ExprDisplayBillboard::register,
			ExprDisplayBrightness::register,
			ExprDisplayGlowOverride::register,
			ExprDisplayHeightWidth::register,
			ExprDisplayInterpolation::register,
			ExprDisplayShadow::register,
			ExprDisplayTeleportDuration::register,
			ExprDisplayTransformationRotation::register,
			ExprDisplayTransformationScaleTranslation::register,
			ExprDisplayViewRange::register,

			ExprItemDisplayTransform::register,

			CondTextDisplayHasDropShadow::register,
			CondTextDisplaySeeThroughBlocks::register,

			EffTextDisplayDropShadow::register,
			EffTextDisplaySeeThroughBlocks::register,

			ExprTextDisplayAlignment::register,
			ExprTextDisplayLineWidth::register,
			ExprTextDisplayOpacity::register
		);
	}

	@Override
	public String name() {
		return "displays";
	}

}
