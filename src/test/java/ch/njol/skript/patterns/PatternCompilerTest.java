package ch.njol.skript.patterns;

import ch.njol.skript.patterns.SkriptPattern.StringificationProperties;
import org.junit.Test;

import static org.junit.Assert.*;

public class PatternCompilerTest {

	@Test
	public void testToString() {
		assertEquals("hello", PatternCompiler.compile("hello").toString());
		assertEquals("hello  world", PatternCompiler.compile("hello  world").toString());
		assertEquals("hello [world]", PatternCompiler.compile("hello [world]").toString());
		assertEquals("hello (world|server)", PatternCompiler.compile("hello (world|server)").toString());
		assertEquals("hello <.*>", PatternCompiler.compile("hello <.*>").toString());
		assertEquals("hello [world|server]", PatternCompiler.compile("hello [(world|server)]").toString());

		StringificationProperties properties = StringificationProperties.builder()
			.excludeParseTags()
			.excludeTypeFlags()
			.build();
		assertEquals("hello [a:world]", PatternCompiler.compile("hello [a:world]").toString());
		assertEquals("hello [world]", PatternCompiler.compile("hello [a:world]").toString(properties));
		assertEquals("hello [%-number%]", PatternCompiler.compile("hello [%-number%]").toString());
		assertEquals("hello [%number%]", PatternCompiler.compile("hello [%-number%]").toString(properties));
		assertEquals("hello [%number@1%]", PatternCompiler.compile("hello [%number@1%]").toString());
		assertEquals("hello [%number%]", PatternCompiler.compile("hello [%number@1%]").toString(properties));
	}

}
