package org.skriptlang.skript.bukkit.misc;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.EnumClassInfo;
import ch.njol.skript.registrations.Classes;
import org.bukkit.block.data.type.CopperGolemStatue;
import org.skriptlang.skript.addon.AddonModule;
import org.skriptlang.skript.addon.SkriptAddon;
import org.skriptlang.skript.bukkit.misc.conditions.CondIsWaxed;
import org.skriptlang.skript.bukkit.misc.effects.EffRotate;
import org.skriptlang.skript.bukkit.misc.effects.EffWax;
import org.skriptlang.skript.bukkit.misc.expressions.ExprCopperGolemPose;
import org.skriptlang.skript.bukkit.misc.expressions.ExprCopperState;
import org.skriptlang.skript.bukkit.misc.expressions.ExprItemOfEntity;
import org.skriptlang.skript.bukkit.misc.expressions.ExprQuaternionAxisAngle;
import org.skriptlang.skript.bukkit.misc.expressions.ExprRotate;
import org.skriptlang.skript.bukkit.misc.expressions.ExprTextOf;
import org.skriptlang.skript.bukkit.paperutil.CopperState;
import org.skriptlang.skript.registration.SyntaxRegistry;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

public class MiscModule implements AddonModule {

	@Override
	public void init(SkriptAddon addon) {
		//noinspection unchecked,rawtypes
		Classes.registerClass(new EnumClassInfo<>((Class) CopperState.getStateClass(), "weatheringcopperstate", "weathering copper states")
			.user("(weathering ?)?copper ?states?")
			.name("Weathering Copper State")
			.description("The weathering state of a copper golem or copper block.")
			.since("INSERT VERSION")
		);

		if (Skript.classExists("org.bukkit.block.data.type.CopperGolemStatue$Pose")) {
			Classes.registerClass(new EnumClassInfo<>(CopperGolemStatue.Pose.class, "coppergolempose", "copper golem poses")
				.user("copper ?golem ?(statue ?)?poses?")
				.name("Copper Golem Pose")
				.description("The pose of a copper golem statue.")
				.requiredPlugins("Minecraft 1.21.9+")
				.since("INSERT VERSION")
			);
		}
	}

	@Override
	public void load(SkriptAddon addon) {
		Set<Consumer<SyntaxRegistry>> elementsToLoad = new HashSet<>(Set.of(
			CondIsWaxed::register,
			EffRotate::register,
			EffWax::register,
			ExprCopperState::register,
			ExprItemOfEntity::register,
			ExprQuaternionAxisAngle::register,
			ExprRotate::register
		));

		if (Skript.classExists("org.bukkit.entity.Display"))
			elementsToLoad.add(ExprTextOf::register);
		if (Skript.classExists("org.bukkit.block.data.type.CopperGolemStatue"))
			elementsToLoad.add(ExprCopperGolemPose::register);

		SyntaxRegistry registry = addon.syntaxRegistry();
		elementsToLoad.forEach(consumer -> consumer.accept(registry));
	}

}
