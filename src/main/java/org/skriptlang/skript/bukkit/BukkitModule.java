package org.skriptlang.skript.bukkit;

import ch.njol.skript.Skript;
import org.skriptlang.skript.addon.AddonModule;
import org.skriptlang.skript.addon.SkriptAddon;
import org.skriptlang.skript.bukkit.brewing.BrewingModule;
import org.skriptlang.skript.bukkit.damagesource.DamageSourceModule;
import org.skriptlang.skript.bukkit.elements.conditions.CondIsWaxed;
import org.skriptlang.skript.bukkit.elements.effects.EffWax;
import org.skriptlang.skript.bukkit.elements.expressions.ExprCopperGolemOxidationTime;
import org.skriptlang.skript.bukkit.elements.expressions.ExprCopperGolemPose;
import org.skriptlang.skript.bukkit.elements.expressions.ExprCopperState;
import org.skriptlang.skript.bukkit.itemcomponents.ItemComponentModule;
import org.skriptlang.skript.registration.SyntaxRegistry;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

public class BukkitModule implements AddonModule {

	@Override
	public void load(SkriptAddon addon) {
		addon.loadModules(
			new DamageSourceModule(),
			new ItemComponentModule(),
			new BrewingModule()
		);

		Set<Consumer<SyntaxRegistry>> consumers = new HashSet<>();

		consumers.addAll(Set.of(
			CondIsWaxed::register,

			EffWax::register,

			ExprCopperState::register
		));

		if (Skript.classExists("org.bukkit.entity.CopperGolem"))
			consumers.add(ExprCopperGolemOxidationTime::register);
		if (Skript.classExists("org.bukkit.block.data.type.CopperGolemStatue"))
			consumers.add(ExprCopperGolemPose::register);

		SyntaxRegistry registry = addon.syntaxRegistry();
		consumers.forEach(consumer -> consumer.accept(registry));
	}

}
