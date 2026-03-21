package org.skriptlang.skript.lang.parsing.sites;

import ch.njol.skript.test.runner.SkriptJUnitTest;
import org.junit.Assert;
import org.junit.Test;

import java.util.Iterator;
import java.util.List;

public class ExpressionListSiteTest extends SkriptJUnitTest {

	// -- Constraints --

	@Test
	public void testConstraintReturnsSameInstance() {
		ExpressionListSite site = new ExpressionListSite(List.of());
		Assert.assertSame(site.constraints(), site.constraints());
	}

	// -- Optional --

	@Test
	public void testOptionalDefaultIsFalse() {
		Assert.assertFalse(new ExpressionListSite(List.of()).isOptional());
	}

	@Test
	public void testOptionalConstructor() {
		Assert.assertTrue(new ExpressionListSite(List.of(), true).isOptional());
		Assert.assertFalse(new ExpressionListSite(List.of(), false).isOptional());
	}

	// -- Expression sites --

	@Test
	public void testGetExpressionSitesIsUnmodifiable() {
		ExpressionListSite site = new ExpressionListSite(List.of());
		Assert.assertThrows(UnsupportedOperationException.class,
				() -> site.getExpressionSites().add(ExpressionSite.builder().build()));
	}

	@Test
	public void testGetExpressionSitesReturnsContents() {
		ExpressionSite a = ExpressionSite.builder().build();
		ExpressionSite b = ExpressionSite.builder().build();
		ExpressionListSite site = new ExpressionListSite(List.of(a, b));
		List<ExpressionSite> sites = site.getExpressionSites();
		Assert.assertEquals(2, sites.size());
		Assert.assertSame(a, sites.get(0));
		Assert.assertSame(b, sites.get(1));
	}

	@Test
	public void testIteratorMatchesSites() {
		ExpressionSite a = ExpressionSite.builder().build();
		ExpressionSite b = ExpressionSite.builder().build();
		ExpressionListSite site = new ExpressionListSite(List.of(a, b));
		Iterator<ExpressionSite> it = site.iterator();
		Assert.assertTrue(it.hasNext());
		Assert.assertSame(a, it.next());
		Assert.assertTrue(it.hasNext());
		Assert.assertSame(b, it.next());
		Assert.assertFalse(it.hasNext());
	}

}
