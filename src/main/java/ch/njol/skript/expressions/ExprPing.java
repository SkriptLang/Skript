package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import org.bukkit.entity.Player;

@Name("Ping")
@Description("Pings of players, as Minecraft server knows them. Note that they will almost certainly"
		+ " be different from the ones you'd get from using ICMP echo requests."
		+ " This expression is only supported on some server software (PaperSpigot).")
@Examples({"command /ping <player=%player%>:",
			"\ttrigger:",
			"\t\tsend \"%arg-1%'s ping is %arg-1's ping%\""})
@Since("2.2-dev36")
public class ExprPing extends SimplePropertyExpression<Player, Long> {

	private static final boolean SUPPORTED = Skript.methodExists(Player.Spigot.class, "getPing");

	static {
		PropertyExpression.register(ExprPing.class, Long.class, "ping", "players");
	}

	@Override
	@SuppressWarnings({"unchecked", "null"})
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
		if (!SUPPORTED) {
			Skript.error("The ping expression is not supported on this server software.");
			return false;
		}
		setExpr((Expression<Player>) exprs[0]);
		return true;
	}

	@Override
	public Long convert(Player player) {
		return (long) player.spigot().getPing();
	}

	@Override
	public Class<? extends Long> getReturnType() {
		return Long.class;
	}

	@Override
	protected String getPropertyName() {
		return "ping";
	}

}
