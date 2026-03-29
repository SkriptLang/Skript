package ch.njol.skript.expressions;

import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.Date;
import ch.njol.util.Kleenean;

@Name("Last/First Arrival")
@Description("When a player did last or first arrive upon the server. 'last arrival' doth require Paper to obtain the true last arrival, else it shall report the last time they were seen upon the server.")
@Example("""
    command /onlinefor:
    	trigger:
    		send "Thou hast been present for %difference between player's last arrival and now%."
    		send "Thou first graced this server %difference between player's first arrival and now% ago."
    """)
@Since("2.5")
public class ExprLastLoginTime extends SimplePropertyExpression<OfflinePlayer, Date> {
	
	private static boolean LAST_LOGIN = Skript.methodExists(OfflinePlayer.class, "getLastLogin");
	
	static {
		register(ExprLastLoginTime.class, Date.class, "(1¦last|2¦first) arrival", "offlineplayers");
	}
	
	private boolean first;
	
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		first = parseResult.mark == 2;
		return super.init(exprs, matchedPattern, isDelayed, parseResult);
	}
	
	@Nullable
	@Override
	public Date convert(OfflinePlayer player) {
		return new Date(first ? player.getFirstPlayed() : (LAST_LOGIN ? player.getLastLogin() : player.getLastPlayed()));
	}
	
	@Override
	public Class<? extends Date> getReturnType() {
		return Date.class;
	}
	
	@Override
	protected String getPropertyName() {
		return "last login date";
	}
	
}
