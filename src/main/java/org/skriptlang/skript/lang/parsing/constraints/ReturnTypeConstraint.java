package org.skriptlang.skript.lang.parsing.constraints;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionInfo;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxElement;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.ClassInfoReference;
import org.skriptlang.skript.lang.parsing.ParsingContext;
import org.skriptlang.skript.registration.SyntaxInfo;

import java.util.ArrayList;
import java.util.List;

public class ReturnTypeConstraint implements Constraint {

	private final ReturnTypes returnTypes;

	public ReturnTypeConstraint(ReturnTypes returnTypes) {
		this.returnTypes = returnTypes;
	}

	@Override
	public boolean acceptsInfo(SyntaxInfo<?> info, ParsingContext context) {
		// we can only check expressions
		if (!(info instanceof ExpressionInfo<?, ?> exprInfo))
			return true;
		Class<?> returnType = exprInfo.returnType;
		// valid if any of the return types are superclasses of the expression's return type
		return returnTypes.isValidReturnType(returnType);
	}

	@Override
	public boolean acceptsPostInit(SyntaxElement element, ParseResult parseResult, ParsingContext context) {
		if (!(element instanceof Expression<?> expression))
			return true;
		// we need to check plurality of the actual return types
		Class<?>[] possibleReturnTypes = expression.possibleReturnTypes();
		boolean returnsPlural = !expression.isSingle();

		// for each possible return type, check if it
		// a) is a valid return type
		// b) matches the plurality requirement
		List<ClassInfoReference> invalidPluralityRefs = new ArrayList<>();
		for (Class<?> returnType : possibleReturnTypes) {
			if (returnTypes.isValidReturnType(returnType)) {
				// check plurality
				for (ClassInfoReference ref : returnTypes) {
					boolean acceptsPlural = !ref.isPlural().isFalse();
					if (returnsPlural && !acceptsPlural) {
						// expression returns plural, but return type does not accept plural
						// add to list for error reporting
						invalidPluralityRefs.add(ref);
					}
				}
			}
		}
		// if there are any invalid plurality refs, we reject
		if (!invalidPluralityRefs.isEmpty()) {
			Skript.error("'" + element + "' can only return a single "
				+ Classes.toString(invalidPluralityRefs.stream().map(classInfoRef -> classInfoRef.getClassInfo().getName().toString()).toArray(), false)
				+ ", not more.");
			return false;
		}
		return true;
	}

	@Override
	public Lifetime lifetime() {
		return Lifetime.TEMPORARY; // only applies for this parse.
	}
}
