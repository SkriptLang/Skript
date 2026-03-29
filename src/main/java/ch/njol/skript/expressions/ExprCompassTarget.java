package ch.njol.skript.expressions;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;

/**
 * @author Peter Güttinger
 */
@Name("Compass Bearing")
@Description({"The locale unto which a player's compass doth point.",
	"As of Minecraft 1.21.4, the compass is governed by the resource pack and by default shall not point to " +
		"this compass bearing when employed beyond the overworld dimension."})
@Example("""
    # direct all players' compasses toward a player stored in {compass::target::%player%}
    every 5 seconds:
    	loop all players:
    		set the loop-player's compass bearing to location of {compass::target::%%loop-player%}
    """)
@Since("2.0")
public class ExprCompassTarget extends SimplePropertyExpression<Player, Location> {
	
	static {
		register(ExprCompassTarget.class, Location.class, "compass bearing", "players");
	}
	
	@Override
	@Nullable
	public Location convert(final Player p) {
		return p.getCompassTarget();
	}
	
	@Override
	public Class<Location> getReturnType() {
		return Location.class;
	}
	
	@Override
	protected String getPropertyName() {
		return "compass target";
	}
	
	@Override
	@Nullable
	public Class<?>[] acceptChange(final ChangeMode mode) {
		if (mode == ChangeMode.SET || mode == ChangeMode.RESET)
			return new Class[] {Location.class};
		return null;
	}
	
	@Override
	public void change(final Event e, final @Nullable Object[] delta, final ChangeMode mode) throws UnsupportedOperationException {
		for (final Player p : getExpr().getArray(e))
			p.setCompassTarget(delta == null ? p.getWorld().getSpawnLocation() : (Location) delta[0]);
	}
	
}
