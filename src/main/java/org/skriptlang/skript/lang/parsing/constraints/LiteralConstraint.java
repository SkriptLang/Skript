package org.skriptlang.skript.lang.parsing.constraints;

import ch.njol.skript.lang.*;
import org.skriptlang.skript.lang.parsing.ParsingContext;
import org.skriptlang.skript.registration.SyntaxInfo;

import static ch.njol.skript.lang.SkriptParser.PARSE_EXPRESSIONS;
import static ch.njol.skript.lang.SkriptParser.PARSE_LITERALS;

public class LiteralConstraint implements Constraint {

	public enum Allows {
		ONLY_LITERALS,
		NO_LITERALS,
		ANY
	}

	public static LiteralConstraint fromFlags(int flags) {
		return new LiteralConstraint(
			(flags & PARSE_LITERALS) != 0,
			(flags & PARSE_EXPRESSIONS) != 0,
			true
		);
	}

	public static boolean allowsLiterals(Constraints constraints) {
		for (LiteralConstraint constraint : constraints.constraintsOfType(LiteralConstraint.class)) {
			if (constraint.allows == Allows.NO_LITERALS) {
				return false;
			}
		}
		return true;
	}

	public static boolean allowsNonLiterals(Constraints constraints) {
		for (LiteralConstraint constraint : constraints.constraintsOfType(LiteralConstraint.class)) {
			if (constraint.allows == Allows.ONLY_LITERALS) {
				return false;
			}
		}
		return true;
	}

	private final Allows allows;
	private final boolean allowSimplifiedLiterals;

	public LiteralConstraint(Allows allows, boolean allowSimplifiedLiterals) {
		this.allows = allows;
		this.allowSimplifiedLiterals = allowSimplifiedLiterals;
	}

	public LiteralConstraint(boolean allowsLiterals, boolean allowsNonLiterals, boolean allowSimplifiedLiterals) {
		if (allowsLiterals && !allowsNonLiterals) {
			this.allows = Allows.ONLY_LITERALS;
		} else if (!allowsLiterals && allowsNonLiterals) {
			this.allows = Allows.NO_LITERALS;
		} else if (allowsLiterals) {
			this.allows = Allows.ANY;
		} else {
			throw new IllegalArgumentException("At least one of allowsLiterals or allowsNonLiterals must be true");
		}
		this.allowSimplifiedLiterals = allowSimplifiedLiterals;
	}

	public int asParseFlags() {
		int flags = 0;
		if (this.allows == Allows.NO_LITERALS || this.allows == Allows.ANY) {
			flags |= PARSE_EXPRESSIONS;
		}
		if (this.allows == Allows.ONLY_LITERALS || this.allows == Allows.ANY) {
			flags |= PARSE_LITERALS;
		}
		return flags;
	}

	@Override
	public boolean acceptsInfo(SyntaxInfo<?> info, ParsingContext context) {
		// we can only check expressions
		if (!(info instanceof ExpressionInfo<?, ?>))
			return true;
		return switch (allows) {
			// If only literals are allowed, then we can check the type class. If simplified literals are allowed,
			// then we cannot reject at this stage.
			case ONLY_LITERALS -> allowSimplifiedLiterals || Literal.class.isAssignableFrom(info.type());
			// If no literals are allowed, we can check the type class.
			case NO_LITERALS -> !Literal.class.isAssignableFrom(info.type());
			// If any is allowed, we accept everything.
			case ANY -> true;
		};
	}

	@Override
	public boolean acceptsPostInit(SyntaxElement element, SkriptParser.ParseResult parseResult, ParsingContext context) {
		// if we don't allow simplified literals, then this stage is irrelevant
		if (!allowSimplifiedLiterals)
			return true;
		// non-expressions are allowed since we don't care about them here
		if (!(element instanceof Expression<?>))
			return true;
		return switch (allows) {
			case ONLY_LITERALS -> element instanceof Literal<?>;
			case NO_LITERALS -> !(element instanceof Literal<?>);
			case ANY -> true;
		};
	}

	@Override
	public Lifetime lifetime() {
		return Lifetime.PERMANENT; // Literals cannot be constructed from non-literals, so this is permanent.
	}
}
