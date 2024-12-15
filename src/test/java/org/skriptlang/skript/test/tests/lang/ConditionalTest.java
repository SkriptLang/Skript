package org.skriptlang.skript.test.tests.lang;

import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.skriptlang.skript.lang.condition.Conditional;

public class ConditionalTest {

	TestConditional condTrue;
	TestConditional condFalse;
	TestConditional condUnknown;
	TestContext context;

	@Before
	public void setup() {
		condTrue = new TestConditional(Kleenean.TRUE);
		condFalse = new TestConditional(Kleenean.FALSE);
		condUnknown = new TestConditional(Kleenean.UNKNOWN);
		context = new TestContext();
	}

	@Test
	public void testBasicConditionals() {
		Assert.assertEquals(Kleenean.TRUE, condTrue.evaluate(context));
		Assert.assertEquals(1, condTrue.timesEvaluated);
		condTrue.reset();

		Assert.assertEquals(Kleenean.FALSE, condFalse.evaluate(context));
		Assert.assertEquals(1, condFalse.timesEvaluated);
		condFalse.reset();

		Assert.assertEquals(Kleenean.UNKNOWN, condUnknown.evaluate(context));
		Assert.assertEquals(1, condUnknown.timesEvaluated);
		condUnknown.reset();
	}

	private void assertBasic(TestConditional conditional, Kleenean expected, Kleenean actual, int expectedEvals) {
		Assert.assertEquals("Incorrect evaluation!", expected, actual);
		assertEvals(conditional, expectedEvals);
	}

	private void assertEvals(TestConditional conditional, int expectedEvals) {
		Assert.assertEquals("Wrong number of evaluations!", expectedEvals, conditional.timesEvaluated);
		conditional.reset();
	}

	@Test
	public void testBasicAndKnown() {
		// true AND known x
		assertBasic(condTrue, Kleenean.TRUE, condTrue.evaluateAnd(Kleenean.TRUE, context), 1);
		assertBasic(condTrue, Kleenean.FALSE, condTrue.evaluateAnd(Kleenean.FALSE, context), 0);
		assertBasic(condTrue, Kleenean.UNKNOWN, condTrue.evaluateAnd(Kleenean.UNKNOWN, context), 1);

		// false AND known x
		assertBasic(condFalse, Kleenean.FALSE, condFalse.evaluateAnd(Kleenean.TRUE, context), 1);
		assertBasic(condFalse, Kleenean.FALSE, condFalse.evaluateAnd(Kleenean.FALSE, context), 0);
		assertBasic(condFalse, Kleenean.FALSE, condFalse.evaluateAnd(Kleenean.UNKNOWN, context), 1);

		// unknown AND known x
		assertBasic(condUnknown, Kleenean.UNKNOWN, condUnknown.evaluateAnd(Kleenean.TRUE, context), 1);
		assertBasic(condUnknown, Kleenean.FALSE, condUnknown.evaluateAnd(Kleenean.FALSE, context), 0);
		assertBasic(condUnknown, Kleenean.UNKNOWN, condUnknown.evaluateAnd(Kleenean.UNKNOWN, context), 1);
	}

	@Test
	public void testBasicOrKnown() {
		// true OR known x
		assertBasic(condTrue, Kleenean.TRUE, condTrue.evaluateOr(Kleenean.TRUE, context), 0);
		assertBasic(condTrue, Kleenean.TRUE, condTrue.evaluateOr(Kleenean.FALSE, context), 1);
		assertBasic(condTrue, Kleenean.TRUE, condTrue.evaluateOr(Kleenean.UNKNOWN, context), 1);

		// false OR known x
		assertBasic(condFalse, Kleenean.TRUE, condFalse.evaluateOr(Kleenean.TRUE, context), 0);
		assertBasic(condFalse, Kleenean.FALSE, condFalse.evaluateOr(Kleenean.FALSE, context), 1);
		assertBasic(condFalse, Kleenean.UNKNOWN, condFalse.evaluateOr(Kleenean.UNKNOWN, context), 1);

		// unknown OR known x
		assertBasic(condUnknown, Kleenean.TRUE, condUnknown.evaluateOr(Kleenean.TRUE, context), 0);
		assertBasic(condUnknown, Kleenean.UNKNOWN, condUnknown.evaluateOr(Kleenean.FALSE, context), 1);
		assertBasic(condUnknown, Kleenean.UNKNOWN, condUnknown.evaluateOr(Kleenean.UNKNOWN, context), 1);
	}

	@Test
	public void testBasicNot() {
		assertBasic(condTrue, Kleenean.FALSE, condTrue.evaluateNot(context), 1);
		assertBasic(condFalse, Kleenean.TRUE, condFalse.evaluateNot(context), 1);
		assertBasic(condUnknown, Kleenean.UNKNOWN, condUnknown.evaluateNot(context), 1);
	}

	@Test
	public void testBasicAndBasic() {

		TestConditional condTrueB = new TestConditional(Kleenean.TRUE);
		TestConditional condFalseB = new TestConditional(Kleenean.FALSE);
		TestConditional condUnknownB = new TestConditional(Kleenean.UNKNOWN);

		// true AND x
		Assert.assertEquals(Kleenean.TRUE, condTrue.evaluateAnd(condTrueB, context));
		assertEvals(condTrue, 1);
		assertEvals(condTrueB, 1);

		Assert.assertEquals(Kleenean.FALSE, condTrue.evaluateAnd(condFalse, context));
		assertEvals(condTrue, 0);
		assertEvals(condFalse, 1);

		Assert.assertEquals(Kleenean.UNKNOWN, condTrue.evaluateAnd(condUnknown, context));
		assertEvals(condTrue, 1);
		assertEvals(condUnknown, 1);

		// false AND x
		Assert.assertEquals(Kleenean.FALSE, condFalse.evaluateAnd(condFalseB, context));
		assertEvals(condFalse, 0);
		assertEvals(condFalseB, 1);

		Assert.assertEquals(Kleenean.FALSE, condFalse.evaluateAnd(condTrue, context));
		assertEvals(condFalse, 1);
		assertEvals(condTrue, 1);

		Assert.assertEquals(Kleenean.FALSE, condFalse.evaluateAnd(condUnknown, context));
		assertEvals(condFalse, 1);
		assertEvals(condUnknown, 1);

		// unknown AND x
		Assert.assertEquals(Kleenean.UNKNOWN, condUnknown.evaluateAnd(condUnknownB, context));
		assertEvals(condUnknown, 1);
		assertEvals(condUnknownB, 1);

		Assert.assertEquals(Kleenean.UNKNOWN, condUnknown.evaluateAnd(condTrue, context));
		assertEvals(condUnknown, 1);
		assertEvals(condTrue, 1);

		Assert.assertEquals(Kleenean.FALSE, condUnknown.evaluateAnd(condFalse, context));
		assertEvals(condUnknown, 0);
		assertEvals(condFalse, 1);
	}

	@Test
	public void testCombinedAnd() {

		TestConditional condTrueB = new TestConditional(Kleenean.TRUE);
		TestConditional condFalseB = new TestConditional(Kleenean.FALSE);
		TestConditional condUnknownB = new TestConditional(Kleenean.UNKNOWN);

		Conditional<TestContext> trueAndTrue = Conditional.builderDNF(TestContext.class)
			.and(condTrue, condTrueB)
			.build();

		Assert.assertEquals(Kleenean.TRUE, trueAndTrue.evaluate(context));
		assertEvals(condTrue, 1);
		assertEvals(condTrueB, 1);


		Conditional<TestContext> trueAndFalse = Conditional.builderDNF(TestContext.class)
			.and(condTrue, condFalse)
			.build();

		Assert.assertEquals(Kleenean.FALSE, trueAndFalse.evaluate(context));
		assertEvals(condTrue, 1);
		assertEvals(condFalse, 1);


		Conditional<TestContext> falseAndTrueAndFalse = Conditional.builderDNF(TestContext.class)
			.and(condFalse, condTrue, condFalse)
			.build();

		Assert.assertEquals(Kleenean.FALSE, falseAndTrueAndFalse.evaluate(context));
		assertEvals(condTrue, 0);
		assertEvals(condFalse, 1);


		Conditional<TestContext> trueAndUnknown = Conditional.builderDNF(TestContext.class)
			.and(condTrue, condUnknown)
			.build();

		Assert.assertEquals(Kleenean.UNKNOWN, trueAndUnknown.evaluate(context));
		assertEvals(condTrue, 1);
		assertEvals(condUnknown, 1);


		Conditional<TestContext> falseAndFalse = Conditional.builderDNF(TestContext.class)
			.and(condFalse, condFalseB)
			.build();

		Assert.assertEquals(Kleenean.FALSE, falseAndFalse.evaluate(context));
		assertEvals(condFalseB, 0);
		assertEvals(condFalse, 1);

		Conditional<TestContext> falseAndUnknown = Conditional.builderDNF(TestContext.class)
			.and(condFalse, condUnknown)
			.build();

		Assert.assertEquals(Kleenean.FALSE, falseAndUnknown.evaluate(context));
		assertEvals(condUnknown, 0);
		assertEvals(condFalse, 1);

		Conditional<TestContext> unknownAndUnknown = Conditional.builderDNF(TestContext.class)
			.and(condUnknown, condUnknownB)
			.build();

		Assert.assertEquals(Kleenean.UNKNOWN, unknownAndUnknown.evaluate(context));
		assertEvals(condUnknown, 1);
		assertEvals(condUnknownB, 1);
	}

	@Test
	public void testCombinedOr() {

		TestConditional condTrueB = new TestConditional(Kleenean.TRUE);
		TestConditional condFalseB = new TestConditional(Kleenean.FALSE);
		TestConditional condUnknownB = new TestConditional(Kleenean.UNKNOWN);

		Conditional<TestContext> trueOrTrue = Conditional.builderDNF(TestContext.class)
			.or(condTrue, condTrueB)
			.build();

		Assert.assertEquals(Kleenean.TRUE, trueOrTrue.evaluate(context));
		assertEvals(condTrue, 1);
		assertEvals(condTrueB, 0);


		Conditional<TestContext> trueOrFalse = Conditional.builderDNF(TestContext.class)
			.or(condTrue, condFalse)
			.build();

		Assert.assertEquals(Kleenean.TRUE, trueOrFalse.evaluate(context));
		assertEvals(condTrue, 1);
		assertEvals(condFalse, 0);


		Conditional<TestContext> falseOrTrueOrFalse = Conditional.builderDNF(TestContext.class)
			.or(condFalse, condTrue, condFalseB)
			.build();

		Assert.assertEquals(Kleenean.TRUE, falseOrTrueOrFalse.evaluate(context));
		assertEvals(condTrue, 1);
		assertEvals(condFalse, 1);
		assertEvals(condFalseB, 0);


		Conditional<TestContext> trueOrUnknown = Conditional.builderDNF(TestContext.class)
			.or(condTrue, condUnknown)
			.build();

		Assert.assertEquals(Kleenean.TRUE, trueOrUnknown.evaluate(context));
		assertEvals(condTrue, 1);
		assertEvals(condUnknown, 0);


		Conditional<TestContext> falseOrFalse = Conditional.builderDNF(TestContext.class)
			.or(condFalse, condFalseB)
			.build();

		Assert.assertEquals(Kleenean.FALSE, falseOrFalse.evaluate(context));
		assertEvals(condFalseB, 1);
		assertEvals(condFalse, 1);

		Conditional<TestContext> falseOrUnknown = Conditional.builderDNF(TestContext.class)
			.or(condFalse, condUnknown)
			.build();

		Assert.assertEquals(Kleenean.UNKNOWN, falseOrUnknown.evaluate(context));
		assertEvals(condUnknown, 1);
		assertEvals(condFalse, 1);

		Conditional<TestContext> unknownAndUnknown = Conditional.builderDNF(TestContext.class)
			.or(condUnknown, condUnknownB)
			.build();

		Assert.assertEquals(Kleenean.UNKNOWN, unknownAndUnknown.evaluate(context));
		assertEvals(condUnknown, 1);
		assertEvals(condUnknownB, 1);
	}

	@Test
	public void testComplexCombined() {
		Conditional<TestContext> trueAndFalseOrUnknownOrTrue = Conditional.builderDNF(TestContext.class)
			.and(condTrue, condFalse)
			.or(condUnknown, condTrue)
			.build();

		Assert.assertEquals(Kleenean.TRUE, trueAndFalseOrUnknownOrTrue.evaluate(context));
		assertEvals(condTrue, 1);
		assertEvals(condFalse, 1);
		assertEvals(condUnknown, 1);

		Conditional<TestContext> trueOrTrueAndFalseOrUnknown = Conditional.builderDNF(TestContext.class)
			.or(condTrue)
			.or(Conditional.compound(Conditional.Operator.AND, condTrue, condFalse))
			.or(condUnknown)
			.build();

		Assert.assertEquals(Kleenean.TRUE, trueOrTrueAndFalseOrUnknown.evaluate(context));
		assertEvals(condTrue, 1);
		assertEvals(condFalse, 0);
		assertEvals(condUnknown, 0);

		// should compose to (U && T && F) || (T && T && F)
		Conditional<TestContext> unknownOrTrueAndTrueAndFalse = Conditional.builderDNF(TestContext.class)
			.or(condUnknown, condTrue)
			.and(condTrue, condFalse)
			.build();

		Assert.assertEquals(Kleenean.FALSE, unknownOrTrueAndTrueAndFalse.evaluate(context));
		assertEvals(condTrue, 1);
		assertEvals(condFalse, 1);
		assertEvals(condUnknown, 1);
	}

	@Test
	public void testCombinedAndOrNot() {
		TestConditional condFalseB = new TestConditional(Kleenean.FALSE);

		Conditional<TestContext> falseOrNotTrueAndFalse = Conditional.builderDNF(condFalse)
			.orNot(Conditional.compound(Conditional.Operator.AND, condTrue, condFalseB))
			.build();

		Assert.assertEquals(Kleenean.TRUE, falseOrNotTrueAndFalse.evaluate(context));
		assertEvals(condTrue, 1);
		assertEvals(condFalse, 1);
		assertEvals(condFalseB, 1);


		Conditional<TestContext> unknownAndNotTrueOrFalseOrNotFalse = Conditional.builderDNF(TestContext.class)
			.and(condUnknown)
			.andNot(Conditional.compound(Conditional.Operator.OR, condTrue, condFalse))
			.orNot(condFalseB)
			.build();

		Assert.assertEquals(Kleenean.TRUE, unknownAndNotTrueOrFalseOrNotFalse.evaluate(context));
		assertEvals(condUnknown, 1);
		assertEvals(condTrue, 1);
		assertEvals(condFalse, 0);
		assertEvals(condFalseB, 1);
	}

	@Ignore
	private static class TestContext {
		
	}
	
	@Ignore
	private static class TestConditional implements Conditional<TestContext> {

		public int timesEvaluated;
		public final Kleenean value;

		TestConditional(Kleenean value) {
			this.value = value;
		}

		@Override
		public Kleenean evaluate(TestContext context) {
			++timesEvaluated;
			return value;
		}

		public void reset() {
			timesEvaluated = 0;
		}

		@Override
		public String toString(@Nullable Event context, boolean debug) {
			return value.toString();
		}
	}

}
