package org.skriptlang.skript.bukkit.toolcomponent;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.registrations.Classes;
import org.bukkit.inventory.meta.components.ToolComponent;
import org.bukkit.inventory.meta.components.ToolComponent.ToolRule;

import java.io.IOException;

public class ToolComponentModule {

	public static void load() throws IOException {
		if (!Skript.classExists("org.bukkit.inventory.meta.components.ToolComponent"))
			return;

		Skript.getAddonInstance().loadClasses("org.skriptlang.skript.bukkit.toolcomponent", "elements");

		Classes.registerClass(new ClassInfo<>(ToolComponent.class, "toolcomponent")
			.user("tool ?components?")
			.name("Tool Component")
			.description("The tool component of an item")
			.requiredPlugins("Minecraft 1.20.6+")
			.since("INSERT VERSION")
		);

		Classes.registerClass(new ClassInfo<>(ToolRule.class, "toolrule")
			.user("tool ?rules?")
			.name("Tool Rule")
			.description("The tool rule of a tool component")
			.requiredPlugins("Minecraft 1.20.6+")
			.since("INSERT VERSION")
		);

	}

}
