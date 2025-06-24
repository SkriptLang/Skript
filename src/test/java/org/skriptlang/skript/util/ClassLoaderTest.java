package org.skriptlang.skript.util;

import org.junit.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;

public class ClassLoaderTest {

	private static void load(ClassLoader loader) {
		loader.loadClasses(ClassLoaderTest.class);
	}

	@Test
	public void testBasePackage() {
		Set<Class<?>> classes = new HashSet<>();
		load(ClassLoader.builder()
				.basePackage("org.skriptlang.skript.util")
				.forEachClass(classes::add)
				.build());
		assertTrue(classes.contains(ClassLoader.class));
		assertTrue(classes.contains(ClassLoaderTest.class));
	}

	@Test
	public void testSubPackages() {
		Set<Class<?>> classes = new HashSet<>();
		ClassLoader.Builder builder = ClassLoader.builder()
				.basePackage("org.skriptlang.skript")
				.addSubPackages("fake1")
				.addSubPackages(List.of("fake2"))
				.forEachClass(classes::add);

		// test without this subpackage
		load(builder.build());
		assertFalse(classes.contains(ClassLoader.class));
		assertFalse(classes.contains(ClassLoaderTest.class));

		// test with this subpackage
		classes.clear();
		load(builder.addSubPackage("util").build());
		assertTrue(classes.contains(ClassLoader.class));
		assertTrue(classes.contains(ClassLoaderTest.class));
	}

	@Test
	public void testFilter() {
		Set<Class<?>> classes = new HashSet<>();
		load(ClassLoader.builder()
				.basePackage("org.skriptlang.skript.util")
				.filter(fqn -> !fqn.endsWith("Test")) // filter out class names ending with "Test"
				.forEachClass(classes::add)
				.build());
		assertTrue(classes.contains(ClassLoader.class));
		assertFalse(classes.contains(ClassLoaderTest.class));
	}

	@Test
	public void testDeep() {
		Set<Class<?>> classes = new HashSet<>();
		ClassLoader.Builder builder = ClassLoader.builder()
				.basePackage("org.skriptlang.skript")
				.forEachClass(classes::add);

		// test without deep
		load(builder.deep(false).build());
		assertFalse(classes.contains(ClassLoader.class));
		assertFalse(classes.contains(ClassLoaderTest.class));

		// test with deep
		classes.clear();
		load(builder.deep(true).build());
		assertTrue(classes.contains(ClassLoader.class));
		assertTrue(classes.contains(ClassLoaderTest.class));
	}

}
