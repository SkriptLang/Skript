package ch.njol.skript.effects;

import org.bukkit.entity.Entity;
import org.bukkit.entity.WindCharge;
import org.bukkit.entity.minecart.ExplosiveMinecart;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;

@Name("Detonate Entities")
@Description("Immediately detonates an entity. Accepted entities are fireworks, TNT minecarts, primed TNT, wind charges and creepers.")
@Examples("detonate last launched firework")
@Since("INSERT VERSION")
public class EffDetonate extends Effect {

	private static final boolean HAS_WINDCHARGE = Skript.classExists("org.bukkit.entity.WindCharge");

	static {
		Skript.registerEffect(EffDetonate.class, "detonate %entities%");
	}

	private Expression<Entity> entities;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		entities = (Expression<Entity>) exprs[0];
 		return true;
	}

	@Override
	protected void execute(Event event) {
		for (Entity entity : entities.getArray(event)) {
			if (entity instanceof Firework firework) {
				firework.detonate();
			} else if (HAS_WINDCHARGE && entity instanceof WindCharge windCharge) {
				windCharge.explode();
			} else if (entity instanceof ExplosiveMinecart explosiveMinecart) {
				explosiveMinecart.explode();
			} else if (entity instanceof Creeper creeper) {
				creeper.explode();
			} else if (entity instanceof TNTPrimed tntPrimed) {
				tntPrimed.setFuseTicks(0);
			}
		}
	}

	public String toString(@Nullable Event event, boolean debug) {
		return "detonate " + entities.toString(event, debug);
	}

}
