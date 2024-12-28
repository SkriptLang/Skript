package org.skriptlang.skript.bukkit.ticking;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.util.SimpleEvent;
import ch.njol.skript.registrations.EventValues;
import ch.njol.skript.util.Getter;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityBreedEvent;
import org.bukkit.event.entity.EntityEnterLoveModeEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

public class TickingModule {

	public static void load() throws IOException {
		if (!Skript.classExists("org.bukkit.ServerTickManager"))
			return;

		Skript.getAddonInstance().loadClasses("org.skriptlang.skript.bukkit.ticking", "elements");
	}
}
