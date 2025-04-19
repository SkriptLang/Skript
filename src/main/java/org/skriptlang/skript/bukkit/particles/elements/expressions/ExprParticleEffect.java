package org.skriptlang.skript.bukkit.particles.elements.expressions;

import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.particles.ParticleEffect;

public class ExprParticleEffect extends SimpleExpression<ParticleEffect> {
	@Override
	protected ParticleEffect @Nullable [] get(Event event) {
		return new ParticleEffect[0];
	}

	@Override
	public boolean isSingle() {
		return false;
	}

	@Override
	public Class<? extends ParticleEffect> getReturnType() {
		return null;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "";
	}

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
		return false;
	}
	// TODO:
	//  Syntax:
	//  count + (name + "particle" + data) + offset + extra
	//    # count:
	//		  %number% [of]
	//    # offset:
	//        [with [an]] offset (of|by) ((%number%, %number%(,|[,] and) %number%)|%vector%)
	//    # extra:
	//        [(at|with) [a]] (speed|extra [value]) [of] %number%
	//  This expression should handle the common elements between all particles
	//  Specific data should be handled by something more dynamic, since data can vary wildly.
	//  Consider VisualEffect approach, or SkBee approach of various functions.
	//  Prefer something like VisualEffect for better readability + grammar, but needs to be
	//  better documented this time.
}
