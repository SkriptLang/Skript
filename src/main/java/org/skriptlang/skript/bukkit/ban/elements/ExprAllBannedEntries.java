package org.skriptlang.skript.bukkit.ban.elements;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;
import org.skriptlang.skript.util.Priority;

@Name("All Banned Players/IPs")
@Description("Obtains the list of all banned players or IP addresses.")
@Examples({
	"command /banlist:",
	"\ttrigger:",
	"\t\tsend all the banned players"
})
@Since("2.7")
public class ExprAllBannedEntries extends SimpleExpression<Object> {

	public static void register(SyntaxRegistry registry) {
		registry.register(
			SyntaxRegistry.EXPRESSION,
			SyntaxInfo.Expression.builder(ExprAllBannedEntries.class, Object.class)
				.addPatterns("[all [[of] the]|the] banned (players|ips:(ips|ip addresses))")
				.priority(Priority.base())
				.supplier(ExprAllBannedEntries::new)
				.build()
		);
	}

	private boolean ip;

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
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
