package ch.njol.skript.patterns;

import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;

/**
 * Unit tests for pattern matching, covering minimum length computation,
 * keyword pre-filtering, and basic match correctness.
 */
public class PatternMatchingTest {

	// ---- Helper methods ----

	private static PatternElement compileElement(String pattern) {
		return PatternCompiler.compile(pattern, new AtomicInteger());
	}

	private static SkriptPattern compilePattern(String pattern) {
		return PatternCompiler.compile(pattern);
	}

	// ---- computeMinLength tests ----

	@Test
	public void testMinLengthSimpleLiteral() {
		// "hello world" has 10 non-space characters
		PatternElement element = compileElement("hello world");
		assertEquals(10, Keyword.computeMinLength(element));
	}

	@Test
	public void testMinLengthLiteralWithLeadingTrailingSpaces() {
		// " hello " — spaces are flexible, non-space = 5
		PatternElement element = compileElement(" hello ");
		assertEquals(5, Keyword.computeMinLength(element));
	}

	@Test
	public void testMinLengthOptionalContributesZero() {
		// "[the] name" — optional contributes 0, " name" = 4 non-space chars
		PatternElement element = compileElement("[the] name");
		assertEquals(4, Keyword.computeMinLength(element));
	}

	@Test
	public void testMinLengthAllOptional() {
		// "[hello] [world]" — all optional, minLength = 0
		PatternElement element = compileElement("[hello] [world]");
		assertEquals(0, Keyword.computeMinLength(element));
	}

	@Test
	public void testMinLengthChoiceUsesMinimum() {
		// "(a|bb|ccc)" — min of choices: "a"=1, "bb"=2, "ccc"=3 → 1
		PatternElement element = compileElement("(a|bb|ccc)");
		assertEquals(1, Keyword.computeMinLength(element));
	}

	@Test
	public void testMinLengthChoiceWithOptional() {
		// "(hello|[the] world)" — "hello"=5, "[the] world" = 5 → min = 5
		PatternElement element = compileElement("(hello|[the] world)");
		assertEquals(5, Keyword.computeMinLength(element));
	}

	@Test
	public void testMinLengthChoiceWithEmptyBranch() {
		// "(hello|)" — one choice is empty → min = 0
		PatternElement element = compileElement("(hello|)");
		assertEquals(0, Keyword.computeMinLength(element));
	}

	@Test
	public void testMinLengthTypePatternContributesZero() {
		// "%number% test" — type=0, " test"=4 → 4
		PatternElement element = compileElement("%number% test");
		assertEquals(4, Keyword.computeMinLength(element));
	}

	@Test
	public void testMinLengthOnlyType() {
		// "%number%" — just a type, minLength = 0
		PatternElement element = compileElement("%number%");
		assertEquals(0, Keyword.computeMinLength(element));
	}

	@Test
	public void testMinLengthGroup() {
		// "(hello world)" — group wraps literal, 10 non-space chars
		PatternElement element = compileElement("(hello world)");
		assertEquals(10, Keyword.computeMinLength(element));
	}

	@Test
	public void testMinLengthRegexContributesZero() {
		// "<.+> test" — regex=0, " test"=4 → 4
		PatternElement element = compileElement("<.+> test");
		assertEquals(4, Keyword.computeMinLength(element));
	}

	@Test
	public void testMinLengthParseTagContributesZero() {
		PatternElement element = compileElement("(high:tall|low) fall");
		// choices: "tall"=4, "low"=3 → min=3; " fall"=4 → total=7
		assertEquals(7, Keyword.computeMinLength(element));
	}

	@Test
	public void testMinLengthComplexPattern() {
		// "[the] name of %player%" — "[the]"=0, " name of "=6, "%player%"=0 → 6
		PatternElement element = compileElement("[the] name of %player%");
		assertEquals(6, Keyword.computeMinLength(element));
	}

	@Test
	public void testMinLengthAnglePattern() {
		// "%number% [in] rad[ian][s]" — type=0, " [in] "=0 (all optional surrounding spaces),
		// actually: " " + "[in]" + " rad" + "[ian]" + "[s]"
		// Literals: " " before [in] has 0 non-space, " rad" has 3 non-space
		// So minLength = 3
		PatternElement element = compileElement("%number% [in] rad[ian][s]");
		assertEquals(3, Keyword.computeMinLength(element));
	}

	@Test
	public void testMinLengthAnglePluralPattern() {
		// "%numbers% in rad[ian][s]" — " in rad" = 5 non-space
		PatternElement element = compileElement("%numbers% in rad[ian][s]");
		assertEquals(5, Keyword.computeMinLength(element));
	}

	@Test
	public void testMinLengthCasePattern() {
		// "%strings% in (upper|lower)[ ]case"
		// " in " = 2 non-space, choice min("upper"=5, "lower"=5)=5, "[ ]" = 0, "case"=4
		// total = 2+5+4 = 11
		PatternElement element = compileElement("%strings% in (upper|lower)[ ]case");
		assertEquals(11, Keyword.computeMinLength(element));
	}

	@Test
	public void testMinLengthNestedOptionalInChoice() {
		// "(a [b]|c d e)" — choice: "a [b]"=min 1, "c d e"=3 → min=1
		PatternElement element = compileElement("(a [b]|c d e)");
		assertEquals(1, Keyword.computeMinLength(element));
	}

	// ---- SkriptPattern.match() basic tests (literal-only patterns, no type registration needed) ----

	@Test
	public void testMatchSimpleLiteral() {
		SkriptPattern pattern = compilePattern("hello world");
		assertNotNull("Should match exact literal", pattern.match("hello world"));
	}

	@Test
	public void testMatchLiteralCaseInsensitive() {
		SkriptPattern pattern = compilePattern("hello world");
		assertNotNull("Should match case-insensitively", pattern.match("Hello World"));
	}

	@Test
	public void testMatchRejectsShortInput() {
		// "hello world" has minLength=10, so "hi" should be rejected
		SkriptPattern pattern = compilePattern("hello world");
		assertNull("Should reject input shorter than minLength", pattern.match("hi"));
	}

	@Test
	public void testMatchRejectsEmptyInput() {
		SkriptPattern pattern = compilePattern("hello");
		assertNull("Should reject empty input", pattern.match(""));
	}

	@Test
	public void testMatchWithOptional() {
		SkriptPattern pattern = compilePattern("[the] name");
		assertNotNull("Should match with optional present", pattern.match("the name"));
		assertNotNull("Should match without optional", pattern.match("name"));
	}

	@Test
	public void testMatchWithChoice() {
		SkriptPattern pattern = compilePattern("(hello|goodbye) world");
		assertNotNull("Should match first choice", pattern.match("hello world"));
		assertNotNull("Should match second choice", pattern.match("goodbye world"));
		assertNull("Should reject non-matching choice", pattern.match("howdy world"));
	}

	@Test
	public void testMatchMinLengthDoesNotRejectValidShortInput() {
		// "[a] b" has minLength=1. Input "b" (length 1) should match.
		SkriptPattern pattern = compilePattern("[a] b");
		assertNotNull("Input at exactly minLength should not be rejected", pattern.match("b"));
	}

	@Test
	public void testMatchMinLengthBoundary() {
		SkriptPattern pattern = compilePattern("abc");
		assertNull("Input shorter than minLength should be rejected", pattern.match("ab"));
		assertNotNull("Input at minLength should match", pattern.match("abc"));
	}

	@Test
	public void testMatchWithKeywordFiltering() {
		SkriptPattern pattern = compilePattern("set the name");
		assertNull("Should be rejected by keyword filter", pattern.match("get the name"));
		assertNotNull("Should pass keyword filter", pattern.match("set the name"));
	}

	@Test
	public void testMatchOptionalOnlyPattern() {
		SkriptPattern pattern = compilePattern("[hello]");
		assertNotNull("Optional-only pattern should match when optional is present", pattern.match("hello"));
		assertNotNull("Optional-only pattern should match empty string", pattern.match(""));
	}

	@Test
	public void testMatchChoiceWithOptionalBranch() {
		SkriptPattern pattern = compilePattern("(hello|[the] world)");
		assertNotNull("Should match first choice", pattern.match("hello"));
		assertNotNull("Should match second choice with optional", pattern.match("the world"));
		assertNotNull("Should match second choice without optional", pattern.match("world"));
	}

	@Test
	public void testMatchMultipleOptionals() {
		SkriptPattern pattern = compilePattern("[a] [b] c [d]");
		assertNotNull("Should match with all optionals", pattern.match("a b c d"));
		assertNotNull("Should match with no optionals", pattern.match("c"));
		assertNotNull("Should match with some optionals", pattern.match("a c"));
	}

	// ---- Tests for type-involving patterns ----

	@Test
	public void testParseAngleDegreeSingle() {
		ParseResult result = SkriptParser.parse("90 degrees", "%number% [in] deg[ree][s]",
			SkriptParser.ALL_FLAGS, ParseContext.DEFAULT);
		assertNotNull("Should parse '90 degrees'", result);
		assertEquals(1, result.exprs.length);
		assertEquals(90L, result.exprs[0].getSingle(null));
	}

	@Test
	public void testParseAngleRadianSingle() {
		ParseResult result = SkriptParser.parse("3.14 radians", "%number% [in] rad[ian][s]",
			SkriptParser.ALL_FLAGS, ParseContext.DEFAULT);
		assertNotNull("Should parse '3.14 radians'", result);
		assertEquals(1, result.exprs.length);
	}

	@Test
	public void testParseAngleRadianShortForm() {
		ParseResult result = SkriptParser.parse("1 rad", "%number% [in] rad[ian][s]",
			SkriptParser.ALL_FLAGS, ParseContext.DEFAULT);
		assertNotNull("Should parse '1 rad'", result);
	}

	@Test
	public void testParseAngleDegreeWithIn() {
		ParseResult result = SkriptParser.parse("90 in degrees", "%number% [in] deg[ree][s]",
			SkriptParser.ALL_FLAGS, ParseContext.DEFAULT);
		assertNotNull("Should parse '90 in degrees'", result);
	}

	@Test
	public void testParseAnglePluralRadians() {
		// Pattern: "%numbers% in rad[ian][s]"
		ParseResult result = SkriptParser.parse("1, 2 and 3 in radians", "%numbers% in rad[ian][s]",
			SkriptParser.ALL_FLAGS, ParseContext.DEFAULT);
		assertNotNull("Should parse '1, 2 and 3 in radians'", result);
		assertArrayEquals(new Long[]{1L, 2L, 3L}, result.exprs[0].getArray(null));

		result = SkriptParser.parse("0.5 * pi, pi and 1.5 * pi in radians", "%numbers% in rad[ian][s]",
			SkriptParser.ALL_FLAGS, ParseContext.DEFAULT);
		assertNotNull("Should parse '0.5*pi', 'pi' and '1.5 * pi' in radians'", result);
		assertArrayEquals(new Double[]{0.5*Math.PI, Math.PI, 1.5*Math.PI}, result.exprs[0].getArray(null));

		Expression<?> expr = new SkriptParser("0.5 * pi, pi and 1.5 * pi in radians").parseExpression(Number.class);
		assertNotNull("Should parse 0.5 times pi'", expr);
		assertArrayEquals(new Double[]{90.0, 180.0, 270.0}, expr.getArray(null));
	}

	@Test
	public void testParseAnglePluralDegrees() {
		ParseResult result = SkriptParser.parse("90, 180 and 270 in degrees", "%numbers% in deg[ree][s]",
			SkriptParser.ALL_FLAGS, ParseContext.DEFAULT);
		assertNotNull("Should parse '90, 180 and 270 in degrees'", result);
		assertArrayEquals(new Long[]{90L, 180L, 270L}, result.exprs[0].getArray(null));
	}

	@Test
	public void testParseCasePatternLowercase() {
		ParseResult result = SkriptParser.parse("\"hello\" in lowercase", "%strings% in (upper|lower)[ ]case",
			SkriptParser.ALL_FLAGS, ParseContext.DEFAULT);
		assertNotNull("Should parse 'hello in lowercase'", result);
	}

	@Test
	public void testParseCasePatternUppercase() {
		ParseResult result = SkriptParser.parse("\"hello\" in uppercase", "%strings% in (upper|lower)[ ]case",
			SkriptParser.ALL_FLAGS, ParseContext.DEFAULT);
		assertNotNull("Should parse 'hello in uppercase'", result);
	}

	@Test
	public void testParseCasePatternWithSpace() {
		ParseResult result = SkriptParser.parse("\"hello\" in lower case", "%strings% in (upper|lower)[ ]case",
			SkriptParser.ALL_FLAGS, ParseContext.DEFAULT);
		assertNotNull("Should parse 'hello in lower case'", result);
	}

	// Use proper case as a test for legacy patterns

	@Test
	public void testMinLengthProperCasePatterns() {
		// "%strings% in [(lenient|strict) ](proper|title)[ ]case"
		// " in "=2, "[(lenient|strict) ]"=0 (optional), min("proper"=6,"title"=5)=5, "[ ]"=0, "case"=4 → 11
		PatternElement element = compileElement("%strings% in [(0¦lenient|1¦strict) ](proper|title)[ ]case");
		assertEquals(11, Keyword.computeMinLength(element));
	}

	@Test
	public void testParseProperCasePattern() {
		ParseResult result = SkriptParser.parse("\"hello\" in proper case",
			"%strings% in [(0¦lenient|1¦strict) ](proper|title)[ ]case",
			SkriptParser.ALL_FLAGS, ParseContext.DEFAULT);
		assertNotNull("Should parse 'in proper case'", result);

		result = SkriptParser.parse("\"hello\" in titlecase",
			"%strings% in [(0¦lenient|1¦strict) ](proper|title)[ ]case",
			SkriptParser.ALL_FLAGS, ParseContext.DEFAULT);
		assertNotNull("Should parse 'in titlecase'", result);

		result = SkriptParser.parse("\"hello\" in strict proper case",
			"%strings% in [(0¦lenient|1¦strict) ](proper|title)[ ]case",
			SkriptParser.ALL_FLAGS, ParseContext.DEFAULT);
		assertNotNull("Should parse 'in strict proper case'", result);
	}

}
