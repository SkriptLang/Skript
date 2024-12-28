package org.skriptlang.skript.bukkit.ticking.elements.conditions;

import ch.njol.skript.bukkitutil.ServerUtils;
import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import org.bukkit.entity.Entity;

@Name("Is Entity Tick Frozen")
@Description("Checks if the specified entities are frozen due to the server's ticking state.")
@Examples("if target entity is tick frozen:")
@Since("INSERT VERSION")
@RequiredPlugins("Minecraft 1.20.4+")
public class CondIsTickFrozen extends PropertyCondition<Entity> {

	static {
		if (ServerUtils.isServerTickManagerPresent())
			register(CondIsTickFrozen.class, "tick frozen", "entities");
	}

	@Override
	public boolean check(Entity entity) {
		return ServerUtils.getServerTickManager().isFrozen(entity);
	}

	@Override
	protected String getPropertyName() {
		return "tick frozen";
	}

}

