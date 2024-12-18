package ch.njol.skript.patterns;

import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import com.google.common.base.MoreObjects;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A result from pattern matching.
 */
public class MatchResult {

	SkriptPattern source;

	int exprOffset;

	Expression<?>[] expressions = new Expression[0];
	String expr;
	int mark;
	List<String> tags = new ArrayList<>();
	List<java.util.regex.MatchResult> regexResults = new ArrayList<>();

	// SkriptParser stuff
	ParseContext parseContext = ParseContext.DEFAULT;
	int flags;

	public MatchResult copy() {
		MatchResult matchResult = new MatchResult();
		matchResult.source = this.source;
		matchResult.exprOffset = this.exprOffset;
		matchResult.expressions = this.expressions.clone();
		matchResult.expr = this.expr;
		matchResult.mark = this.mark;
		matchResult.tags = new ArrayList<>(this.tags);
		matchResult.regexResults = new ArrayList<>(this.regexResults);
		matchResult.parseContext = this.parseContext;
		matchResult.flags = this.flags;
		return matchResult;
	}

	public ParseResult toParseResult() {
		ParseResult parseResult = new ParseResult(expr, expressions);
		parseResult.source = source;
		parseResult.regexes.addAll(regexResults);
		parseResult.mark = mark;
		parseResult.tags.addAll(tags);
		return parseResult;
	}

	public Expression<?>[] getExpressions() {
		return expressions;
	}

	public String getExpr() {
		return expr;
	}

	public int getMark() {
		return mark;
	}

	public List<String> getTags() {
		return tags;
	}

	public List<java.util.regex.MatchResult> getRegexResults() {
		return regexResults;
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
			.add("source", source)
			.add("exprOffset", exprOffset)
			.add("expressions", Arrays.toString(expressions))
			.add("expr", expr)
			.add("mark", mark)
			.add("tags", tags)
			.add("regexResults", regexResults)
			.add("parseContext", parseContext)
			.add("flags", flags)
			.toString();
	}

}
