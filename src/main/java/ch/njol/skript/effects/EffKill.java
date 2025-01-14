package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.bukkitutil.DamageUtils;
import ch.njol.skript.bukkitutil.HealthUtils;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.EnderDragonPart;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageEvent;
import org.jetbrains.annotations.Nullable;

@Name("Kill")
@Description("Kills an entity.")
@Examples({
	"kill the player",
	"kill all creepers in the player's world",
	"kill all endermen, witches and bats"
})
@Since("1.0, 2.10 (ignoring totem of undying)")
public class EffKill extends Effect {

	private static final boolean SUPPORTS_DAMAGE_SOURCE = Skript.classExists("org.bukkit.damage.DamageSource");

	static {
		Skript.registerEffect(EffKill.class, "kill %entities%");
	}

	private Expression<Entity> entities;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parser) {
		//noinspection unchecked
		this.entities = (Expression<Entity>) exprs[0];
		return true;
	}

	@Override
	protected void execute(Event event) {
		for (Entity entity : entities.getArray(event)) {

			if (entity instanceof EnderDragonPart part)
				entity = part.getParent();

			if (entity instanceof Damageable damageable) {
				if (SUPPORTS_DAMAGE_SOURCE) {
					EntityDamageEvent.DamageCause cause = EntityDamageEvent.DamageCause.KILL;
					HealthUtils.damage(damageable, 100 + damageable.getHealth(), DamageUtils.getDamageSourceFromCause(cause));
				} else {
					HealthUtils.setHealth(damageable, 0);
					HealthUtils.damage(damageable, 1);
				}
			}

			// if everything done so far has failed to kill this thing
			// We also don't want to remove a player as this would remove the player's data from the server.
			if (entity.isValid() && !(entity instanceof Player))
				entity.remove();

		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "kill " + entities.toString(event, debug);
	}

}
