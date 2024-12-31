package org.skriptlang.skript.util;

import org.junit.Assert;
import org.junit.Test;

import java.util.AbstractCollection;
import java.util.Collection;

public class ClassUtilsTest {

	@Test
	public void testIsNormalClass() {
		Assert.assertTrue(ClassUtils.isNormalClass(String.class));
		Assert.assertFalse(ClassUtils.isNormalClass(Test.class));
		Assert.assertFalse(ClassUtils.isNormalClass(String[].class));
		Assert.assertFalse(ClassUtils.isNormalClass(int.class));
		Assert.assertFalse(ClassUtils.isNormalClass(Collection.class));
		Assert.assertFalse(ClassUtils.isNormalClass(AbstractCollection.class));
	}

}
