package org.skriptlang.skript.bukkit.damagesource.elements;

import ch.njol.skript.doc.*;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import org.bukkit.Location;
import org.bukkit.damage.DamageSource;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.damagesource.DamageSourceExperimentSyntax;

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
@RequiredPlugins("Minecraft 1.20.4+")
@SuppressWarnings("UnstableApiUsage")
public class ExprSourceLocation extends SimplePropertyExpression<DamageSource, Location> implements DamageSourceExperimentSyntax {

	static {
		registerDefault(ExprSourceLocation.class, Location.class, "source location", "damagesources");
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
