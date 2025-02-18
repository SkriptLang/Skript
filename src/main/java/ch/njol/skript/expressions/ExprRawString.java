package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionList;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.VariableString;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.util.SkriptColor;
import ch.njol.util.Kleenean;
import ch.njol.util.StringUtils;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Name("Raw String")
@Description("Returns the string without formatting (colors etc.) and without stripping them from it, " +
	"e.g. <code>raw \"&aHello There!\"</code> would output <code>&aHello There!</code>")
@Examples("send raw \"&aThis text is unformatted!\" to all players")
@Since("2.7")
public class ExprRawString extends SimpleExpression<String> {

	private static final Pattern HEX_PATTERN = Pattern.compile("(?i)&x((?:&\\p{XDigit}){6})");

	static {
		Skript.registerExpression(ExprRawString.class, String.class, ExpressionType.COMBINED, "raw %strings%");
	}

	@SuppressWarnings("NotNullFieldNotInitialized")
	private Expression<String> expr;
	@SuppressWarnings("NotNullFieldNotInitialized")
	private Expression<? extends String>[] messages;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		expr = (Expression<String>) exprs[0];
		messages = expr instanceof ExpressionList<?> ?
			((ExpressionList<String>) expr).getExpressions() : new Expression[]{expr};
		for (Expression<? extends String> message : messages) {
			if (message instanceof ExprColoured) {
				Skript.error("The 'colored' expression may not be used in a 'raw string' expression");
				return false;
			}
		}
		return true;
	}

	@Override
	protected String[] get(Event event) {
		List<String> strings = new ArrayList<>();
		for (Expression<? extends String> message : messages) {
			if (message instanceof VariableString) {
				strings.add(((VariableString) message).toUnformattedString(event));
				continue;
			}
			for (String string : message.getArray(event)) {
				String raw = SkriptColor.replaceColorChar(string);
				if (raw.toLowerCase().contains("&x")) {
					raw = StringUtils.replaceAll(raw, HEX_PATTERN, matchResult ->
						"<#" + matchResult.group(1).replace("&", "") + '>');
				}
				strings.add(raw);
			}
		}
		return strings.toArray(new String[0]);
	}

	@Override
	public boolean isSingle() {
		return expr.isSingle();
	}

	@Override
	public Class<? extends String> getReturnType() {
		return String.class;
	}

	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return "raw " + expr.toString(e, debug);
	}

}
