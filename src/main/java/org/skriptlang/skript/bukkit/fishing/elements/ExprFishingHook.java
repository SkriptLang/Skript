package org.skriptlang.skript.bukkit.fishing.elements;

import ch.njol.skript.doc.*;
import ch.njol.skript.expressions.base.EventValueExpression;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FishHook;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Fishing Hook")
@Description("The <a href='classes.html#entity'>fishing hook</a> in a fishing event.")
@Examples({
	"on fish line cast:",
		"\twait a second",
		"\tteleport player to fishing hook"
})
@Events("Fishing")
@Since("INSERT VERSION")
public class ExprFishingHook extends EventValueExpression<Entity> {

	static {
		register(ExprFishingHook.class, Entity.class, "fish[ing] (hook|bobber)");
	}

	public ExprFishingHook() {
		super(FishHook.class);
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "the fishing hook";
	}

}
