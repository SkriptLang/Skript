package org.skriptlang.skript.bukkit.damagesource.elements;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.Location;
import org.bukkit.damage.DamageSource;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.damagesource.DamageSourceExperiment;
import org.skriptlang.skript.bukkit.damagesource.DamageSourceWrapper;

//@Name("Damage Source - Source Location")
//@Description({
//	"The final location where the damage was originated from.",
//	"The 'source location' for vanilla damage sources will retrieve the 'damage location' if set. "
//		+  "If 'damage location' is not set, will attempt to grab the location of the 'causing entity', "
//		+ "otherwise, null.",
//	"Cannot change any attributes of the damage source from an 'on damage' or 'on death' event."
//})
//@Example("""
//	set {_source} to a new custom damage source:
//		set the damage type to magic
//		set the causing entity to {_player}
//		set the direct entity to {_arrow}
//		set the damage location to location(0, 0, 10)
//		set the source location to location(10, 0, 0)
//		set the food exhaustion to 10
//		make event-damage source be indirect
//		make event-damage source scale with difficulty
//	damage all players by 5 using {_source}
//	""")
//@Since("INSERT VERSION")
//@RequiredPlugins("Minecraft 1.20.4+")
public class ExprSourceLocation extends SimplePropertyExpression<DamageSource, Location> implements DamageSourceExperiment {

//	static {
//		registerDefault(ExprSourceLocation.class, Location.class, "source location", "damagesources");
//	}

	@Override
	public @Nullable Location convert(DamageSource damageSource) {
		return damageSource.getSourceLocation();
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		if (mode == ChangeMode.SET || mode == ChangeMode.DELETE)
			return CollectionUtils.array(Location.class);
		return null;
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		Location location = delta == null ? null : (Location) delta[0];

		boolean hasFinal = false;
		for (DamageSource damageSource : getExpr().getArray(event)) {
			if (!(damageSource instanceof DamageSourceWrapper wrapper)) {
				hasFinal = true;
				continue;
			}
			wrapper.setSourceLocation(location);
		}
		if (hasFinal)
			error("You cannot change the 'source location' attribute of a finalized damage source.");
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
