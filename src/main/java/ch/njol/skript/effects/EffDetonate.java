package ch.njol.skript.effects;

import org.bukkit.entity.*;
import org.bukkit.entity.minecart.ExplosiveMinecart;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;


@Name("Detonate Entity")
@Description("Automatically detonates an entity.\n Accepted entities are 'Fireworks, TNT Minecarts and WindCharges")
@Examples("detonate last launched firework")
@Since("INSERT VERSION")
public class EffDetonate extends Effect {
	static {
		Skript.registerEffect(EffDetonate.class, "detonate [a[n]|the] %entities%");
	}

	private Expression<Entity> entities;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		this.entities = (Expression<Entity>) exprs[0];
 		return true;
	}

	@Override
	protected void execute(Event event) {
		for (Object entity : entities.getArray(event)) {
			if (entity instanceof Firework) {
				((Firework) entity).detonate();
			}
			if (entity instanceof WindCharge) {
				((WindCharge) entity).explode();
			}
			if (entity instanceof ExplosiveMinecart) {
				((ExplosiveMinecart) entity).explode();
			}
		}
	}

	public String toString(@Nullable Event event, boolean debug) {
		return "detonate " + entities.toString(event, debug);
	}

}
