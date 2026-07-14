# Test class for DocsGenerator
import org.junit.Test;

public class DocsGeneratorTest {
    @Test
    public void testGenerateDocs() {
        String input = "public class MyClass {\n" +
                "    public static void main(String[] args) {\n" +
                "        System.out.println(\"Hello, World!\");\n" +
                "    }\n" +
                "}";
        String expectedOutput = "public class MyClass {\n" +
                "    public static void main(String[] args) {\n" +
                "        System.out.println(\"Hello, World!\");\n" +
                "    }\n" +
                "}";
        String actualOutput = DocsGenerator.generateDocs(input);
        assert expectedOutput.equals(actualOutput);
    }
}