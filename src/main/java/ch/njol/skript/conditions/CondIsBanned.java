package ch.njol.skript.conditions;

import java.net.InetSocketAddress;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import ch.njol.skript.Skript;
import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;

/**
 * @author Peter Güttinger
 */
@Name("Is Banned")
@Description("Checks whether a player or IP is banned.")
@Examples({"player is banned",
		"victim is not IP-banned",
		"\"127.0.0.1\" is banned"})
@Since("1.4")
public class CondIsBanned extends PropertyCondition<Object> {
	
	static {
		Skript.registerCondition(CondIsBanned.class,
				"%offlineplayers/strings% (is|are) banned",
				"%players/strings% (is|are) IP(-| |)banned",
				"%offlineplayers/strings% (isn't|is not|aren't|are not) banned",
				"%players/strings% (isn't|is not|aren't|are not) IP(-| |)banned");
	}
	
	private boolean ipBanned;
	
	@SuppressWarnings("null")
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parseResult) {
		setExpr(exprs[0]);
		setNegated(matchedPattern >= 2);
		ipBanned = matchedPattern % 2 != 0;
		return true;
	}
	
	@Override
	public boolean check(Object obj) {
		if (obj instanceof Player) {
			if (ipBanned) {
				InetSocketAddress sockAddr = ((Player) obj).getAddress();
				if (sockAddr == null)
					return false; // Assume not banned, they've never played here
				return Bukkit.getIPBans().contains(sockAddr.getAddress().getHostAddress());
			} else {
				return ((Player) obj).isBanned();
			}
		} else if (obj instanceof OfflinePlayer) {
			return ((OfflinePlayer) obj).isBanned();
		} else if (obj instanceof String) {
			return Bukkit.getIPBans().contains(obj) ||
					!ipBanned && Bukkit.getBannedPlayers().stream()
							.anyMatch(offPlayer -> offPlayer != null && obj.equals(offPlayer.getName()));
		}
		assert false;
		return false;
	}
	
	@Override
	protected String getPropertyName() {
		return ipBanned ? "IP-banned" : "banned";
	}
	
}
