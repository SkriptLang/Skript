package ch.njol.skript.expressions;

import org.bukkit.event.Event;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleLiteral;
import ch.njol.util.Kleenean;
import org.jetbrains.annotations.Nullable;

@Name("New Line")
@Description("Returns a line break separator.")
@Examples("send \"Hello%nl%Goodbye!\" to player")
@Since("2.5")
public class LitNewLine extends SimpleLiteral<String> {

	static {
		Skript.registerExpression(LitNewLine.class, String.class, ExpressionType.SIMPLE, "nl", "new[ ]line", "line[ ]break");
	}

	public LitNewLine() {
		super("\n", false);
	}

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult result) {
		return true;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "new line";
	}
}
