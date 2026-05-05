package org.skriptlang.skript.bukkit.entity.elements.effects;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.util.Kleenean;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.Projectile;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.log.runtime.RuntimeErrorProducer;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Projectile Critical State")
@Description("Change whether a projectile is in its critical state. As of now this only applies to arrows and tridents.")
@Example("""
	on shoot:
		enable projectile critical state of event-projectile
	""")
@Since("INSERT VERSION")
public class EffProjectileCriticalState extends Effect implements RuntimeErrorProducer {

	public static void register(SyntaxRegistry registry) {
		registry.register(
			SyntaxRegistry.EFFECT,
			SyntaxInfo.builder(EffProjectileCriticalState.class)
				.addPatterns(
					"(enable|:disable) (projectile|arrow) critical (state|mode) (of|for) %projectiles%",
					"(enable|:disable) %projectiles%'s (projectile|arrow) critical (state|mode)"
				)
				.supplier(EffProjectileCriticalState::new)
				.build()
		);
	}

	private Expression<Projectile> projectiles;
	private boolean negated;

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
		//noinspection unchecked
		projectiles = (Expression<Projectile>) expressions[0];
		negated = parseResult.hasTag("disable");
		return true;
	}

	@Override
	protected void execute(Event event) {
		for (Projectile projectile : projectiles.getArray(event)) {
			if (projectile instanceof AbstractArrow abstractArrow) {
				abstractArrow.setCritical(!negated);
			} else {
				warning("This projectile is not supported. This only applies to arrows and tridents.");
			}
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return new SyntaxStringBuilder(event, debug)
			.append(negated ? "disable" : "enable")
			.append("projectile critical state", projectiles)
			.toString();
	}

}
