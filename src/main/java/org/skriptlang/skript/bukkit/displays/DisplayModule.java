package org.skriptlang.skript.bukkit.displays;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.EnumClassInfo;
import ch.njol.skript.classes.data.DefaultChangers;
import ch.njol.skript.expressions.base.EventValueExpression;
import ch.njol.skript.registrations.Classes;
import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.TextDisplay;
import org.skriptlang.skript.addon.AddonModule;
import org.skriptlang.skript.addon.SkriptAddon;
import org.skriptlang.skript.bukkit.displays.generic.*;
import org.skriptlang.skript.bukkit.displays.item.ExprItemDisplayTransform;
import org.skriptlang.skript.bukkit.displays.text.*;
import org.skriptlang.skript.lang.converter.Converter;
import org.skriptlang.skript.lang.converter.Converters;
import org.skriptlang.skript.registration.SyntaxRegistry;

public class DisplayModule implements AddonModule {

	private static final boolean DISPLAYS_EXIST = Skript.classExists("org.bukkit.entity.Display");

	@Override
	public void init(SkriptAddon addon) {
		if (DISPLAYS_EXIST) {
			DisplayData.register();

			Classes.registerClass(new ClassInfo<>(Display.class, "display")
				.user("displays?")
				.name("Display Entity")
				.description("A text, block or item display entity.")
				.since("2.10")
				.defaultExpression(new EventValueExpression<>(Display.class))
				.changer(DefaultChangers.nonLivingEntityChanger));

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
		} else {
			Skript.error("Failed to load the display module: org.bukkit.entity.Display class doesn't exist.");
		}
	}

	@Override
	public void load(SkriptAddon addon) {
		if (DISPLAYS_EXIST) {
			SyntaxRegistry registry = addon.syntaxRegistry();

			ExprDisplayBillboard.register(registry);
			ExprDisplayBrightness.register(registry);
			ExprDisplayGlowOverride.register(registry);
			ExprDisplayHeightWidth.register(registry);
			ExprDisplayInterpolation.register(registry);
			ExprDisplayShadow.register(registry);
			ExprDisplayTeleportDuration.register(registry); // 1.20.4+
			ExprDisplayTransformationRotation.register(registry);
			ExprDisplayTransformationScaleTranslation.register(registry);
			ExprDisplayViewRange.register(registry);

			ExprItemDisplayTransform.register(registry);

			CondTextDisplayHasDropShadow.register(registry);
			CondTextDisplaySeeThroughBlocks.register(registry);
			EffTextDisplayDropShadow.register(registry);
			EffTextDisplaySeeThroughBlocks.register(registry);
			ExprTextDisplayAlignment.register(registry);
			ExprTextDisplayLineWidth.register(registry);
			ExprTextDisplayOpacity.register(registry);
		} else {
			Skript.error("Failed to load the display module: org.bukkit.entity.Display class doesn't exist.");
		}
	}

}
