package ch.njol.skript.expressions.arithmetic;

import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.Variable;
import ch.njol.skript.lang.util.ContextlessEvent;
import ch.njol.skript.test.runner.SkriptJUnitTest;
import org.junit.Assert;
import org.junit.Test;
import ch.njol.skript.lang.simplification.SimplifiedLiteral;

/**
 * Check if arithmetic expressions are simplified correctly.
 */
public class ArithmeticSimplificationTest extends SkriptJUnitTest {

	@Test
	public void test() {
		//noinspection unchecked
		var arithmetic = new SkriptParser("5 * 2 - 3 + 4").parseExpression(Number.class);
		Assert.assertTrue(arithmetic instanceof SimplifiedLiteral<? extends Number>);
		Assert.assertEquals(5 * 2 - 3 + 4, ((SimplifiedLiteral<? extends Number>) arithmetic).getSingle().intValue());

		//noinspection unchecked
		arithmetic = new SkriptParser("5 + 4").parseExpression(Number.class);
		Assert.assertTrue(arithmetic instanceof SimplifiedLiteral<? extends Number>);
		Assert.assertEquals(5 + 4, ((SimplifiedLiteral<? extends Number>) arithmetic).getSingle().intValue());

		//noinspection unchecked
		arithmetic = new SkriptParser("5 - 10 + {_loc}").parseExpression(Number.class);
		Assert.assertTrue(arithmetic instanceof ExprArithmetic<?,?,?>);
		var leftArith = ((ExprArithmetic<?,?,?>) arithmetic).getFirst();
		var rightArith = ((ExprArithmetic<?,?,?>) arithmetic).getSecond();
		Assert.assertTrue(leftArith instanceof SimplifiedLiteral<?>);
		Assert.assertTrue(rightArith instanceof Variable<?>);
		//noinspection unchecked,DataFlowIssue
		Assert.assertEquals(5 - 10, ((ExprArithmetic<?,?,Number>) arithmetic).getSingle(ContextlessEvent.get()).intValue());
	}

}
