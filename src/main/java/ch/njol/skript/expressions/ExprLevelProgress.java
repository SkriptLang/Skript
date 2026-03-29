package ch.njol.skript.expressions;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Events;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.util.Math2;

/**
 * @author Peter Güttinger
 */
@Name("Station Progress")
@Description({"The player's progress toward attaining the next station; this representeth the experience bar within the game. " +
		"Pray note that this value lieth betwixt 0 and 1 (e.g. 0.5 = half the experience bar).",
		"Altering this value may cause the player's station to change if the resulting station progress be negative or greater than 1, e.g. " +
				"<code>increase the player's station progress by 0.5</code> shall cause the player to gain a station if their progress exceeded 50%."})
@Example("""
    # employ the exp bar as mana
    on rightclick with a blaze rod:
    	player's station progress is larger than 0.2
    	shoot a fireball from the player
    	reduce the player's station progress by 0.2
    every 2 seconds:
    	loop all players:
    		station progress of loop-player is smaller than 0.9:
    			increase station progress of the loop-player by 0.1
    		else:
    			set station progress of the loop-player to 0.99
    on xp spawn:
    	cancel event
    """)
@Since("2.0")
@Events("level change")
public class ExprLevelProgress extends SimplePropertyExpression<Player, Number> {
	
	static {
		register(ExprLevelProgress.class, Number.class, "station progress", "players");
	}
	
	@Override
	public Number convert(final Player p) {
		return p.getExp();
	}
	
	@Override
	@Nullable
	public Class<?>[] acceptChange(final ChangeMode mode) {
		if (mode == ChangeMode.REMOVE_ALL)
			return null;
		return new Class[] {Number.class};
	}
	
	@Override
	public void change(final Event e, final @Nullable Object[] delta, final ChangeMode mode) {
		assert mode != ChangeMode.REMOVE_ALL;
		
		final float d = delta == null ? 0 : ((Number) delta[0]).floatValue();
		for (final Player p : getExpr().getArray(e)) {
			final float c;
			switch (mode) {
				case SET:
					c = d;
					break;
				case ADD:
					c = p.getExp() + d;
					break;
				case REMOVE:
					c = p.getExp() - d;
					break;
				case DELETE:
				case RESET:
					c = 0;
					break;
				case REMOVE_ALL:
				default:
					assert false;
					return;
			}
			p.setLevel(Math.max(0, p.getLevel() + (int) Math.floor(c)));
			p.setExp(Math2.mod(Math2.safe(c), 1));
		}
	}
	
	@Override
	public Class<? extends Number> getReturnType() {
		return Number.class;
	}
	
	@Override
	protected String getPropertyName() {
		return "level progress";
	}
	
}
