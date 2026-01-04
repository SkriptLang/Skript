package org.skriptlang.skript.bukkit.fishing.elements.expressions;

import ch.njol.skript.doc.*;
import ch.njol.skript.expressions.base.EventValueExpression;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FishHook;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.docs.Origin;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Fishing Hook")
@Description("The <a href='#entity'>fishing hook</a> in a fishing event.")
@Examples({
	"on fish line cast:",
		"\twait a second",
		"\tteleport player to fishing hook"
})
@Events("Fishing")
@Since("2.10")
public class ExprFishingHook extends EventValueExpression<Entity> {

	public static void register(SyntaxRegistry registry, Origin origin) {
		registry.register(SyntaxRegistry.EXPRESSION,
			infoBuilder(ExprFishingHook.class, Entity.class, "fish[ing] (hook|bobber)")
				.supplier(ExprFishingHook::new)
				.origin(origin)
				.build());
	}

	public ExprFishingHook() {
		super(FishHook.class);
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "the fishing hook";
	}

}
