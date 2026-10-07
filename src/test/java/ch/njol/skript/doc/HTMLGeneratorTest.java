package ch.njol.skript.doc;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.function.Function;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.skriptlang.skript.common.function.DefaultFunction;

import java.io.File;
import java.lang.reflect.Method;
import java.nio.file.Files;

import static org.junit.Assert.assertEquals;

public class HTMLGeneratorTest {

	@Rule
	public TemporaryFolder temporaryFolder = new TemporaryFolder();

	@Test
	public void testMultilineFunctionExamples() throws Exception {
		String example = """
			if true:
				if true:
					set {_value} to fromBase("ff", 16)

				broadcast {_value}
			""";
		String indent = "&nbsp;&nbsp;&nbsp;&nbsp;";
		String expected = "if true:<br>" + indent + "if true:<br>" + indent + indent
			+ "set {_value} to fromBase(\"ff\", 16)<br><br>" + indent + "broadcast {_value}<br>"
			+ "<br>location(0, 0, 0)";

		assertExamples(expected, example, "location(0, 0, 0)");
	}

	@Test
	public void testFunctionExampleEscaping() throws Exception {
		assertExamples("broadcast \"&lt;value&gt; &amp; C:\\test\"<br>broadcast 'done'",
			"broadcast \"<value> & C:\\test\"\nbroadcast 'done'");
	}

	@Test
	public void testMissingFunctionExamples() throws Exception {
		assertExamples("Missing examples.");
	}

	private void assertExamples(String expected, String... examples) throws Exception {
		File templateDir = temporaryFolder.newFolder();
		Files.writeString(new File(templateDir, "template.html").toPath(), "");
		HTMLGenerator generator = new HTMLGenerator(templateDir, temporaryFolder.newFolder());
		DefaultFunction<String> function = DefaultFunction.builder(Skript.instance(), "htmlGeneratorExampleTest", String.class)
			.examples(examples)
			.build(arguments -> "");
		Method generateFunction = HTMLGenerator.class.getDeclaredMethod("generateFunction", String.class, Function.class);
		generateFunction.setAccessible(true);
		String rendered = (String) generateFunction.invoke(generator,
			"<code>${element.examples}</code><code>${element.examples-safe}</code>", function);

		// Apply the page's tab conversion before exercising the actual minifier.
		rendered = rendered.replace("\t", "&nbsp;&nbsp;&nbsp;&nbsp;");
		Method minifyHtml = HTMLGenerator.class.getDeclaredMethod("minifyHtml", String.class);
		minifyHtml.setAccessible(true);
		String expectedSafe = expected.replace("\\", "\\\\").replace("\"", "\\\"")
			.replace("&nbsp;&nbsp;&nbsp;&nbsp;", " ").replace("  ", " ");
		assertEquals("<code>" + expected + "</code><code>" + expectedSafe + "</code>",
			minifyHtml.invoke(null, rendered));
	}

}
