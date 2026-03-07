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

@Name("Damage Source - Source Location")
@Description({
	"The final location where the damage was originated from.",
	"The 'source location' for vanilla damage sources will retrieve the 'damage location' if set. "
		+  "If 'damage location' is not set, will attempt to grab the location of the 'causing entity', "
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
