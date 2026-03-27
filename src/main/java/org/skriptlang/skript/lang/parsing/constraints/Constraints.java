package org.skriptlang.skript.lang.parsing.constraints;

import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxElement;
import ch.njol.util.Kleenean;
import com.google.common.collect.Iterators;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.parsing.ParsingContext;
import org.skriptlang.skript.registration.SyntaxInfo;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * A collection of constraints to apply during parsing.
 * These constraints should be sourced from a single parsing site and includes both
 * permanent and temporary constraints.
 */
public class Constraints implements Iterable<Constraint>{

	final ArrayList<Constraint> permanentConstraints = new ArrayList<>();
	final ArrayList<Constraint> temporaryConstraints = new ArrayList<>();

	private @Nullable Integer parseFlagsCache;

	/**
	 * Package-private factory that creates a Constraints backed by pre-built lists.
	 * Used by {@link ConstraintStack#asConstraints()} to avoid repeated stream allocation.
	 */
	static Constraints fromLists(List<Constraint> temporary, List<Constraint> permanent) {
		Constraints c = new Constraints();
		c.permanentConstraints.addAll(permanent);
		c.temporaryConstraints.addAll(temporary);
		return c;
	}

	public static Constraints of(Constraint... constraints) {
		Constraints cons = new Constraints();
		for (Constraint constraint : constraints) {
			if (constraint.lifetime() == Constraint.Lifetime.PERMANENT) {
				cons.permanentConstraints.add(constraint);
			} else {
				cons.temporaryConstraints.add(constraint);
			}
		}
		return cons;
	}

	public Constraints combine(Constraints other) {
		return new Constraints() {
			@Override
			public Iterable<Constraint> permanentConstraints() {
				// concat two iterables
				return () -> Iterators.concat(
						Constraints.this.permanentConstraints().iterator(),
						other.permanentConstraints().iterator()
				);
			}

			@Override
			public Iterable<Constraint> temporaryConstraints() {
				return () -> Iterators.concat(
						Constraints.this.temporaryConstraints().iterator(),
						other.temporaryConstraints().iterator()
				);
			}
		};
	}

	public Iterable<Constraint> permanentConstraints() {
		return permanentConstraints;
	}

	public Iterable<Constraint> temporaryConstraints() {
		return temporaryConstraints;
	}

	public <T extends Constraint> Iterable<T> constraintsOfType(Class<T> type) {
		return () -> Iterators.filter(this.iterator(), type);
	}

	/**
	 * Limits parsing to infos which pass this test. This will be called prior to any attempts to parse against the info.
	 * <br>
	 * May print errors.
	 * @param info The info to check.
	 * @param context The current parsing context.
	 * @return Whether this info could feasibly fit this site.
	 * @see Constraint#acceptsInfo(SyntaxInfo, ParsingContext)
	 */
	public boolean acceptsInfo(SyntaxInfo<?> info, ParsingContext context) {
		for (Constraint constraint : this) {
			if (!constraint.acceptsInfo(info, context))
				return false;
		}
		return true;
	}

	/**
	 * Limits parsing to elements which pass this test. Elements are checked against this method immediately after
	 * construction, prior to {@link SyntaxElement#init(Expression[], int, Kleenean, ParseResult)}.
	 * <br>
	 * Note that simplification has not yet taken place.
	 * <br>
	 * May print errors.
	 *
	 * @param info The info used to create the element.
	 * @param element The parsed but un-initialized element to check.
	 * @param parseResult The parse result from parsing this element.
	 * @param context The current parsing context.
	 * @return Whether the element/info fits this site.
	 * @see Constraint#acceptsPreInit(SyntaxInfo, SyntaxElement, ParseResult, ParsingContext)
	 */
	public boolean acceptsPreInit(SyntaxInfo<?> info, SyntaxElement element, ParseResult parseResult, ParsingContext context) {
		for (Constraint constraint : this) {
			if (!constraint.acceptsPreInit(info, element, parseResult, context))
				return false;
		}
		return true;
	}

	/**
	 * Limits parsing to elements which pass this test. If an element successfully passes init(), it will then be
	 * checked against this method. The element will be fully initialized and simplified.
	 * and continue parsing.
	 * <br>
	 * May print errors.
	 * @param element The parsed and initialized element to check.
	 * @param parseResult The parse result from parsing this element.
	 * @param context The current parsing context.
	 * @return Whether the element fits this site.
	 * @see Constraint#acceptsPostInit(SyntaxElement, ParseResult, ParsingContext)
	 */
	public boolean acceptsPostInit(SyntaxElement element, ParseResult parseResult, ParsingContext context) {
		for (Constraint constraint : this) {
			if (!constraint.acceptsPostInit(element, parseResult, context))
				return false;
		}
		return true;
	}

	@Override
	public @NotNull Iterator<Constraint> iterator() {
		if (temporaryConstraints.isEmpty()) return permanentConstraints.iterator();
		if (permanentConstraints.isEmpty()) return temporaryConstraints.iterator();
		return Iterators.concat(temporaryConstraints.iterator(), permanentConstraints.iterator());
	}

	public int asParseFlags() {
		if (parseFlagsCache != null) return parseFlagsCache;
		int flags = 0;
		for (Constraint constraint : this) {
			if (constraint instanceof LiteralConstraint literalConstraint) {
				flags |= literalConstraint.asParseFlags();
			}
		}
		return parseFlagsCache = flags;
	}
}
