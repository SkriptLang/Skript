package org.skriptlang.skript.bukkit.damagesource.elements.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.Location;
import org.bukkit.damage.DamageSource;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.damagesource.elements.expressions.ExprSecDamageSource.DamageSourceSectionEvent;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Damage Source - Damage Location")
@Description({
	"The location where the damage was originated from.",
	"The 'damage location' on vanilla damage sources will be set if an entity did not cause the damage.",
	"Attributes of a damage source cannot be changed once created, only while within the 'custom damage source' section."
})
@Example("""
	damage all players by 5 using a custom damage source:
		set the damage type to magic
		set the causing entity to {_player}
		set the direct entity to {_arrow}
		set the damage location to location(0, 0, 10)
	""")
@Example("""
	on death:
		set {_location} to the damage location of event-damage source
	""")
@Since("2.12")
public class ExprDamageLocation extends SimplePropertyExpression<DamageSource, Location> {

	public static void register(SyntaxRegistry registry) {
		registry.register(
			SyntaxRegistry.EXPRESSION,
			infoBuilder(
				ExprDamageLocation.class,
				Location.class,
				"damage location",
				"damagesources",
				true
			)
				.supplier(ExprDamageLocation::new)
				.build()
		);
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
		} else if (!getExpr().isSingle() || !getExpr().isDefault()) {
			Skript.error("You can only change the attributes of the damage source being created in this section.");
		} else if (mode == ChangeMode.SET || mode == ChangeMode.DELETE) {
			return CollectionUtils.array(Location.class);
		}
		return null;
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		if (!(event instanceof DamageSourceSectionEvent sectionEvent))
			return;

		sectionEvent.damageLocation = delta == null ? null : (Location) delta[0];
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
