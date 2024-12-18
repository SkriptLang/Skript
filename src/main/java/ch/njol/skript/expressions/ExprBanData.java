package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer;
import ch.njol.skript.doc.*;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
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
@Examples({"set {_player} to offlineplayer(\"Notch\", false)",
	"set {_expiration} to the date {_player}'s ban expires",
	"set {_time.left} to difference between now and {_expiration}",
	"set {_reason} to the reason {_player} was banned",
	"send \"There is %{_time.left}% before %{_player}% gets unbanned! They were banned for '%{_reason}%'\" to player"})
@Since("INSERT VERSION")
@RequiredPlugins("Spigot 1.20.1+")
public class ExprBanData extends SimpleExpression<Object> {

	static {
		if (Skript.methodExists(BanEntry.class, "remove"))
			Skript.registerExpression(ExprBanData.class, Object.class, ExpressionType.SIMPLE,
				"[the] date %offlineplayer/string% was banned",
				"[the] date of %offlineplayer/string%'s ban",

				"[the] source of %offlineplayer/string%'s ban",

				"[the] date %offlineplayer/string%'s ban expires",
				"[the] expiration date of %offlineplayer/string%'s ban",

				"[the] reason %offlineplayer/string% was banned",
				"[the] reason for %offlineplayer/string%'s ban"
		);
	}

	private BanEntryType entryType;
	private Expression<Object> banTargetExpr; // offline player or string (ip)

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
		entryType = BanEntryType.fromInt(matchedPattern);
		banTargetExpr = (Expression<Object>) expressions[0];
		return true;
	}

	@Override
	protected Object @Nullable [] get(Event event) {
		if (banTargetExpr == null)
			return null;

		Object banTarget = banTargetExpr.getSingle(event);
		BanEntry<?> banEntry;

		if (banTarget instanceof String ipTarget)
			banEntry = getBanEntry(ipTarget);
		else if (banTarget instanceof OfflinePlayer playerTarget)
			banEntry = getBanEntry(playerTarget);
		else
			return null;

		if (banEntry == null)
			return null;

		return switch (entryType) {
			case BAN_DATE -> {
				java.util.Date creation = banEntry.getCreated();
				ch.njol.skript.util.Date skriptCreation = new ch.njol.skript.util.Date(creation.getTime());
				yield new Object[]{ skriptCreation };
			}
			case SOURCE -> new Object[]{ banEntry.getSource() };
			case EXPIRE_DATE -> {
				java.util.Date expiration = banEntry.getExpiration();
				if (expiration == null)
					yield null;
				ch.njol.skript.util.Date skriptExpiration = new ch.njol.skript.util.Date(expiration.getTime());
				yield new Object[]{ skriptExpiration };
			}
			case REASON -> new Object[]{ banEntry.getReason() };
		};
	}

	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public Class<?> getReturnType() {
		return Object.class;
	}

	@Override
	public Class<?> @Nullable [] acceptChange(Changer.ChangeMode mode) {
		if (mode == Changer.ChangeMode.SET) {
			return switch (entryType) {
				case EXPIRE_DATE -> new Class<?>[]{ ch.njol.skript.util.Date.class };
				case SOURCE, REASON -> new Class<?>[]{ String.class };
				default -> null;
			};
		} else if (mode == Changer.ChangeMode.ADD || mode == Changer.ChangeMode.REMOVE) {
			if (entryType == BanEntryType.EXPIRE_DATE)
				return new Class<?>[]{ Timespan.class };
		}
		return null;
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, Changer.ChangeMode mode) {
		Object banTarget = banTargetExpr.getSingle(event);
		BanEntry<?> banEntry;

		if (banTarget instanceof String ipTarget)
			banEntry = getBanEntry(ipTarget);
		else if (banTarget instanceof OfflinePlayer playerTarget)
			banEntry = getBanEntry(playerTarget);
		else
			return;

		if (banEntry == null)
			return;

		if (mode == Changer.ChangeMode.SET) {
			switch (entryType) {
				case SOURCE -> {
					String newSource = (String) delta[0];
					if (newSource == null)
						return;
					banEntry.setSource(newSource);
				}
				case EXPIRE_DATE -> {
					ch.njol.skript.util.Date newDate = (ch.njol.skript.util.Date) delta[0];
					if (newDate == null)
						return;
					banEntry.setExpiration(new java.util.Date(newDate.getTimestamp()));
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
				if (mode == Changer.ChangeMode.ADD)
					newExpirationMillis = expiration.getTime() + timespan.getAs(Timespan.TimePeriod.MILLISECOND);
				else if (mode == Changer.ChangeMode.REMOVE)
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
		return switch (entryType) {
			case BAN_DATE -> "the date " + banTargetExpr.toString(event, debug) + " was banned";
			case SOURCE -> "the source of " + banTargetExpr.toString(event, debug) + "'s ban";
			case EXPIRE_DATE -> "the date " + banTargetExpr.toString(event, debug) + "'s ban expires";
			case REASON -> "the reason " + banTargetExpr.toString(event, debug) + " was banned";
		};
	}

	private BanEntry<InetAddress> getBanEntry(String ipTarget) {
		try {
			InetAddress address = InetAddress.getByName(ipTarget);
			return Bukkit.getBanList(BanListType.IP).getBanEntry(address);
		} catch (UnknownHostException ignored) { // this only happens when you pass a url and it performs a lookup
			return null;
		}
	}

	private BanEntry<PlayerProfile> getBanEntry(OfflinePlayer playerTarget) {
		return Bukkit.getBanList(BanListType.PROFILE).getBanEntry(playerTarget.getPlayerProfile());
	}

	private enum BanEntryType {
		BAN_DATE, SOURCE, EXPIRE_DATE, REASON;

		public static BanEntryType fromInt(int value) {
			return switch (value) {
				case 0, 1 -> BAN_DATE;
				case 2 -> SOURCE;
				case 3, 4 -> EXPIRE_DATE;
				case 5, 6 -> REASON;
				default -> null;
			};
		}
	}

}
