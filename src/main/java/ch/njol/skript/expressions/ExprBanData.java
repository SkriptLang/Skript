package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.*;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.util.Date;
import ch.njol.skript.util.Timespan;
import ch.njol.util.Kleenean;
import com.destroystokyo.paper.profile.PlayerProfile;
import io.papermc.paper.ban.BanListType;
import org.bukkit.BanEntry;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import java.net.InetAddress;
import java.net.UnknownHostException;

@Name("Ban Details")
@Description("Returns data about a player or IP ban. " +
	"The source can be any string, but it's usually the player's name. " +
	"The expiration date won't return a value if the ban is permanent.")
@Examples({
	"set {_player} to offlineplayer(\"Notch\", false)",
	"set {_expiration} to the date {_player}'s ban expires",
	"set {_time.left} to difference between now and {_expiration}",
	"set {_reason} to the reason {_player} was banned",
	"send \"There is %{_time.left}% before %{_player}% gets unbanned! They were banned for '%{_reason}%'\" to player"
})
@Since("INSERT VERSION")
@RequiredPlugins("Spigot 1.20.1+")
public class ExprBanData extends SimpleExpression<Object> {

	static {
		if (Skript.methodExists(BanEntry.class, "remove"))
			Skript.registerExpression(ExprBanData.class, Object.class, ExpressionType.COMBINED,
				"[the] date %offlineplayer/string% was banned",
				"[the] date of %offlineplayer/string%'s ban",
				"[the] ban date of %offlineplayer/string%",

				"[the] source of %offlineplayer/string%'s ban",
				"[the] ban source of %offlineplayer/string%",

				"[the] date %offlineplayer/string%'s ban expires",
				"[the] expiration date of %offlineplayer/string%'s ban",
				"[the] ban expiration date of %offlineplayer/string%",

				"[the] reason %offlineplayer/string% was banned",
				"[the] reason for %offlineplayer/string%'s ban",
				"[the] ban reason of %offlineplayer/string%"
		);
	}

	private BanEntryType entryType;
	private Expression<Object> banTarget; // offline player or string (ip)

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		entryType = switch (matchedPattern) {
			case 0, 1, 2 -> BanEntryType.BAN_DATE;
			case 3, 4 -> BanEntryType.SOURCE;
			case 5, 6, 7 -> BanEntryType.EXPIRE_DATE;
			case 8, 9, 10 -> BanEntryType.REASON;
			default -> null;
		};
		banTarget = (Expression<Object>) expressions[0];
		return true;
	}

	@Override
	protected Object @Nullable [] get(Event event) {
		if (banTarget == null)
			return null;

		Object target = banTarget.getSingle(event);
		BanEntry<?> banEntry;

		if (target instanceof String ipTarget) {
			banEntry = getBanEntry(ipTarget);
		} else if (target instanceof OfflinePlayer playerTarget) {
			banEntry = getBanEntry(playerTarget);
		} else {
			return null;
		}

		if (banEntry == null)
			return null;

		return switch (entryType) {
			case BAN_DATE -> {
				java.util.Date creation = banEntry.getCreated();
				Date skriptCreation = new Date(creation.getTime());
				yield new Date[]{ skriptCreation };
			}
			case SOURCE -> new String[]{ banEntry.getSource() };
			case EXPIRE_DATE -> {
				java.util.Date expiration = banEntry.getExpiration();
				if (expiration == null)
					yield null;
				Date skriptExpiration = new Date(expiration.getTime());
				yield new Date[]{ skriptExpiration };
			}
			case REASON -> new String[]{ banEntry.getReason() };
		};
	}

	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public Class<?> getReturnType() {
		return switch (entryType) {
			case BAN_DATE, EXPIRE_DATE -> Date.class;
			case SOURCE, REASON -> String.class;
		};
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		if (mode == ChangeMode.SET) {
			return switch (entryType) {
				case EXPIRE_DATE -> new Class<?>[]{ Date.class };
				case SOURCE, REASON -> new Class<?>[]{ String.class };
				default -> null;
			};
		} else if (mode == ChangeMode.ADD || mode == ChangeMode.REMOVE) {
			if (entryType == BanEntryType.EXPIRE_DATE)
				return new Class<?>[]{ Timespan.class };
		}
		return null;
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		Object target = banTarget.getSingle(event);
		BanEntry<?> banEntry;

		if (target instanceof String ipTarget) {
			banEntry = getBanEntry(ipTarget);
		} else if (target instanceof OfflinePlayer playerTarget) {
			banEntry = getBanEntry(playerTarget);
		} else {
			return;
		}

		if (banEntry == null)
			return;

		assert delta != null;
		if (mode == ChangeMode.SET) {
			switch (entryType) {
				case SOURCE -> {
					String newSource = (String) delta[0];
					banEntry.setSource(newSource);
				}
				case EXPIRE_DATE -> {
					Date newDate = (Date) delta[0];
					if (newDate == null)
						return;
					banEntry.setExpiration(new java.util.Date(newDate.getTime()));
				}
				case REASON -> {
					String newReason = (String) delta[0];
					if (newReason == null)
						return;
					banEntry.setReason(newReason);
				}
			}
		} else {
			Timespan timespan = (Timespan) delta[0];
			if (timespan == null)
				return;

			if (entryType == BanEntryType.EXPIRE_DATE) {
				java.util.Date expiration = banEntry.getExpiration();
				if (expiration == null)
					return;
				long newExpirationMillis;
				if (mode == ChangeMode.ADD)
					newExpirationMillis = expiration.getTime() + timespan.getAs(Timespan.TimePeriod.MILLISECOND);
				else if (mode == ChangeMode.REMOVE)
					newExpirationMillis = expiration.getTime() - timespan.getAs(Timespan.TimePeriod.MILLISECOND);
				else
					return;
				banEntry.setExpiration(new java.util.Date(newExpirationMillis));
			}
		}
		banEntry.save();
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		String target = banTarget.toString(event, debug);
		return switch (entryType) {
			case BAN_DATE -> "the date " + target + " was banned";
			case SOURCE -> "the source of " + target + "'s ban";
			case EXPIRE_DATE -> "the date " + target + "'s ban expires";
			case REASON -> "the reason " + target + " was banned";
		};
	}

	private static BanEntry<InetAddress> getBanEntry(String ipTarget) {
		try {
			InetAddress address = InetAddress.getByName(ipTarget);
			return Bukkit.getBanList(BanListType.IP).getBanEntry(address);
		} catch (UnknownHostException ignored) {} // this only happens when you pass a url and it performs a lookup
		return null;
	}

	private static BanEntry<PlayerProfile> getBanEntry(OfflinePlayer playerTarget) {
		return Bukkit.getBanList(BanListType.PROFILE).getBanEntry(playerTarget.getPlayerProfile());
	}

	protected enum BanEntryType {
		BAN_DATE, SOURCE, EXPIRE_DATE, REASON
	}

}
