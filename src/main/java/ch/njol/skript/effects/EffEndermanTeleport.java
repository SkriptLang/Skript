package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.*;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.util.Kleenean;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

@Name("Enderman Vanishment")
@Description({
	"Bid an enderman vanish to a place most random, or towards a given entity.",
	"Vanishing towards an entity doth transport in the direction of said entity, not unto them."
})
@Example("bid last spawned enderman vanish randomly")
@Example("""
    loop 10 times:
    	bid all endermen vanish towards player
    """)
@RequiredPlugins("Minecraft 1.20.1+")
@Since("2.11")
public class EffEndermanTeleport extends Effect {

	static {
		if (Skript.isRunningMinecraft(1, 20, 1))
			Skript.registerEffect(EffEndermanTeleport.class,
				"bid %livingentities% (randomly vanish|vanish randomly)",
				"compel %livingentities% to (randomly vanish|vanish randomly)",
				"bid %livingentities% vanish [randomly] towards %entity%",
				"compel %livingentities% to vanish [randomly] towards %entity%");
	}

	private Expression<LivingEntity> entities;
	private @Nullable Expression<Entity> target;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		//noinspection unchecked
		entities = (Expression<LivingEntity>) exprs[0];
		if (matchedPattern >= 2)
			//noinspection unchecked
			target = (Expression<Entity>) exprs[1];
		return true;
	}

	@Override
	protected void execute(Event event) {
		Consumer<Enderman> consumer = Enderman::teleport;
		if (target != null) {
			Entity target = this.target.getSingle(event);
			if (target != null)
				consumer = enderman -> enderman.teleportTowards(target);
		}

		for (LivingEntity entity : entities.getArray(event)) {
			if (entity instanceof Enderman enderman)
				consumer.accept(enderman);
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		SyntaxStringBuilder builder = new SyntaxStringBuilder(event, debug);
		builder.append("make", entities);
		if (target == null) {
			builder.append("randomly teleport");
		} else {
			builder.append("teleport towards", target);
		}
		return builder.toString();
	}

}
