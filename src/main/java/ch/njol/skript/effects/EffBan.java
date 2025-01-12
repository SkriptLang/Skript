package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.skript.util.Timespan;
import ch.njol.util.Kleenean;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import java.net.InetSocketAddress;
import java.util.Date;

@Name("Ban")
@Description({
	"Bans or unbans a player or an IP address.",
	"If a reason is given, it will be shown to the player when they try to join the server while banned.",
	"A length of ban may also be given to apply a temporary ban. If it is absent for any reason, a permanent ban will be used instead.",
	"We recommend that you test your scripts so that no accidental permanent bans are applied.",
	"",
	"Note that banning people does not kick them from the server.",
	"You can optionally use 'and kick' or consider using the <a href='effects.html#EffKick'>kick effect</a> after applying a ban."
})
@Examples({
	"unban player",
	"ban \"127.0.0.1\"",
	"IP-ban the player because \"he is an idiot\"",
	"ban player due to \"inappropriate language\" for 2 days",
	"ban and kick player due to \"inappropriate language\" for 2 days"
})
@Since({
	"1.4",
	"2.1.1 (ban reason)",
	"2.5 (timespan)",
	"2.9.0 (kick)"
})
public class EffBan extends Effect {

	static {
		Skript.registerEffect(EffBan.class,
			"ban [kick:and kick] %strings/offlineplayers% [(by reason of|because [of]|on account of|due to) %-string%] [for %-timespan%]",
			"unban %strings/offlineplayers%",
			"ban [kick:and kick] %players% by IP [(by reason of|because [of]|on account of|due to) %-string%] [for %-timespan%]",
			"unban %players% by IP",
			"IP(-| )ban [kick:and kick] %players% [(by reason of|because [of]|on account of|due to) %-string%] [for %-timespan%]",
			"(IP(-| )unban|un[-]IP[-]ban) %players%");
	}

	private Expression<?> players;
	private @Nullable Expression<String> reason;
	private @Nullable Expression<Timespan> expires;
	private boolean ban, ipBan, kick;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		players = exprs[0];
		reason = exprs.length > 1 ? (Expression<String>) exprs[1] : null;
		expires = exprs.length > 1 ? (Expression<Timespan>) exprs[2] : null;
		ban = matchedPattern % 2 == 0;
		ipBan = matchedPattern >= 2;
		kick = parseResult.hasTag("kick");
		return true;
	}

	@Override
	protected void execute(Event event) {
		String reason = this.reason != null ? this.reason.getSingle(event) : null; // don't check for null, just ignore an invalid reason
		Timespan timespan = this.expires != null ? this.expires.getSingle(event) : null;
		Date expires = timespan != null ? new Date(System.currentTimeMillis() + timespan.getAs(Timespan.TimePeriod.MILLISECOND)) : null;
		String source = "Skript ban effect";
		for (Object object : players.getArray(event)) {
			if (object instanceof Player player) {
				if (ipBan) {
					InetSocketAddress addr = player.getAddress();
					if (addr == null)
						return; // Can't ban unknown IP
					String ip = addr.getAddress().getHostAddress();
					if (ban)
						Bukkit.getBanList(BanList.Type.IP).addBan(ip, reason, expires, source);
					else
						Bukkit.getBanList(BanList.Type.IP).pardon(ip);
				} else {
					if (ban)
						Bukkit.getBanList(BanList.Type.NAME).addBan(player.getName(), reason, expires, source); // FIXME [UUID] ban UUID
					else
						Bukkit.getBanList(BanList.Type.NAME).pardon(player.getName());
				}
				if (kick)
					player.kickPlayer(reason);
			} else if (object instanceof OfflinePlayer offlinePlayer) {
				String name = offlinePlayer.getName();
				if (name == null)
					return; // Can't ban, name unknown
				if (ban)
					Bukkit.getBanList(BanList.Type.NAME).addBan(name, reason, expires, source);
				else
					Bukkit.getBanList(BanList.Type.NAME).pardon(name);
			} else if (object instanceof String string) {
				if (ban) {
					Bukkit.getBanList(BanList.Type.IP).addBan(string, reason, expires, source);
					Bukkit.getBanList(BanList.Type.NAME).addBan(string, reason, expires, source);
				} else {
					Bukkit.getBanList(BanList.Type.IP).pardon(string);
					Bukkit.getBanList(BanList.Type.NAME).pardon(string);
				}
			} else {
				assert false;
			}
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		SyntaxStringBuilder builder = new SyntaxStringBuilder(event, debug);

		if (ipBan)
			builder.append("IP");
		builder.append(ban ? "ban" : "unban");
		if (kick)
			builder.append("and kick");
		builder.append(players);
		if (reason != null)
			builder.append("on account of", reason);
		if (expires != null)
			builder.append("for", expires);

		return builder.toString();
	}

}
