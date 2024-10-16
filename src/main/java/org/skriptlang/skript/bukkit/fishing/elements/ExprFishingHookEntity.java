package org.skriptlang.skript.bukkit.fishing.elements;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.*;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FishHook;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerFishEvent;
import org.jetbrains.annotations.Nullable;

@Name("Fishing Hooked Entity")
@Description("Returns the hooked entity in the hooked event.")
@Examples({
	"on entity hooked:",
		"\tif hooked entity is a player:",
			"\t\tteleport hooked entity to player",
})
@Events("Fishing")
@Since("INSERT VERSION")
public class ExprFishingHookEntity extends SimpleExpression<Entity> {

	static {
		Skript.registerExpression(ExprFishingHookEntity.class, Entity.class, ExpressionType.EVENT,
			"hook[ed] entity");
	}

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern,
						Kleenean isDelayed, ParseResult parseResult) {
		if (!getParser().isCurrentEvent(PlayerFishEvent.class)) {
			Skript.error("The 'hooked entity' expression can only be used in the fishing event.");
			return false;
		}

		return true;
	}

	@Override
	protected Entity @Nullable [] get(Event event) {
		if (!(event instanceof PlayerFishEvent fishEvent))
			return null;

		return new Entity[] {fishEvent.getHook().getHookedEntity()};
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		return switch (mode) {
			case SET, DELETE -> CollectionUtils.array(Entity.class);
			default -> null;
		};
	}

	@Override
	public void change(Event event, @Nullable Object[] delta, ChangeMode mode) {
		if (!(event instanceof PlayerFishEvent fishEvent))
			return;

		FishHook hook = fishEvent.getHook();

		switch (mode) {
			case SET -> hook.setHookedEntity((Entity) delta[0]);
			case DELETE -> {
				if (hook.getHookedEntity() != null && !(hook.getHookedEntity() instanceof Player))
					hook.getHookedEntity().remove();
			}
			default -> throw new IllegalStateException("Unexpected value: " + mode);
		}
	}

	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public Class<? extends Entity> getReturnType() {
		return Entity.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "hooked entity";
	}

}
