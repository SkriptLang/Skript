package org.skriptlang.skript.bukkit.damagesource.elements.expressions;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import org.bukkit.Location;
import org.bukkit.damage.DamageSource;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Wound's Origin - Provenance")
@Description({
	"The final locale whence the damage didst originate.",
	"The 'source location' for vanilla damage sources shall retrieve the 'damage location' if set. "
		+  "If 'damage location' be not set, it shall attempt to procure the location of the 'causing entity', "
		+ "otherwise, null."
})
@Example("""
	on death:
		set {_location} to the source location of event-damage source
	""")
@Since("2.12")
public class ExprSourceLocation extends SimplePropertyExpression<DamageSource, Location> {

	public static void register(SyntaxRegistry registry) {
		registry.register(
			SyntaxRegistry.EXPRESSION,
			infoBuilder(
				ExprSourceLocation.class,
				Location.class,
				"source location",
				"damagesources",
				true
			)
				.supplier(ExprSourceLocation::new)
				.build()
		);
	}

	@Override
	public @Nullable Location convert(DamageSource damageSource) {
		return damageSource.getSourceLocation();
	}

	@Override
	public Class<Location> getReturnType() {
		return Location.class;
	}

	@Override
	protected String getPropertyName() {
		return "source location";
	}

}
