package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.Utils;
import ch.njol.skript.util.chat.ChatMessages;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Colored / Uncolored")
@Description({"Parses &lt;color&gt;s and, optionally, chat styles in a message or removes",
		"any colors <i>and</i> chat styles from the message. Parsing all",
		"chat styles requires this expression to be used in same line with",
		"the <a href=effects.html#EffSend>send effect</a>."})
@Examples({"on chat:",
		"	set message to colored message # Safe; only colors get parsed",
		"command /fade &lt;player&gt;:",
		"	trigger:",
		"		set display name of the player-argument to uncolored display name of the player-argument",
		"command /format &lt;text&gt;:",
		"	trigger:",
		"		message formatted text-argument # Safe, because we're sending to whoever used this command"})
@Since("2.0")
public class ExprColoured extends PropertyExpression<String, String> {
	static {
		Skript.registerExpression(ExprColoured.class, String.class, ExpressionType.COMBINED,
				"(colo[u]r-|colo[u]red )%strings%",
				"(format-|formatted )%strings%",
				"(un|non)[-](colo[u]r-|colo[u]red |format-|formatted )%strings%");
	}
	
	/**
	 * If colors should be parsed.
	 */
	boolean color;
	
	/**
	 * If all styles should be parsed whenever possible.
	 */
	boolean format;
	
	@SuppressWarnings({"unchecked", "null"})
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parseResult) {
		setExpr((Expression<? extends String>) exprs[0]);
		color = matchedPattern <= 1; // colored and formatted
		format = matchedPattern == 1;
		return true;
	}
	
	@Override
	protected String[] get(final Event e, final String[] source) {
		return get(source, s -> color ? Utils.replaceChatStyles(s) : "" + ChatMessages.stripStyles(s));
	}
	
	@Override
	public Class<? extends String> getReturnType() {
		return String.class;
	}
	
	@Override
	public String toString(final @Nullable Event e, final boolean debug) {
		return (color ? "" : "un") + "colored " + getExpr().toString(e, debug);
	}
	
	/**
	 * If parent of this expression should try to parse all styles instead of
	 * just colors. This is unsafe to do with untrusted user input.
	 * @return If unsafe formatting was requested in script.
	 */
	public boolean isUnsafeFormat() {
		return format;
	}
	
}
