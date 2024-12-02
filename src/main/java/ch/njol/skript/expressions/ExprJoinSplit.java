package ch.njol.skript.expressions;

import java.util.regex.Pattern;

import ch.njol.skript.SkriptConfig;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptConfig;
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
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

import java.util.regex.Pattern;

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

	@UnknownNullability
	private Expression<String> strings;
	@UnknownNullability
	private Expression<String> delimiter;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		join = matchedPattern == 0;
		regex = matchedPattern >= 3;
		caseSensitivity = SkriptConfig.caseSensitive.value() || parseResult.hasTag("case");
		removeTrailing = parseResult.hasTag("trailing");

		strings = (Expression<String>) exprs[0];
		delimiter = (Expression<String>) exprs[1];
		return true;
	}

	@Override
	@Nullable
	protected String[] get(Event event) {
		String[] strings = this.strings.getArray(event);
		String delimiter = this.delimiter != null ? this.delimiter.getSingle(event) : "";

		if (strings.length == 0 || delimiter == null)
			return new String[0];

		if (join) {
			return new String[]{StringUtils.join(strings, delimiter)};
		} else {
			if (!regex) {
				delimiter = (caseSensitivity ? "" : "(?i)") + Pattern.quote(delimiter);
			}

			return strings[0].split(delimiter, removeTrailing ? 0 : -1);
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
		if (join)
			return "join " + strings.toString(event, debug) +
				(delimiter != null ? " with " + delimiter.toString(event, debug) : "");
		return (regex ? "regex " : "") +
			"split " + strings.toString(event, debug) +
			(delimiter != null ? " at " + delimiter.toString(event, debug) : "") +
			(regex ? "" : "(case sensitive: " + caseSensitivity + ")") +
			(removeTrailing ? " without trailing string" : "");
	}

}
