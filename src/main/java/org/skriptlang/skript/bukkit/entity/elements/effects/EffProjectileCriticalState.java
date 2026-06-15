package org.skriptlang.skript.bukkit.entity.elements.effects;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.entity.EntityData;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.util.Kleenean;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.Projectile;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Projectile Critical State")
@Description("""
	Change whether a projectile is in its critical state. When in critical state \
	the projectile will have a trail of particles and deal more damage.
	Currently this only applies to arrows and tridents.
	""")
@Example("""
	on shoot:
		make event-projectile crit
		toggle whether event-projectile will be critical
	""")
@Since("INSERT VERSION")
public class EffProjectileCriticalState extends Effect {

	public static void register(SyntaxRegistry registry) {
		registry.register(
			SyntaxRegistry.EFFECT,
			SyntaxInfo.builder(EffProjectileCriticalState.class)
				.addPatterns(
					"make %projectiles% [negate:not] crit[ical]",
					"toggle whether %projectiles% will [be] crit[ical]"
				)
				.supplier(EffProjectileCriticalState::new)
				.build()
		);
	}

	private Expression<Projectile> projectiles;
	private boolean negated;
	private boolean toggle;

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		//noinspection unchecked
		projectiles = (Expression<Projectile>) expressions[0];
		negated = parseResult.hasTag("negate");
		toggle = matchedPattern == 1;
		return true;
	}

	@Override
	protected void execute(Event event) {
		for (Projectile projectile : projectiles.getArray(event)) {
			if (projectile instanceof AbstractArrow abstractArrow) {
				abstractArrow.setCritical(toggle ? !abstractArrow.isCritical() : !negated);
			} else {
				warning("This projectile (" + EntityData.toString(projectile) + ") is not supported. This only applies to arrows and tridents.");
			}
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		if (toggle) {
			return new SyntaxStringBuilder(event, debug)
				.append("toggle whether", projectiles, "will crit")
				.toString();
		}

		return new SyntaxStringBuilder(event, debug)
			.append("make", projectiles)
			.appendIf(negated, "not")
			.append("crit")
			.toString();
	}

}
