package org.skriptlang.skript.bukkit.ban.elements;

import ch.njol.skript.doc.*;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.skript.util.Timespan;
import ch.njol.util.Kleenean;
import com.destroystokyo.paper.profile.PlayerProfile;
import io.papermc.paper.ban.BanListType;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.time.Duration;

@Name("Ban")
@Description("""
	Bans or unbans a player or an IP address.
	If a reason is given, it will be shown to the player when they try to join the server while banned.
	A length of ban may also be given to apply a temporary ban. If it is absent for any reason, a permanent ban will be used instead.
	We recommend that you test your scripts so that no accidental permanent bans are applied.
	
	Note that banning people does not kick them from the server.
	You can optionally use 'and kick' or consider using the <a href='#EffKick'>kick effect</a> after applying a ban.
	""")
@Example("unban player")
@Example("ban \"127.0.0.1\"")
@Example("IP-ban the player because \"he is an idiot\"")
@Example("ban player due to \"inappropriate language\" for 2 days")
@Example("ban and kick player due to \"inappropriate language\" for 2 days")
@Since("1.4, 2.1.1 (ban reason), 2.5 (timespan), 2.9.0 (kick)")
public class EffBan extends Effect {

	public static void register(SyntaxRegistry registry) {
		registry.register(
			SyntaxRegistry.EFFECT,
			SyntaxInfo.builder(EffBan.class)
				.addPatterns(
					"ban [kick:and kick] %strings/offlineplayers% [(by reason of|because [of]|on account of|due to) %-string%] [for %-timespan%] [(using|with) source %-string%]",
					"unban %strings/offlineplayers%",
					"ban [kick:and kick] %players% by IP [(by reason of|because [of]|on account of|due to) %-string%] [for %-timespan%] [(using|with) source %-string%]",
					"unban %players% by IP",
					"IP(-| )ban [kick:and kick] %players% [(by reason of|because [of]|on account of|due to) %-string%] [for %-timespan%] [(using|with) source %-string%]",
					"(IP(-| )unban|un[-]IP[-]ban) %players%"
				)
				.supplier(EffBan::new)
				.build()
		);
	}

	private Expression<?> targets;
	private @Nullable Expression<String> reason;
	private @Nullable Expression<Timespan> duration;
	private @Nullable Expression<String> source;

	/** Whether to ban or unban */
	private boolean ban;
	/** Whether the ban is an IP ban */
	private boolean ipBan;
	/** Kick after banning? */
	private boolean kick;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		targets = expressions[0];

		if (expressions.length > 1) {
			reason = (Expression<String>) expressions[1];
			duration = (Expression<Timespan>) expressions[2];
			source = (Expression<String>) expressions[3];
		}

		ban = matchedPattern % 2 == 0;
		ipBan = matchedPattern >= 2;
		kick = parseResult.hasTag("kick");
		return true;
	}

	@Override
	protected void execute(Event event) {
		String reason = this.reason != null ? this.reason.getSingle(event) : null;

		Duration duration = null;
		Timespan timespan = this.duration != null ? this.duration.getSingle(event) : null;
		if (timespan != null) {
			duration = timespan.getDuration();
		}

		String source = "Skript";
		if (this.source != null) {
			String customSource = this.source.getSingle(event);
			source = customSource != null ? customSource : source;
		}

		for (Object target : targets.getArray(event)) {
			if (target instanceof Player player) {
				if (ipBan) {
					InetSocketAddress addr = player.getAddress();
					if (addr == null) {
						continue; // Can't ban unknown IP
					}
					InetAddress ip = addr.getAddress();
					BanList<InetAddress> banList = Bukkit.getBanList(BanListType.IP);
					if (ban) {
						banList.addBan(ip, reason, duration, source);
					} else {
						banList.pardon(ip);
					}
				} else {
					BanList<PlayerProfile> banList = Bukkit.getBanList(BanListType.PROFILE);
					if (ban) {
						banList.addBan(player.getPlayerProfile(), reason, duration, source);
					} else {
						banList.pardon(player.getPlayerProfile());
					}
					if (kick) {
						player.kickPlayer(reason);
					}
				}
			} else if (target instanceof OfflinePlayer offlinePlayer) {
				BanList<PlayerProfile> banList = Bukkit.getBanList(BanListType.PROFILE);
				if (ban) {
					banList.addBan(offlinePlayer.getPlayerProfile(), reason, duration, source);
				} else {
					banList.pardon(offlinePlayer.getPlayerProfile());
				}
			} else if (target instanceof String ip) {
				InetAddress address;
				try {
					address = InetAddress.getByName(ip);
				} catch (UnknownHostException e) {
					continue; // this only happens when you pass a url and it performs a lookup
				}
				BanList<InetAddress> banList = Bukkit.getBanList(BanListType.IP);
				if (ban) {
					banList.addBan(address, reason, duration, source);
				} else {
					banList.pardon(address);
				}
			} else {
				assert false;
			}
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return new SyntaxStringBuilder(event, debug)
			.appendIf(ipBan, "IP")
			.append(ban ? "ban" : "unban")
			.appendIf(kick, "and kick")
			.append(targets)
			.appendIf(reason != null, reason)
			.appendIf(duration != null, "for", duration)
			.toString();
	}

}
