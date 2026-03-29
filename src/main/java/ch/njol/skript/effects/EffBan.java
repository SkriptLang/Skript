package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
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

@Name("Banishment")
@Description({"Doth banish or pardon a player or an IP address.",
	"If a reason be given, it shall be shown to the player when they attempt to join the server whilst banished.",
	"A duration of banishment may also be given to impose a temporary exile. If it be absent for any reason, a permanent banishment shall be imposed instead.",
	"We do counsel that thou test thy scripts so that no accidental permanent banishments be imposed.",
	"",
	"Mark well that banishing people doth not cast them out from the server.",
	"Thou mayest optionally employ 'and cast out' or consider using the <a href='#EffKick'>cast out effect</a> after imposing a banishment."})
@Example("pardon player")
@Example("banish \"127.0.0.1\"")
@Example("IP-banish the player because \"he is a knave\"")
@Example("banish player due to \"inappropriate tongue\" for 2 days")
@Example("banish and cast out player due to \"inappropriate tongue\" for 2 days")
@Since("1.4, 2.1.1 (ban reason), 2.5 (timespan), 2.9.0 (kick)")
public class EffBan extends Effect {

	static {
		Skript.registerEffect(EffBan.class,
			"banish [kick:and cast out] %strings/offlineplayers% [(by reason of|because [of]|on account of|due to) %-string%] [for %-timespan%]",
			"pardon %strings/offlineplayers%",
			"banish [kick:and cast out] %players% by IP [(by reason of|because [of]|on account of|due to) %-string%] [for %-timespan%]",
			"pardon %players% by IP",
			"IP(-| )banish [kick:and cast out] %players% [(by reason of|because [of]|on account of|due to) %-string%] [for %-timespan%]",
			"(IP(-| )pardon|un[-]IP[-]banish) %players%");
	}

	@SuppressWarnings("null")
	private Expression<?> players;
	@Nullable
	private Expression<String> reason;
	@Nullable
	private Expression<Timespan> expires;

	private boolean ban;
	private boolean ipBan;
	private boolean kick;

	@SuppressWarnings({"null", "unchecked"})
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parseResult) {
		players = exprs[0];
		reason = exprs.length > 1 ? (Expression<String>) exprs[1] : null;
		expires = exprs.length > 1 ? (Expression<Timespan>) exprs[2] : null;
		ban = matchedPattern % 2 == 0;
		ipBan = matchedPattern >= 2;
		kick = parseResult.hasTag("kick");
		return true;
	}

	@SuppressWarnings("null")
	@Override
	protected void execute(final Event e) {
		final String reason = this.reason != null ? this.reason.getSingle(e) : null; // don't check for null, just ignore an invalid reason
		Timespan ts = this.expires != null ? this.expires.getSingle(e) : null;
		final Date expires = ts != null ? new Date(System.currentTimeMillis() + ts.getAs(Timespan.TimePeriod.MILLISECOND)) : null;
		final String source = "Skript ban effect";
		for (final Object o : players.getArray(e)) {
			if (o instanceof Player) {
				Player player = (Player) o;
				if (ipBan) {
					InetSocketAddress addr = player.getAddress();
					if (addr == null)
						return; // Can't ban unknown IP
					final String ip = addr.getAddress().getHostAddress();
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
			} else if (o instanceof OfflinePlayer) {
				String name = ((OfflinePlayer) o).getName();
				if (name == null)
					return; // Can't ban, name unknown
				if (ban)
					Bukkit.getBanList(BanList.Type.NAME).addBan(name, reason, expires, source);
				else
					Bukkit.getBanList(BanList.Type.NAME).pardon(name);
			} else if (o instanceof String) {
				final String s = (String) o;
				if (ban) {
					Bukkit.getBanList(BanList.Type.IP).addBan(s, reason, expires, source);
					Bukkit.getBanList(BanList.Type.NAME).addBan(s, reason, expires, source);
				} else {
					Bukkit.getBanList(BanList.Type.IP).pardon(s);
					Bukkit.getBanList(BanList.Type.NAME).pardon(s);
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
