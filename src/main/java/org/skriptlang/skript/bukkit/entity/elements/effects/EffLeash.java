package org.skriptlang.skript.bukkit.entity.elements.effects;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Leash entities")
@Description("""
	Leash living entities to other entities. When trying to leash an Ender Dragon, Wither, Player, or a Bat, this effect will not work.
	See <a href=\\"https://jd.papermc.io/paper/1.21.11/org/bukkit/entity/LivingEntity.html#setLeashHolder(org.bukkit.entity.Entity)\\">Paper's Javadocs for more info</a>.
	""")
@Example("""
	on right click on entity:
		leash event-entity to player
		send "&aYou leashed &2%event-entity%!" to player
	""")
@Since("2.3")
public class EffLeash extends Effect {

	public static void register(SyntaxRegistry registry) {
		registry.register(
			SyntaxRegistry.EFFECT,
			SyntaxInfo.builder(EffLeash.class)
				.addPatterns(
					"(leash|lead) %livingentities% to %entity%",
					"make %entity% (leash|lead) %livingentities%",
					"un(leash|lead) [holder of] %livingentities%"
				).supplier(EffLeash::new)
				.build()
		);
	}

	private Expression<Entity> holder;
	private Expression<LivingEntity> targets;
	private boolean leash;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		leash = matchedPattern != 2;
		if (leash) {
			holder = (Expression<Entity>) exprs[1 - matchedPattern];
			targets = (Expression<LivingEntity>) exprs[matchedPattern];
		} else {
			targets = (Expression<LivingEntity>) exprs[0];
		}
		return true;
	}

	@Override
	protected void execute(Event event) {
		if (leash) {
			Entity holder = this.holder.getSingle(event);
			if (holder == null)
				return;
			for (LivingEntity target : targets.getArray(event))
				target.setLeashHolder(holder);
		} else {
			for (LivingEntity target : targets.getArray(event))
				target.setLeashHolder(null);
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		if (leash) {
			return "leash " + targets.toString(event, debug) + " to " + holder.toString(event, debug);
		} else {
			return "unleash " + targets.toString(event, debug);
		}
	}

}
