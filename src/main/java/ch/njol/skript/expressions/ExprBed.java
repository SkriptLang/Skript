package ch.njol.skript.expressions;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
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

@Name("Resting Place")
@Description({
	"Returneth the resting place of a player, " +
	"that is to say, the spawn point of a player shouldst they have e'er slumbered in a bed and the bed still standeth unobstructed; howbeit, " +
	"thou canst set the unsafe resting place of players and they shall respawn there even if it hath been obstructed or existeth no more, " +
	"and that be the default comportment of this expression; otherwise thou must needs be particular, i.e. <code>safe resting place locale</code>.",
	"",
	"NOTA BENE: Offline players cannot have their resting place altered, only those who walk among us."
})
@Example("""
    if resting place of player exists:
    	teleport player the the player's resting place
    else:
    	teleport the player to the world's spawn point
    """)
@Example("""
    set the resting place locale of player to spawn location of world("world") # unsafe/improper resting place
    set the safe resting place locale of player to spawn location of world("world") # safe/proper resting place
    """)
@Since("2.0, 2.7 (offlineplayers, safe bed)")
public class ExprBed extends SimplePropertyExpression<OfflinePlayer, Location> {

	static {
		register(ExprBed.class, Location.class, "[(safe:(safe|proper)|(unsafe|improper))] resting place[s] [locale[s]]", "offlineplayers");
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
