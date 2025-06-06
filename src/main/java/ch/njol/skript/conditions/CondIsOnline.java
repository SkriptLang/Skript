package ch.njol.skript.conditions;

import ch.njol.skript.Skript;
import org.bukkit.OfflinePlayer;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;

@Name("Is Online")
@Description(
	"Checks whether a player is online. The 'connected' pattern will return false once this player leaves the server, "
		+ "even if they rejoin. Be aware that using the 'connected' pattern with a variable will not have this special behavior. "
		+ "Use the direct event-player or other non-variable expression for best results."
)
@Examples({
	"player is online",
	"player-argument is offline",
	"while player is connected:",
		"\twait 60 seconds",
		"\tsend \"hello!\" to player",
	"",
	"# The following will act like `{_player} is online`.",
	"# Using variables with `is connected` will not behave the same as with non-variables.",
	"while {_player} is connected:",
	    "\tbroadcast \"online!\"",
	    "\twait 1 tick"
})
@Since("1.4")
@RequiredPlugins("Paper 1.20+ (Connected)")
public class CondIsOnline extends PropertyCondition<OfflinePlayer> {
	
	static {
		if (Skript.methodExists(OfflinePlayer.class, "isConnected"))
			register(CondIsOnline.class, "(online|:offline|:connected)", "offlineplayers");
		else
			register(CondIsOnline.class, "(online|:offline)", "offlineplayers");
	}
	
	private boolean connected; // https://github.com/SkriptLang/Skript/issues/6100
	
	@SuppressWarnings({"unchecked"})
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		this.setExpr((Expression<OfflinePlayer>) exprs[0]);
		this.setNegated(matchedPattern == 1 ^ parseResult.hasTag("offline"));
		this.connected = parseResult.hasTag("connected");
		return true;
	}
	
	@Override
	public boolean check(OfflinePlayer op) {
		if (connected)
			return op.isConnected();
		return op.isOnline();
	}
	
	@Override
	protected String getPropertyName() {
		return connected ? "connected" : "online";
	}
	
}
