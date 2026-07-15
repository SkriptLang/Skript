package org.skriptlang.skript.bukkit.world.worldborder.elements;

import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.Parser;
import ch.njol.skript.expressions.base.EventValueExpression;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.registrations.Classes;
import org.bukkit.WorldBorder;
import org.skriptlang.skript.addon.AddonModule;
import org.skriptlang.skript.addon.HierarchicalAddonModule;
import org.skriptlang.skript.addon.SkriptAddon;
import org.skriptlang.skript.bukkit.lang.eventvalue.EventValueRegistry;
import org.skriptlang.skript.bukkit.world.worldborder.elements.effects.EffWorldBorderExpand;
import org.skriptlang.skript.bukkit.world.worldborder.elements.events.WorldBorderEvents;
import org.skriptlang.skript.bukkit.world.worldborder.elements.expressions.*;

public class WorldBorderModule extends HierarchicalAddonModule {

	public WorldBorderModule(AddonModule parentModule) {
		super(parentModule);
	}

	@Override
	protected void initSelf(SkriptAddon addon) {
		Classes.registerClass(new ClassInfo<>(WorldBorder.class, "worldborder")
			.user("world ?borders?")
			.name("World Border")
			.description("Represents the border of a world or player.")
			.since("2.11")
			.parser(new Parser<>() {
				@Override
				public boolean canParse(ParseContext context) {
					return false;
				}

				@Override
				public String toString(WorldBorder border, int flags) {
					if (border.getWorld() == null)
						return "virtual world border";
					return "world border of world named '" + border.getWorld().getName() + "'";
				}

				@Override
				public String toVariableNameString(WorldBorder border) {
					return toString(border, 0);
				}
			})
			.defaultExpression(new EventValueExpression<>(WorldBorder.class)));
	}

	@Override
	protected void loadSelf(SkriptAddon addon) {
		EventValueRegistry eventValueRegistry = addon.registry(EventValueRegistry.class);

		WorldBorderEvents.register(moduleRegistry(addon), eventValueRegistry);

		register(addon,
			syntaxRegistry -> ExprSecCreateWorldBorder.register(syntaxRegistry, eventValueRegistry),
			EffWorldBorderExpand::register,
			ExprWorldBorder::register,
			ExprWorldBorderCenter::register,
			ExprWorldBorderDamageAmount::register,
			ExprWorldBorderDamageBuffer::register,
			ExprWorldBorderSize::register,
			ExprWorldBorderWarningDistance::register,
			ExprWorldBorderWarningTime::register
		);
	}

	@Override
	public String name() {
		return "worldborder";
	}

}
