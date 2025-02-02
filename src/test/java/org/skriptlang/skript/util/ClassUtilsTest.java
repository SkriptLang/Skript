package org.skriptlang.skript.util;

import org.junit.Test;

import java.util.AbstractCollection;
import java.util.Collection;

import static org.junit.Assert.*;

public class ClassUtilsTest {

	@Test
	public void testIsNormalClass() {
		assertTrue(ClassUtils.isNormalClass(String.class));
		assertFalse(ClassUtils.isNormalClass(Test.class));
		assertFalse(ClassUtils.isNormalClass(String[].class));
		assertFalse(ClassUtils.isNormalClass(int.class));
		assertFalse(ClassUtils.isNormalClass(Collection.class));
		assertFalse(ClassUtils.isNormalClass(AbstractCollection.class));
	}

}
