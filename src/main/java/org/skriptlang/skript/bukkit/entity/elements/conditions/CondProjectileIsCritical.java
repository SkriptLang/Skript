package org.skriptlang.skript.bukkit.entity.elements.conditions;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.Projectile;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Projectile Is Critical")
@Description("""
	Check whether a projectile is in its critical state. When in critical state the projectile will have a trail of particles and deal more damage.
	Currently this only applies to arrows and tridents.
	""")
@Example("""
	on shoot:
		if event-projectile will crit:
			enable projectile critical state of event-projectile
	""")
@Since("INSERT VERSION")
public class CondProjectileIsCritical extends PropertyCondition<Projectile> {

	public static void register(SyntaxRegistry registry) {
		registry.register(
			SyntaxRegistry.CONDITION,
			infoBuilder(
				CondProjectileIsCritical.class,
				PropertyType.WILL,
				"crit[ical]",
				"projectiles"
			)
				.supplier(CondProjectileIsCritical::new)
				.build()
		);
	}

	@Override
	public boolean check(Projectile projectile) {
		if (projectile instanceof AbstractArrow abstractArrow) {
			return abstractArrow.isCritical();
		}
		warning("This projectile is not supported. Critical projectile state only applies to arrows and tridents.");
		return false;
	}

	protected String getPropertyName() {
		return "projectile critical state";
	}

}
