package ch.njol.skript.test.runner;

import ch.njol.skript.localization.Noun;
import org.skriptlang.skript.lang.arithmetic.Arithmetics;
import org.skriptlang.skript.lang.arithmetic.Operator;
import org.skriptlang.skript.util.Priority;

public final class CustomTestOperators {

	static {
		if (TestMode.ENABLED && !TestMode.GEN_DOCS) {
			Arithmetics.registerOperation(
				new Operator("bar!", Priority.base(), (Noun) null),
				String.class,
				(s1, s2) -> s1.concat(" bar ").concat(s2)
			);
			Arithmetics.registerOperation(
				new Operator("foo!1", Priority.after(Operator.ADDITION_SUBTRACTION_PRIORITY), (Noun) null),
				Number.class,
				(n1, n2) -> n1.intValue() + 2 * n2.intValue()
			);
			Arithmetics.registerOperation(
				new Operator("foo!2", Priority.before(Operator.MULTIPLICATION_DIVISION_PRIORITY), (Noun) null),
				Number.class,
				(n1, n2) -> n1.intValue() + 2 * n2.intValue()
			);
			Arithmetics.registerOperation(
				new Operator("blob!", Priority.base(), (Noun) null),
				String.class,
				Number.class,
				String.class,
				(s, n) -> s.concat(String.valueOf(n.intValue() + 1))
			);
		}
	}

	private CustomTestOperators() {
		throw new UnsupportedOperationException();
	}

}
