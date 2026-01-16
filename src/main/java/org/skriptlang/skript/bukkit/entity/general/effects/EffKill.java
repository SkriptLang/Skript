package org.skriptlang.skript.bukkit.entity.general.effects;

import ch.njol.skript.bukkitutil.DamageUtils;
import ch.njol.skript.bukkitutil.HealthUtils;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
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
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Kill")
@Description("Kills an entity.")
@Example("kill the player")
@Example("kill all creepers in the player's world")
@Example("kill all endermen, witches and bats")
@Since("1.0, 2.10 (ignoring totem of undying)")
public class EffKill extends Effect {

	public static void register(SyntaxRegistry registry) {
		registry.register(
			SyntaxRegistry.EFFECT,
			SyntaxInfo.builder(EffKill.class)
				.addPattern("kill %entities%")
				.supplier(EffKill::new)
				.build()
		);
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
				EntityDamageEvent.DamageCause cause = EntityDamageEvent.DamageCause.KILL;
				HealthUtils.damage(damageable, 100 + damageable.getHealth(), DamageUtils.getDamageSourceFromCause(cause));
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
