package org.skriptlang.skript.bukkit.entity.player.elements.effects;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.skript.util.Timespan;
import ch.njol.skript.util.Timespan.TimePeriod;
import ch.njol.util.Kleenean;
import net.kyori.adventure.text.Component;
import org.bukkit.BanList.Type;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.text.TextComponentParser;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

import java.net.InetSocketAddress;
import java.util.Date;

@Name("Ban")
@Description({"Bans or unbans a player or an IP address.",
	"If a reason is given, it will be shown to the player when they try to join the server while banned.",
	"A length of ban may also be given to apply a temporary ban. If it is absent for any reason, a permanent ban will be used instead.",
	"We recommend that you test your scripts so that no accidental permanent bans are applied.",
	"",
	"Note that banning people does not kick them from the server.",
	"You can optionally use 'and kick' or consider using the <a href='#EffKick'>kick effect</a> after applying a ban."})
@Example("unban player")
@Example("ban \"127.0.0.1\"")
@Example("IP-ban the player because \"he is an idiot\"")
@Example("ban player due to \"inappropriate language\" for 2 days")
@Example("ban and kick player due to \"inappropriate language\" for 2 days")
@Since("1.4, 2.1.1 (ban reason), 2.5 (timespan), 2.9.0 (kick)")
public class EffBan extends Effect {

	private static final String SKRIPT_BAN_SOURCE = "Skript ban effect";

	public static void register(SyntaxRegistry syntaxRegistry) {
		syntaxRegistry.register(SyntaxRegistry.EFFECT, SyntaxInfo.builder(EffBan.class)
			.supplier(EffBan::new)
			.addPatterns("ban [kick:and kick] %strings/offlineplayers% [(by reason of|because [of]|on account of|due to) %-textcomponent%] [for %-timespan%]",
				"unban %strings/offlineplayers%",
				"ban [kick:and kick] %players% by IP [(by reason of|because [of]|on account of|due to) %-textcomponent%] [for %-timespan%]",
				"unban %players% by IP",
				"IP(-| )ban [kick:and kick] %players% [(by reason of|because [of]|on account of|due to) %-textcomponent%] [for %-timespan%]",
				"(IP(-| )unban|un[-]IP[-]ban) %players%")
			.build());
	}

	private Expression<?> players;
	private @Nullable Expression<Component> reason;
	private @Nullable Expression<Timespan> expires;

	private boolean ban;
	private boolean ipBan;
	private boolean kick;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		players = exprs[0];
		reason = exprs.length > 1 ? (Expression<Component>) exprs[1] : null;
		expires = exprs.length > 1 ? (Expression<Timespan>) exprs[2] : null;
		ban = matchedPattern % 2 == 0;
		ipBan = matchedPattern >= 2;
		kick = parseResult.hasTag("kick");
		return true;
	}

	@Override
	protected void execute(Event event) {
		Component reason = this.reason == null ? null : this.reason.getSingle(event);
		Timespan duration = this.expires == null ? null : this.expires.getSingle(event);
		Date expires = duration == null ? null : new Date(System.currentTimeMillis() + duration.getAs(TimePeriod.MILLISECOND));
		for (Object object : players.getArray(event)) {
			switch (object) {
				case Player player -> {
					if (ipBan) {
						InetSocketAddress address = player.getAddress();
						if (address == null) { // Can't ban unknown IP
							return;
						}
						String ip = address.getAddress().getHostAddress();
						var banList = Bukkit.getBanList(Type.IP);
						if (ban) {
							banList.addBan(ip, toLegacyString(reason), expires, SKRIPT_BAN_SOURCE);
						} else {
							banList.pardon(ip);
						}
					} else {
						var banList = Bukkit.getBanList(Type.NAME);
						if (ban) {
							banList.addBan(player.getName(), toLegacyString(reason), expires, SKRIPT_BAN_SOURCE); // FIXME [UUID] ban UUID
						} else {
							banList.pardon(player.getName());
						}
					}
					if (kick) {
						player.kick(reason);
					}
				}
				case OfflinePlayer offlinePlayer -> {
					String name = offlinePlayer.getName();
					if (name == null) { // Can't ban, name unknown
						return;
					}
					var banList = Bukkit.getBanList(Type.NAME);
					if (ban) {
						banList.addBan(name, toLegacyString(reason), expires, SKRIPT_BAN_SOURCE);
					} else {
						banList.pardon(name);
					}
				}
				case String ip -> {
					var ipBanList = Bukkit.getBanList(Type.IP);
					var nameBanList = Bukkit.getBanList(Type.NAME);
					if (ban) {
						String legacyReason = toLegacyString(reason);
						ipBanList.addBan(ip, legacyReason, expires, SKRIPT_BAN_SOURCE);
						nameBanList.addBan(ip, legacyReason, expires, SKRIPT_BAN_SOURCE);
					} else {
						ipBanList.pardon(ip);
						nameBanList.pardon(ip);
					}
				}
				default -> throw new IllegalStateException("Unexpected value: " + object);
			}
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return new SyntaxStringBuilder(event, debug)
			.appendIf(ipBan, "IP")
			.append(ban ? "ban" : "unban")
			.appendIf(kick, "and kick")
			.append(players)
			.appendIf(reason != null, "on account of", reason)
			.appendIf(expires != null, "for", expires)
			.toString();
	}

	private static @Nullable String toLegacyString(@Nullable Component component) {
		if (component == null) {
			return null;
		}
		return TextComponentParser.instance().toLegacyString(component);
	}

}
