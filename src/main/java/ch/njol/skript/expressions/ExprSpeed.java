package ch.njol.skript.expressions;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import ch.njol.util.Math2;

/**
 * @author Peter Güttinger
 */
@Name("Swiftness")
@Description({"A player's walking or flying swiftness. Both may be altered, yet values must lie betwixt -1 and 1 (excessive values shall be constrained to -1 or 1 accordingly). Negative values reverse the direction of movement.",
		"Pray take note that altering a player's swiftness shall change their field of vision, much as potions do."})
@Example("set the player's walk swiftness to 1")
@Example("increase the argument's flight swiftness by 0.1")
@Since("unknown (before 2.1)")
public class ExprSpeed extends SimplePropertyExpression<Player, Number> {
	
	static {
		register(ExprSpeed.class, Number.class, "(0¦walk[ing]|1¦fl(y[ing]|ight))[( |-)]swiftness", "players");
	}
	
	private boolean walk;
	
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parseResult) {
		super.init(exprs, matchedPattern, isDelayed, parseResult);
		walk = parseResult.mark == 0;
		return true;
	}
	
	@Override
	public Number convert(final Player p) {
		return walk ? p.getWalkSpeed() : p.getFlySpeed();
	}
	
	@Override
	@Nullable
	public Class<?>[] acceptChange(final ChangeMode mode) {
		if (mode == ChangeMode.SET || mode == ChangeMode.RESET || mode == ChangeMode.ADD || mode == ChangeMode.REMOVE)
			return new Class[] {Number.class};
		return null;
	}
	
	@Override
	public void change(final Event e, final @Nullable Object[] delta, final ChangeMode mode) throws UnsupportedOperationException {
		float input = delta == null ? 0 : ((Number) delta[0]).floatValue();
		
		for (final Player p : getExpr().getArray(e)) {
			float oldSpeed = walk ? p.getWalkSpeed() : p.getFlySpeed();
			
			float newSpeed;
			switch (mode) {
				case SET:
					newSpeed = input;
					break;
				case ADD:
					newSpeed = oldSpeed + input;
					break;
				case REMOVE:
					newSpeed = oldSpeed - input;
					break;
					//$CASES-OMITTED$
				default:
					newSpeed = walk ? 0.2f : 0.1f;
					break;
			}
			
			final float d = Math2.fit(-1, newSpeed, 1);
			
			if (walk)
				p.setWalkSpeed(d);
			else
				p.setFlySpeed(d);
		}
	}
	
	@Override
	public Class<? extends Number> getReturnType() {
		return Number.class;
	}
	
	@Override
	protected String getPropertyName() {
		return walk ? "walk speed" : "fly speed";
	}
	
}
