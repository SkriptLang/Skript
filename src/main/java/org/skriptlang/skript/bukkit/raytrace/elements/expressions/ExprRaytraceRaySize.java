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

@Name("Raytrace - Ray Size")
@Description("""
	The size of the ray used by the raytrace this section belongs to.
	The size is the radius, in blocks, that the ray is expanded by when checking entities.
	A size of 0, the default, is a ray with no thickness.
	Can only be used within a raytrace section.
	""")
@Example("""
	set {_hit} to the results of a raytrace from {_start} along {_direction}:
		set the ray size to 0.5
	""")
@Since("INSERT VERSION")
public class ExprRaytraceRaySize extends SimpleExpression<Number> implements EventRestrictedSyntax {

	public static void register(SyntaxRegistry registry) {
		registry.register(
			SyntaxRegistry.EXPRESSION,
			SyntaxInfo.Expression.builder(ExprRaytraceRaySize.class, Number.class)
				.supplier(ExprRaytraceRaySize::new)
				.priority(SyntaxInfo.SIMPLE)
				.addPattern("[the] ray size")
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
		return new Number[]{sectionEvent.getConfig().raySize};
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
		double raySize = ((Number) delta[0]).doubleValue();
		if (raySize < 0) {
			error("The size of a ray cannot be negative, but was " + raySize + ".");
			return;
		}
		sectionEvent.getConfig().raySize = raySize;
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
		return "the ray size";
	}

}
