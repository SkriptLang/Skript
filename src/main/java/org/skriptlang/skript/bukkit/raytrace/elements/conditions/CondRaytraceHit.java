package org.skriptlang.skript.bukkit.raytrace.elements.conditions;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.bukkit.util.RayTraceResult;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Raytrace Result - Hit Anything")
@Description("""
	Checks what a raytrace hit, if anything.
	A raytrace can only ever hit one thing, so it never hits both a block and an entity.
	""")
@Example("""
	set {_hit} to the results of a raytrace from player for 20 meters
	if {_hit} hit an entity:
		send "You are looking at %the hit entity of {_hit}%"
	else if {_hit} didn't hit anything:
		send "You are looking at nothing at all"
	""")
@Since("INSERT VERSION")
public class CondRaytraceHit extends Condition {

	private enum HitTarget {
		BLOCK,
		ENTITY,
		ANYTHING
	}

	public static void register(SyntaxRegistry registry) {
		registry.register(
			SyntaxRegistry.CONDITION,
			SyntaxInfo.builder(CondRaytraceHit.class)
				.addPatterns(
					"%raytraceresults% hit [a[n]] (:block|:entity|anything:(any|some)thing)",
					"%raytraceresults% (didn't|did not|doesn't|does not) hit [a[n]] (:block|:entity|anything:(any|some)thing)"
				)
				.supplier(CondRaytraceHit::new)
				.build()
		);
	}

	private Expression<RayTraceResult> results;
	private HitTarget target;

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		//noinspection unchecked
		results = (Expression<RayTraceResult>) expressions[0];
		if (parseResult.hasTag("block")) {
			target = HitTarget.BLOCK;
		} else if (parseResult.hasTag("entity")) {
			target = HitTarget.ENTITY;
		} else {
			target = HitTarget.ANYTHING;
		}
		setNegated(matchedPattern == 1);
		return true;
	}

	@Override
	public boolean check(Event event) {
		return results.check(event, result -> switch (target) {
			case BLOCK -> result.getHitBlock() != null;
			case ENTITY -> result.getHitEntity() != null;
			case ANYTHING -> result.getHitBlock() != null || result.getHitEntity() != null;
		}, isNegated());
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		SyntaxStringBuilder builder = new SyntaxStringBuilder(event, debug);
		builder.append(results);
		builder.append(isNegated() ? "didn't hit" : "hit");
		builder.append(switch (target) {
			case BLOCK -> "a block";
			case ENTITY -> "an entity";
			case ANYTHING -> "anything";
		});
		return builder.toString();
	}

}
