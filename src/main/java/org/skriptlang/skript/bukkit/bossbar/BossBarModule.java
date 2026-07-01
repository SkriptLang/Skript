package org.skriptlang.skript.bukkit.bossbar;

import ch.njol.skript.classes.EnumClassInfo;
import ch.njol.skript.registrations.Classes;
import org.bukkit.boss.BarStyle;
import org.skriptlang.skript.addon.AddonModule;
import org.skriptlang.skript.addon.HierarchicalAddonModule;
import org.skriptlang.skript.addon.SkriptAddon;
import org.skriptlang.skript.bukkit.bossbar.elements.conditions.CondHasBossBarFlag;
import org.skriptlang.skript.bukkit.bossbar.elements.effects.EffBossBarFlags;
import org.skriptlang.skript.bukkit.bossbar.elements.expressions.*;
import org.skriptlang.skript.bukkit.lang.eventvalue.EventValueRegistry;

public class BossBarModule extends HierarchicalAddonModule {

	public BossBarModule(AddonModule parentModule) {
		super(parentModule);
	}

	@Override
	protected void initSelf(SkriptAddon addon) {
		Classes.registerClass(new BossBarClassInfo(addon));
		Classes.registerClass(new KeyedBossBarClassInfo());
		Classes.registerClass(new EnumClassInfo<>(BarStyle.class, "bossbarstyle", "boss bar styles")
			.user("boss ?bar styles?")
			.name("Boss Bar Style")
			.description("The style of a boss bar.")
			.since("INSERT VERSION"));
	}

	@Override
	protected void loadSelf(SkriptAddon addon) {
		EventValueRegistry eventValueRegistry = addon.registry(EventValueRegistry.class);
		register(addon,
			syntaxRegistry -> ExprSecCreateBossBar.register(syntaxRegistry, eventValueRegistry),
			ExprBossBarFromKey::register,
			ExprBossBarFromEntity::register,
			ExprKeyOfBossBar::register,
			EffBossBarFlags::register,
			CondHasBossBarFlag::register
		);
	}

	@Override
	public String name() {
		return "boss bar";
	}

}
