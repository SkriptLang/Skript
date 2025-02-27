package ch.njol.skript.expressions;

import java.util.UUID;

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

@Name("Random UUID")
@Description("Returns a random UUID.")
@Examples("set {_uuid} to random uuid")
@Since("2.5.1, INSERT VERSION (return UUIDs)")
public class ExprRandomUUID extends SimpleExpression<UUID> {

	static {
		Skript.registerExpression(ExprRandomUUID.class, UUID.class, ExpressionType.SIMPLE, "[a] random uuid");
	}

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		return true;
	}

	@Override
	protected UUID @Nullable [] get(Event e) {
		return new UUID[]{ UUID.randomUUID() };
	}

	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public Class<? extends UUID> getReturnType() {
		return UUID.class;
	}

	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return "random uuid";
	}

}
