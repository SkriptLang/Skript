package org.skriptlang.skript.bukkit.test.elements.expressions;

import ch.njol.skript.doc.NoDoc;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import org.skriptlang.skript.bukkit.test.util.TestOfflinePlayer;
import ch.njol.util.Kleenean;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

@NoDoc
public class ExprTestOfflinePlayer extends SimpleExpression<OfflinePlayer> {

	public static void register(SyntaxRegistry syntaxRegistry) {
		syntaxRegistry.register(SyntaxRegistry.EXPRESSION,
			SyntaxInfo.Expression.simple(ExprTestOfflinePlayer.class, ExprTestOfflinePlayer::new, OfflinePlayer.class,
				"[the] test(-| )offline[-| ]player"));
	}

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		return true;
	}

	@Override
	protected OfflinePlayer[] get(Event event) {
		return new OfflinePlayer[]{TestOfflinePlayer.getInstance()};
	}

	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public Class<? extends OfflinePlayer> getReturnType() {
		return OfflinePlayer.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "the test offline player";
	}

}
