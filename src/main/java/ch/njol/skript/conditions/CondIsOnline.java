package ch.njol.skript.conditions;

import ch.njol.skript.Skript;
import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.OfflinePlayer;

@Name("Be Present in the Realm")
@Description(
	"Doth ascertain whether a player be present within the realm. The 'connected' pattern shall yield false once said player departeth the server, "
		+ "even shouldst they return anon. Be warned that employing the 'connected' pattern with a variable shall not possess this special comportment. "
		+ "Employ the direct event-player or other non-variable expression for truest results."
)
@Example("player is online")
@Example("player-argument is offline")
@Example("""
	while player is connected:
		wait 60 seconds
		send "hello!" to player
	""")
@Example("""
	# The following will act like `{_player} is online`.
	# Using variables with `is connected` will not behave the same as with non-variables.
	while {_player} is connected:
		broadcast "online!"
		wait 1 tick
	""")
@Since("1.4")
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
