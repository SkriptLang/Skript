package org.skriptlang.skript.lang.parsing.sites;

import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.test.runner.SkriptJUnitTest;
import ch.njol.skript.util.ClassInfoReference;
import ch.njol.util.Kleenean;
import org.junit.Assert;
import org.junit.Test;
import org.skriptlang.skript.lang.parsing.constraints.Constraints;

public class ExpressionSiteTest extends SkriptJUnitTest {

	// -- Constraints --

	@Test
	public void testConstraintReturnsSameInstance() {
		ExpressionSite site = ExpressionSite.builder().build();
		Assert.assertSame(site.constraints(), site.constraints());
	}

	// -- Optional --

	@Test
	public void testOptionalDefaultIsFalse() {
		Assert.assertFalse(ExpressionSite.builder().build().isOptional());
	}

	@Test
	public void testOptionalBuilder() {
		Assert.assertTrue(ExpressionSite.builder().optional(true).build().isOptional());
		Assert.assertFalse(ExpressionSite.builder().optional(false).build().isOptional());
	}

	// -- Literal flags --

	@Test
	public void testAllowLiteralsDefaultIsTrue() {
		Assert.assertTrue(ExpressionSite.builder().build().allowsLiterals());
	}

	@Test
	public void testAllowNonLiteralsDefaultIsTrue() {
		Assert.assertTrue(ExpressionSite.builder().build().allowsNonLiterals());
	}

	@Test
	public void testAllowSimplifiedLiteralsDefaultIsTrue() {
		Assert.assertTrue(ExpressionSite.builder().build().allowsSimplifiedLiterals());
	}

	@Test
	public void testAllowLiteralsBuilder() {
		Assert.assertFalse(ExpressionSite.builder().allowLiterals(false).build().allowsLiterals());
	}

	@Test
	public void testAllowNonLiteralsBuilder() {
		Assert.assertFalse(ExpressionSite.builder().allowNonLiterals(false).build().allowsNonLiterals());
	}

	@Test
	public void testAllowSimplifiedLiteralsBuilder() {
		Assert.assertFalse(ExpressionSite.builder().allowSimplifiedLiterals(false).build().allowsSimplifiedLiterals());
	}

	// -- Return types --

	@Test
	public void testReturnTypesBuilder() {
		ClassInfoReference ref = new ClassInfoReference(new ClassInfo<>(Object.class, "object"), Kleenean.UNKNOWN);
		ExpressionSite site = ExpressionSite.builder().returnTypes(ref).build();
		Assert.assertEquals(1, site.getReturnTypes().size());
		Assert.assertTrue(site.getReturnTypes().contains(ref));
	}

	// -- TimeState --

	@Test
	public void testTimeStateDefaultIsPresent() {
		Assert.assertEquals(ExpressionSite.TimeState.PRESENT, ExpressionSite.builder().build().timeState());
	}

	@Test
	public void testTimeStateEnumBuilder() {
		Assert.assertEquals(ExpressionSite.TimeState.PAST,
				ExpressionSite.builder().timeState(ExpressionSite.TimeState.PAST).build().timeState());
		Assert.assertEquals(ExpressionSite.TimeState.FUTURE,
				ExpressionSite.builder().timeState(ExpressionSite.TimeState.FUTURE).build().timeState());
	}

	@Test
	public void testTimeStateIntBuilder() {
		Assert.assertEquals(ExpressionSite.TimeState.PAST,
				ExpressionSite.builder().timeState(-1).build().timeState());
		Assert.assertEquals(ExpressionSite.TimeState.PRESENT,
				ExpressionSite.builder().timeState(0).build().timeState());
		Assert.assertEquals(ExpressionSite.TimeState.FUTURE,
				ExpressionSite.builder().timeState(1).build().timeState());
	}

	@Test
	public void testTimeStateIntBuilderThrowsForInvalidValue() {
		Assert.assertThrows(IllegalArgumentException.class,
				() -> ExpressionSite.builder().timeState(99));
	}

	// -- toBuilder / applyTo --

	@Test
	public void testToBuilderRoundTrip() {
		ExpressionSite original = ExpressionSite.builder()
				.optional(true)
				.allowLiterals(false)
				.allowNonLiterals(false)
				.timeState(ExpressionSite.TimeState.PAST)
				.build();
		ExpressionSite copy = original.toBuilder().build();
		Assert.assertEquals(original.isOptional(), copy.isOptional());
		Assert.assertEquals(original.allowsLiterals(), copy.allowsLiterals());
		Assert.assertEquals(original.allowsNonLiterals(), copy.allowsNonLiterals());
		Assert.assertEquals(original.timeState(), copy.timeState());
	}

	@Test
	public void testApplyToTransfersAllFields() {
		ExpressionSite original = ExpressionSite.builder()
				.optional(true)
				.allowSimplifiedLiterals(false)
				.timeState(ExpressionSite.TimeState.FUTURE)
				.build();
		ExpressionSite.Builder target = ExpressionSite.builder();
		original.applyTo(target);
		ExpressionSite copy = target.build();
		Assert.assertEquals(original.isOptional(), copy.isOptional());
		Assert.assertEquals(original.allowsSimplifiedLiterals(), copy.allowsSimplifiedLiterals());
		Assert.assertEquals(original.timeState(), copy.timeState());
	}

	// -- TimeState enum --

	@Test
	public void testTimeStateGetValue() {
		Assert.assertEquals(-1, ExpressionSite.TimeState.PAST.getValue());
		Assert.assertEquals(0, ExpressionSite.TimeState.PRESENT.getValue());
		Assert.assertEquals(1, ExpressionSite.TimeState.FUTURE.getValue());
	}

	@Test
	public void testTimeStateFromInt() {
		Assert.assertEquals(ExpressionSite.TimeState.PAST, ExpressionSite.TimeState.fromInt(-1));
		Assert.assertEquals(ExpressionSite.TimeState.PRESENT, ExpressionSite.TimeState.fromInt(0));
		Assert.assertEquals(ExpressionSite.TimeState.FUTURE, ExpressionSite.TimeState.fromInt(1));
	}

	@Test
	public void testTimeStateFromIntThrowsForInvalidValue() {
		Assert.assertThrows(IllegalArgumentException.class, () -> ExpressionSite.TimeState.fromInt(99));
	}

}
