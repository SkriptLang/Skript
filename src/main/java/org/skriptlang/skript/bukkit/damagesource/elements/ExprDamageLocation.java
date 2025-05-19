package org.skriptlang.skript.bukkit.damagesource.elements;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.*;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.Location;
import org.bukkit.damage.DamageSource;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.damagesource.DamageSourceExperiment;
import org.skriptlang.skript.bukkit.damagesource.DamageSourceWrapper;
import org.skriptlang.skript.bukkit.damagesource.elements.ExprSecDamageSource.DamageSourceSectionEvent;

@Name("Damage Source - Damage Location")
@Description({
	"The location where the damage was originated from.",
	"The 'damage location' on vanilla damage sources will be set if an entity did not cause the damage.",
	"Cannot change any attributes of a damage source outside the 'custom damage source' section."
})
@Example("""
	set {_source} to a new custom damage source:
		set the damage type to magic
		set the causing entity to {_player}
		set the direct entity to {_arrow}
		set the damage location to location(0, 0, 10)
	damage all players by 5 using {_source}
	""")
@Example("""
	on death:
		set {_location} to the damage location of event-damage source
	""")
@Since("INSERT VERSION")
@RequiredPlugins("Minecraft 1.20.4+")
public class ExprDamageLocation extends SimplePropertyExpression<DamageSource, Location> implements DamageSourceExperiment {

	static {
		registerDefault(ExprDamageLocation.class, Location.class, "damage location", "damagesources");
	}

	private boolean isEvent;

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		isEvent = getParser().isCurrentEvent(DamageSourceSectionEvent.class);
		return super.init(expressions, matchedPattern, isDelayed, parseResult);
	}

	@Override
	public @Nullable Location convert(DamageSource damageSource) {
		return damageSource.getDamageLocation();
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		if (!isEvent) {
			Skript.error("You cannot change the attributes of a damage source outside a 'custom damage source' section.");
		} else if (mode == ChangeMode.SET || mode == ChangeMode.DELETE) {
			return CollectionUtils.array(Location.class);
		}
		return null;
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		if (!(event instanceof DamageSourceSectionEvent sectionEvent))
			return;

		Location location = delta == null ? null : (Location) delta[0];
		DamageSourceWrapper wrapper = (DamageSourceWrapper) sectionEvent.getDamageSource();
		wrapper.setDamageLocation(location);

		if (!getExpr().stream(event).filter(source -> !source.equals(wrapper)).toList().isEmpty())
			error("You can only change the attributes of the damage source from this section.");
	}

	@Override
	public Class<Location> getReturnType() {
		return Location.class;
	}

	@Override
	protected String getPropertyName() {
		return "damage location";
	}

}
