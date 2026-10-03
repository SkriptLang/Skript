package ch.njol.skript.doc;

import ch.njol.skript.lang.function.Function;
import ch.njol.skript.lang.function.JavaFunction;
import org.easymock.EasyMock;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

@SuppressWarnings("removal")
public class HTMLGeneratorTest {

	@Rule
	public TemporaryFolder temporaryFolder = new TemporaryFolder();

	@Test
	public void functionExamplesPreserveNewlinesAndEscaping() throws Exception {
		assertExamples("if 1 &lt; 2:<br>\tbroadcast \"a &amp; b\"", "if 1 < 2:\n\tbroadcast \"a & b\"");
	}

	@Test
	public void functionExamplesPreserveTextBlocks() throws Exception {
		assertExamples("if true:<br>    broadcast \"hello\"<br>", """
				if true:
				    broadcast "hello"
				""");
	}

	@Test
	public void functionExamplesNormalizeWindowsNewlines() throws Exception {
		assertExamples("first<br>second<br>third", "first\r\nsecond\rthird");
	}

	@Test
	public void functionExamplesKeepSeparateEntries() throws Exception {
		assertExamples("first<br>second<br>third", "first\nsecond", "third");
	}

	@Test
	public void functionExamplesRetainSingleLinesAndMissingFallback() throws Exception {
		assertExamples("broadcast \"&lt;hello&gt;\"", "broadcast \"<hello>\"");
		assertExamples("Missing examples.");
	}

	@Test
	public void functionExamplesDoNotModifySourceData() throws Exception {
		String[] examples = {"a < b\n\tbroadcast \"C:\\scripts\""};
		String[] original = examples.clone();
		assertExamples("a &lt; b<br>\tbroadcast \"C:\\scripts\"", examples);
		assertExamples("a &lt; b<br>\tbroadcast \"C:\\scripts\"", examples);
		assertArrayEquals(original, examples);
	}

	private void assertExamples(String expected, String... examples) throws Exception {
		JavaFunction<?> function = EasyMock.niceMock(JavaFunction.class);
		EasyMock.expect(function.getName()).andStubReturn("htmlExampleRegression");
		EasyMock.expect(function.since()).andStubReturn(List.of("2.16"));
		EasyMock.expect(function.description()).andStubReturn(List.of("Example formatting"));
		EasyMock.expect(function.examples()).andStubReturn(Arrays.asList(examples));
		EasyMock.expect(function.keywords()).andStubReturn(List.of());
		EasyMock.replay(function);

		File templates = temporaryFolder.newFolder();
		Files.writeString(templates.toPath().resolve("template.html"), "${content}");
		HTMLGenerator generator = new HTMLGenerator(templates, temporaryFolder.newFolder());
		Method generateFunction = HTMLGenerator.class.getDeclaredMethod("generateFunction",
				String.class, Function.class);
		generateFunction.setAccessible(true);
		String generated = (String) generateFunction.invoke(generator,
				"${element.examples}\u0000${element.examples-safe}", function);
		String safe = expected.replace("\\", "\\\\").replace("\"", "\\\"").replace("\t", "    ");
		assertEquals(expected + "\u0000" + safe, generated);
	}

}
