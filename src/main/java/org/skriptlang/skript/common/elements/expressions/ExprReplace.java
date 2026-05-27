package org.skriptlang.skript.common.elements.expressions;

import ch.njol.skript.SkriptConfig;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import ch.njol.util.StringUtils;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.DefaultSyntaxInfos;
import org.skriptlang.skript.registration.SyntaxRegistry;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Name("Text Replacement")
@Description("Performs a text replacement on a given value, returning the result. Supports regex and case sensitive replacement.")
@Example("send \"Welcome [player]\" where \"[player]\" is replaced with \"%player%\" to player")
@Example("""
	# Function for sanitizing user inputs
	# Strips the input of any non-alphanumeric characters using regex
	function sanitizeInput(input: string) :: string:
		return {_input} where regex pattern "\\W" is replaced with ""
	""")
@Example("""
	# Function to convert &# hex color codes to <# > (mini message format)
	function colorFormat(input: string) :: string:
		return {_input} where all instances of regex pattern "&#([a-fA-F0-9]{6})" are replaced with "<#$1>"
	""")
@Example("""
	# Very simple chat censor
	on chat:
		set message to message where all instances of "idiot", "noob" are replaced with "****"
		set message to message where regex "\\b(idiot|noob)\\b" is replaced with "****" # Regex version using word boundaries for better results
	""")
@Since("INSERT VERSION")
public class ExprReplace extends SimpleExpression<String> {

	public static void register(SyntaxRegistry registry) {
		registry.register(
			SyntaxRegistry.EXPRESSION,
			DefaultSyntaxInfos.Expression.builder(ExprReplace.class, String.class)
				.addPatterns(
					"%strings% where [(first:[the] first instance[s]|all instances) of] %strings% (is|are) replaced with %string% [regex:using regex|case:with case sensitivity]",
					"%strings% where [(first:[the] first instance[s]|all instances) of] regex [pattern[s]] %strings% (is|are) replaced with %string%"
				)
				.supplier(ExprReplace::new)
				.build()
		);
	}

	private Expression<String> needleExpr;
	private Expression<String> haystackExpr;
	private Expression<String> replacementExpr;

	private boolean isFirst;
	private boolean isRegex;
	private boolean isCaseSensitive;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] expr, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		haystackExpr = (Expression<String>) expr[0];
		needleExpr = (Expression<String>) expr[1];
		replacementExpr = (Expression<String>) expr[2];

		isRegex = matchedPattern == 1 || parseResult.hasTag("regex");
		isFirst = parseResult.hasTag("first");
		isCaseSensitive = SkriptConfig.caseSensitive.value() || parseResult.hasTag("case");
		return true;
	}

	@Override
	protected String @Nullable [] get(Event event) {
		String replacement = replacementExpr.getSingle(event);
		String[] needles = needleExpr.getArray(event);
		String[] haystacks = haystackExpr.getArray(event);

		if (replacement == null) {
			return haystacks;
		}

		List<String> result = new ArrayList<>(haystacks.length);

		if (isRegex) {
			List<Pattern> patterns = new ArrayList<>(needles.length);
			for (String needle : needles) {
				try { // Pre compile regex for use with multiple haystacks
					patterns.add(Pattern.compile(needle));
				} catch (Exception ignored) {
				}
			}

			for (String haystack : haystacks) {
				for (Pattern pattern : patterns) {
					Matcher matcher = pattern.matcher(haystack);
					try { // Throws IndexOutOfBounds on improper use of regex groups in replacement
						if (isFirst) {
							haystack = matcher.replaceFirst(replacement);
						} else {
							haystack = matcher.replaceAll(replacement);
						}
					} catch (IndexOutOfBoundsException ignored) {}
				}
				result.add(haystack);
			}
		} else {
			for (String haystack : haystacks) {
				for (String needle : needles) {
					if (isFirst) {
						haystack = StringUtils.replaceFirst(haystack, needle, replacement, isCaseSensitive);
					} else {
						haystack = StringUtils.replace(haystack, needle, replacement, isCaseSensitive);
					}
				}
				result.add(haystack);
			}
		}

		return result.toArray(new String[0]);
	}

	@Override
	public boolean isSingle() {
		return haystackExpr.isSingle();
	}


	@Override
	public Class<? extends String> getReturnType() {
		return String.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return new SyntaxStringBuilder(event, debug)
			.append("replace")
			.appendIf(isFirst, "first")
			.append(needleExpr, "in", haystackExpr, "with", replacementExpr)
			.appendIf(isRegex, "using regex")
			.appendIf(isCaseSensitive, "with case sensitivity")
			.toString();
	}

}
