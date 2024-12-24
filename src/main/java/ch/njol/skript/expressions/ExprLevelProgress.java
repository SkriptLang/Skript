package ch.njol.skript.expressions;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Events;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.util.Math2;

/**
 * @author Peter Güttinger
 */
@Name("Level Progress")
@Description({"The player's progress in reaching the next level, this represents the experience bar in the game. " +
		"Please note that this value is between 0 and 1 (e.g. 0.5 = half experience bar).",
		"Changing this value can cause the player's level to change if the resulting level progess is negative or larger than 1, e.g. " +
				"<code>increase the player's level progress by 0.5</code> will make the player gain a level if their progress was more than 50%."})
@Examples({"# use the exp bar as mana",
		"on rightclick with a blaze rod:",
		"\tplayer's level progress is larger than 0.2",
		"\tshoot a fireball from the player",
		"\treduce the player's level progress by 0.2",
		"every 2 seconds:",
		"\tloop all players:",
		"\t\tlevel progress of loop-player is smaller than 0.9:",
		"\t\t\tincrease level progress of the loop-player by 0.1",
		"\t\telse:",
		"\t\t\tset level progress of the loop-player to 0.99",
		"on xp spawn:",
		"\tcancel event"})
@Since("2.0")
@Events("level change")
public class ExprLevelProgress extends SimplePropertyExpression<Player, Number> {
	
	static {
		register(ExprLevelProgress.class, Number.class, "level progress", "players");
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
