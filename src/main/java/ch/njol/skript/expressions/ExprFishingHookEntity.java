package ch.njol.skript.expressions;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Events;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FishHook;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Fishing Hooked Entity")
@Description("Returns the hooked entity of the fishing hook.")
@Examples({
	"on entity hooked:",
		"\tif hooked entity of fishing hook is a player:",
			"\t\tteleport hooked entity of fishing hook to player"
})
@Events("Fishing")
@Since("INSERT VERSION")
public class ExprFishingHookEntity extends SimplePropertyExpression<FishHook, Entity> {

	static {
		register(ExprFishingHookEntity.class, Entity.class, "hook[ed] entity", "fishinghooks");
	}

	@Override
	public @Nullable Entity convert(FishHook fishHook) {
		return fishHook.getHookedEntity();
	}

	@Override
	public Class<? extends Entity> getReturnType() {
		return Entity.class;
	}

	@Override
	protected String getPropertyName() {
		return "hooked entity";
	}

	@Override
	public @Nullable Class<?>[] acceptChange(ChangeMode mode) {
		return switch (mode) {
			case DELETE, SET -> CollectionUtils.array(Entity.class);
			default -> null;
		};
	}

	@Override
	public void change(Event event, @Nullable Object[] delta, ChangeMode mode) {
		FishHook[] hooks = getExpr().getArray(event);
		switch (mode) {
			case SET -> {
				for (FishHook fishHook : hooks)
					fishHook.setHookedEntity((Entity) delta[0]);
			}
			case DELETE -> {
				for (FishHook fishHook : hooks) {
					if (fishHook.getHookedEntity() != null && !(fishHook.getHookedEntity() instanceof Player))
						fishHook.getHookedEntity().remove();
				}
			}
			default -> throw new IllegalStateException("Unexpected value: " + mode);
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "hooked entity of " + getExpr().toString(event, debug);
	}

}
