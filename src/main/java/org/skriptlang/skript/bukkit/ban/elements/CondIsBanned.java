package org.skriptlang.skript.bukkit.ban.elements;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.*;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;
import org.skriptlang.skript.util.Priority;

import java.net.InetSocketAddress;

@Name("Is Banned")
@Description("Checks whether a player or IP is banned.")
@Example("player is banned")
@Example("victim is not IP-banned")
@Example("\"127.0.0.1\" is banned")
@Since("1.4")
public class CondIsBanned extends PropertyCondition<Object> {

	public static void register(SyntaxRegistry registry) {
		registry.register(
			SyntaxRegistry.CONDITION,
			SyntaxInfo.builder(CondIsBanned.class)
				.addPatterns(
					"%offlineplayers/strings% (is|are) banned",
					"%players/strings% (is|are) IP(-| |)banned",
					"%offlineplayers/strings% (isn't|is not|aren't|are not) banned",
					"%players/strings% (isn't|is not|aren't|are not) IP(-| |)banned"
				)
				.priority(Priority.base())
				.build()
		);
	}
	
	private boolean ipBanned;

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		setExpr(expressions[0]);
		setNegated(matchedPattern >= 2);
		ipBanned = matchedPattern % 2 != 0;
		return true;
	}
	
	@Override
	public boolean check(Object obj) {
		if (obj instanceof Player player) {
			if (ipBanned) {
				InetSocketAddress sockAddr = player.getAddress();
				if (sockAddr == null) {
					return false; // Assume not banned, they've never played here
				}
				return Bukkit.getIPBans().contains(sockAddr.getAddress().getHostAddress());
			} else {
				return player.isBanned();
			}
		} else if (obj instanceof OfflinePlayer offlinePlayer) {
			return offlinePlayer.isBanned();
		} else if (obj instanceof String ipOrPlayerName) {
			if (ipBanned) {
				return Bukkit.getIPBans().contains(ipOrPlayerName);
			} else {
				return Bukkit.getBannedPlayers().stream()
					.anyMatch(bannedPlayer -> bannedPlayer != null && ipOrPlayerName.equals(bannedPlayer.getName()));
			}
		}
		assert false;
		return false;
	}
	
	@Override
	protected String getPropertyName() {
		return ipBanned ? "IP-banned" : "banned";
	}
	
}
