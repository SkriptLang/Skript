# Custom parser for multiline Java strings
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DocsParser {
    public static String parseMultilineString(String input) {
        // Use regular expression to match multiline Java strings
        Pattern pattern = Pattern.compile("\"\"\"([\\s\\S]*?)\"\"\"");
        Matcher matcher = pattern.matcher(input);
        StringBuilder builder = new StringBuilder();

        while (matcher.find()) {
            String match = matcher.group(1);
            // Preserve newline characters and indentation
            String parsedMatch = match.replaceAll("\n", "\n    ");
            builder.append(parsedMatch);
        }

        return builder.toString();
    }
}