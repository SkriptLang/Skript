package ch.njol.skript.doc;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.function.Function;
import org.junit.Test;
import org.skriptlang.skript.common.function.DefaultFunction;

import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.assertEquals;

public class HTMLGeneratorTest {

	@Test
	public void formatsMultilineFunctionExamplesAsHtmlBreaks() throws Exception {
		Path templateDir = Files.createTempDirectory("skript-html-generator");
		try {
			Files.writeString(templateDir.resolve("template.html"), "");
			HTMLGenerator generator = new HTMLGenerator(templateDir.toFile(), templateDir.toFile());
			DefaultFunction<String> function = DefaultFunction.builder(Skript.instance(), "test", String.class)
				.examples("first <line>\nsecond line")
				.build(args -> "result");

			Method generateFunction = HTMLGenerator.class.getDeclaredMethod("generateFunction", String.class, Function.class);
			generateFunction.setAccessible(true);
			String generated = (String) generateFunction.invoke(generator, "${element.examples}", function);

			assertEquals("first &lt;line&gt;<br>second line", generated);
		} finally {
			Files.deleteIfExists(templateDir.resolve("template.html"));
			Files.deleteIfExists(templateDir);
		}
	}

}
