package org.skriptlang.skript.lang.script;

import org.junit.Test;

public class SimpleAnnotationTest {

	@Test
	public void testToString() {
		Annotation annotation = new SimpleAnnotation("test");
		assert annotation.toString().equals("@test");
		annotation = Annotation.create("test");
		assert annotation.toString().equals("@test");
	}

	@Test
	public void testLength() {
		Annotation annotation = new SimpleAnnotation("test");
		assert annotation.length() == 5;
		annotation = Annotation.create("test");
		assert annotation.length() == 5;
	}

	@Test
	public void testCharAt() {
		Annotation annotation = new SimpleAnnotation("test");
		assert annotation.charAt(0) == '@';
		assert annotation.charAt(1) == 't';
		assert annotation.charAt(2) == 'e';
		assert annotation.charAt(3) == 's';
		assert annotation.charAt(4) == 't';
		annotation = Annotation.create("test");
		assert annotation.charAt(0) == '@';
		assert annotation.charAt(1) == 't';
		assert annotation.charAt(2) == 'e';
		assert annotation.charAt(3) == 's';
		assert annotation.charAt(4) == 't';
	}

	@Test
	public void testSubSequence() {
		Annotation annotation = Annotation.create("test");
		assert annotation.subSequence(0, 1).equals("@");
		assert annotation.subSequence(0, 2).equals("@t");
		assert annotation.subSequence(0, 4).equals("@tes");
		assert annotation.subSequence(1, annotation.length()).equals("test");
	}

}
