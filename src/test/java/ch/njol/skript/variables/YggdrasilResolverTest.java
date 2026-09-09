package ch.njol.skript.variables;

import ch.njol.yggdrasil.SimpleClassResolver;
import org.junit.Test;

import static org.junit.Assert.*;

public class YggdrasilResolverTest {
	private static class Parent {}
	private static class Child extends Parent {}
	private static class Leaf extends Child {}

	@Test
	public void choosesTheMostSpecificRegisteredSuperclass() {
		SimpleClassResolver resolver = new SimpleClassResolver();
		resolver.registerClass(Parent.class, "parent");
		assertEquals("parent", resolver.getID(Leaf.class));
		resolver.registerClass(Child.class, "child");
		assertEquals("child", resolver.getID(Leaf.class));
		assertEquals("child", resolver.getID(Child.class));
		assertSame(Child.class, resolver.getClass("child"));
		assertNull(resolver.getID(String.class));
	}
}
