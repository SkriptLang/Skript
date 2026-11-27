package org.skriptlang.skript.bukkit.raytrace.elements.expressions;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.EventRestrictedSyntax;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.raytrace.RaytraceSectionEvent;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Raytrace - Max Distance")
@Description("""
	The maximum distance, in blocks, the raytrace this section belongs to may travel.
	This starts out as the distance the raytrace was given, being the distance from its header, \
	or the distance to its end location when casting between two locations.
	Setting it overrides that distance, and it must be positive.
	Can only be used within a raytrace section.
	""")
@Example("""
	set {_hit} to the results of a raytrace from {_start} along {_direction} for 20 meters:
		if {_short sighted} is true:
			set the max distance to 5
	""")
@Since("INSERT VERSION")
public class ExprRaytraceMaxDistance extends SimpleExpression<Number> implements EventRestrictedSyntax {

	public static void register(SyntaxRegistry registry) {
		registry.register(
			SyntaxRegistry.EXPRESSION,
			SyntaxInfo.Expression.builder(ExprRaytraceMaxDistance.class, Number.class)
				.supplier(ExprRaytraceMaxDistance::new)
				.priority(SyntaxInfo.SIMPLE)
				.addPattern("[the] max[imum] [ray] distance")
				.build()
		);
	}

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		return true;
	}

	@Override
	public Class<? extends Event>[] supportedEvents() {
		return CollectionUtils.array(RaytraceSectionEvent.class);
	}

	@Override
	protected Number @Nullable [] get(Event event) {
		if (!(event instanceof RaytraceSectionEvent sectionEvent))
			return null;
		return new Number[]{sectionEvent.getConfig().maxDistance};
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		if (mode == ChangeMode.SET)
			return CollectionUtils.array(Number.class);
		return null;
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		if (!(event instanceof RaytraceSectionEvent sectionEvent) || delta == null)
			return;
		double maxDistance = ((Number) delta[0]).doubleValue();
		if (maxDistance <= 0) {
			error("A raytrace must travel a positive distance, but was given " + maxDistance + ".");
			return;
		}
		sectionEvent.getConfig().maxDistance = maxDistance;
	}

	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public Class<? extends Number> getReturnType() {
		return Number.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "the max distance";
	}

}
