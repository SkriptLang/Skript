package org.skriptlang.skript.bukkit.entity.displays;

import ch.njol.skript.classes.EnumClassInfo;
import ch.njol.skript.registrations.Classes;
import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.TextDisplay;
import org.jetbrains.annotations.Nullable;
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

public class DisplayModule extends HierarchicalAddonModule {

	public DisplayModule(AddonModule parentModule) {
		super(parentModule);
	}

	@Override
	protected void initSelf(SkriptAddon addon) {
		// Classes

		Classes.registerClass(new DisplayClassInfo());

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
