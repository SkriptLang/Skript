package ch.njol.skript.expressions;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import ch.njol.skript.SkriptConfig;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SyntaxStringBuilder;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import ch.njol.util.StringUtils;

@Name("Join & Split")
@Description("Joins several texts with a common delimiter (e.g. \", \"), or splits a text into multiple texts at a given delimiter.")
@Examples({
	"message \"Online players: %join all players' names with \"\" | \"\"%\" # %all players% would use the default \"x, y, and z\"",
	"set {_s::*} to the string argument split at \",\""
})
@Since("2.1, 2.5.2 (regex support), 2.7 (case sensitivity), INSERT VERSION (without trailing string)")
public class ExprJoinSplit extends SimpleExpression<String> {

	static {
		Skript.registerExpression(ExprJoinSplit.class, String.class, ExpressionType.COMBINED,
			"(concat[enate]|join) %strings% [(with|using|by) [[the] delimiter] %-string%]",
			"split %string% (at|using|by) [[the] delimiter] %string% [case:with case sensitivity] [trailing:without [the] trailing [empty] (string|text)]",
			"%string% split (at|using|by) [[the] delimiter] %string% [case:with case sensitivity] [trailing:without [the] trailing [empty] (string|text)]",
			"regex split %string% (at|using|by) [[the] delimiter] %string% [trailing:without [the] trailing [empty] (string|text)]",
			"regex %string% split (at|using|by) [[the] delimiter] %string% [trailing:without [the] trailing [empty] (string|text)]");
	}

	private boolean join;
	private boolean regex;
	private boolean caseSensitivity;
	private boolean removeTrailing;

	private Expression<String> strings;
	private @Nullable Expression<String> delimiter;

	private @Nullable Pattern pattern;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		join = matchedPattern == 0;
		regex = matchedPattern >= 3;
		caseSensitivity = SkriptConfig.caseSensitive.value() || parseResult.hasTag("case");
		removeTrailing = parseResult.hasTag("trailing");
		//noinspection unchecked
		strings = (Expression<String>) exprs[0];
		//noinspection unchecked
		delimiter = (Expression<String>) exprs[1];
		if (!join && delimiter instanceof Literal<String> literal) {
			String stringPattern = literal.getSingle();
			try {
				this.pattern = compilePattern(stringPattern);
			} catch (PatternSyntaxException e) {
				Skript.error("'" + stringPattern + "' is not a valid regular expression");
				return false;
			}
		}
		return true;
	}

	@Override
	protected String @Nullable [] get(Event event) {
		String[] strings = this.strings.getArray(event);
		String delimiter = this.delimiter != null ? this.delimiter.getSingle(event) : "";

		if (strings.length == 0 || delimiter == null)
			return new String[0];

		if (join)
			return new String[] {StringUtils.join(strings, delimiter)};

		try {
			Pattern pattern = this.pattern;
			if (pattern == null)
				pattern = compilePattern(delimiter);
			return pattern.split(strings[0], removeTrailing ? 0 : -1);
		} catch (PatternSyntaxException e) {
			return new String[0];
		}
	}

	@Override
	public boolean isSingle() {
		return join;
	}

	@Override
	public Class<? extends String> getReturnType() {
		return String.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		SyntaxStringBuilder builder = new SyntaxStringBuilder(event, debug);
		if (join) {
			builder.append("join", strings);
			if (delimiter != null)
				builder.append("with", delimiter);
			return builder.toString();
		}

        assert delimiter != null;
		if (regex)
			builder.append("regex");
        builder.append("split", strings, "at", delimiter);
		if (removeTrailing)
			builder.append("without trailing text");
		if (!regex)
			builder.append("(case sensitive:", caseSensitivity + ")");
		return builder.toString();
	}

	private Pattern compilePattern(String delimiter) {
		return Pattern.compile(regex ? delimiter : (caseSensitivity ? "" : "(?i)") + Pattern.quote(delimiter));
	}

}
