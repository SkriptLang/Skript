package ch.njol.skript.expressions;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Bed")
@Description({
	"Returns the bed location of a player, " +
	"i.e. the spawn point of a player if they ever slept in a bed and the bed still exists and is unobstructed however, " +
	"you can set the unsafe bed location of players and they will respawn there even if it has been obstructed or doesn't exist anymore " +
	"and that's the default behavior of this expression otherwise you will need to be specific i.e. <code>safe bed location</code>.",
	"",
	"NOTE: Offline players can not have their bed location changed, only online players."
})
@Examples({
	"if bed of player exists:",
	"\tteleport player the the player's bed",
	"else:",
	"\tteleport the player to the world's spawn point",
	"",
	"set the bed location of player to spawn location of world(\"world\") # unsafe/invalid bed location",
	"set the safe bed location of player to spawn location of world(\"world\") # safe/valid bed location"
})
@Since("2.0, 2.7 (offlineplayers, safe bed)")
public class ExprBed extends SimplePropertyExpression<OfflinePlayer, Location> {

	static {
		register(ExprBed.class, Location.class, "[(safe:(safe|valid)|(unsafe|invalid))] bed[s] [location[s]]", "offlineplayers");
	}

	private boolean isSafe;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		isSafe = parseResult.hasTag("safe");
		setExpr((Expression<? extends OfflinePlayer>) exprs[0]);
		return true;
	}

	@Override
	@Nullable
	public Location convert(OfflinePlayer p) {
		return p.getBedSpawnLocation();
	}
	
	@Override
	@Nullable
	public Class<?>[] acceptChange(ChangeMode mode) {
		return mode == ChangeMode.SET || mode == ChangeMode.DELETE ? CollectionUtils.array(Location.class) : null;
	}
	
	@Override
	public void change(Event e, @Nullable Object[] delta, ChangeMode mode) {
		Location loc = delta == null ? null : (Location) delta[0];
		for (OfflinePlayer p : getExpr().getArray(e)) {
			Player op = p.getPlayer();
			if (op != null) // is online
				op.setBedSpawnLocation(loc, !isSafe);
		}
	}
	
	@Override
	protected String getPropertyName() {
		return "bed";
	}
	
	@Override
	public Class<? extends Location> getReturnType() {
		return Location.class;
	}
	
}
