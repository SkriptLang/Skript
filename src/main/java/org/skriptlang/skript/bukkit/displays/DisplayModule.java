package org.skriptlang.skript.bukkit.displays;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.EnumClassInfo;
import ch.njol.skript.classes.data.DefaultChangers;
import ch.njol.skript.expressions.base.EventValueExpression;
import ch.njol.skript.registrations.Classes;
import org.bukkit.entity.Display;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.TextDisplay;

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
			.since("INSERT VERSION")
			.defaultExpression(new EventValueExpression<>(Display.class))
			.changer(DefaultChangers.nonLivingEntityChanger));

		Classes.registerClass(new EnumClassInfo<>(Display.Billboard.class, "billboard", "billboards")
			.user("billboards?")
			.name("Display Billboard")
			.description("Represents the billboard setting of a display.")
			.since("INSERT VERSION"));

		Classes.registerClass(new EnumClassInfo<>(TextDisplay.TextAlignment.class, "textalignment", "text alignments")
			.user("text ?alignments?")
			.name("Display Text Alignment")
			.description("Represents the text alignment setting of a text display.")
			.since("INSERT VERSION"));

		Classes.registerClass(new EnumClassInfo<>(ItemDisplay.ItemDisplayTransform.class, "itemdisplaytransform", "item display transforms")
			.user("item ?display ?transforms?")
			.name("Item Display Transforms")
			.description("Represents the transform setting of an item display.")
			.since("INSERT VERSION"));
	}

}
