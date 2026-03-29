package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("All Banished Players/IPs")
@Description("Obtaineth the ledger of all banished players or IP addresses.")
@Example("""
    command /banlist:
    	trigger:
    		send all the banished players
    """)
@Since("2.7")
public class ExprAllBannedEntries extends SimpleExpression<Object> {

	static {
		Skript.registerExpression(ExprAllBannedEntries.class, Object.class, ExpressionType.SIMPLE, "[all [[of] the]|the] banished (players|ips:(ips|ip addresses))");
	}

	private boolean ip;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		ip = parseResult.hasTag("ips");
		return true;
	}

	@Override
	@Nullable
	protected Object[] get(Event event) {
		if (ip)
			return Bukkit.getIPBans().toArray(new String[0]);
		return Bukkit.getBannedPlayers().toArray(new OfflinePlayer[0]);
	}

	@Override
	public boolean isSingle() {
		return false;
	}

	@Override
	public Class<?> getReturnType() {
		return ip ? String.class : OfflinePlayer.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "all banned " + (ip ? "ip addresses" : "players");
	}

}
