package ch.njol.skript.test.runner;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.NoDoc;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@NoDoc
public class ExprTestPlayer extends SimpleExpression<OfflinePlayer> {

	private static final OfflinePlayer PLAYER = Bukkit.getOfflinePlayer("Notch");

	static {
		if (TestMode.ENABLED) {
			Skript.registerExpression(ExprTestPlayer.class, OfflinePlayer.class, ExpressionType.SIMPLE,
				"[the] test(-| )player");
		}
	}

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		return true;
	}

	@Override
	protected OfflinePlayer @Nullable [] get(Event event) {
		return new OfflinePlayer[] {PLAYER};
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
		return "the test player";
	}

}
