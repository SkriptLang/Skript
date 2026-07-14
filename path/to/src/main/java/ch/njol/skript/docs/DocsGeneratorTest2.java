# Test class for DocsGenerator with multiline Java strings
import org.junit.Test;

public class DocsGeneratorTest2 {
    @Test
    public void testGenerateDocsMultiline() {
        String input = "public class MyClass {\n" +
                "    public static void main(String[] args) {\n" +
                "        System.out.println(\"Hello, World!\");\n" +
                "        System.out.println(\"This is a multiline string\");\n" +
                "    }\n" +
                "}";
        String expectedOutput = "public class MyClass {\n" +
                "    public static void main(String[] args) {\n" +
                "        System.out.println(\"Hello, World!\");\n" +
                "        System.out.println(\"This is a multiline string\");\n" +
                "    }\n" +
                "}";
        String actualOutput = DocsGenerator.generateDocs(input);
        assert expectedOutput.equals(actualOutput);
    }
}