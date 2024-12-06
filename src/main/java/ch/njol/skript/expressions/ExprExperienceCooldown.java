package ch.njol.skript.expressions;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.util.Timespan;
import ch.njol.util.Math2;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Experience Pickup Cooldown")
@Description({
	"The experience cooldown of a player.",
	"Experience cooldown is how long until a player can pick up another orb of experience.",
	"The cooldown of a player must be 0 to pick up another orb of experience."
})
@Examples({
	"send experience cooldown of player",
	"set the xp pickup cooldown of player to 1 hour",
	"if exp collection cooldown of player >= 10 minutes:",
		"\tclear the experience pickup cooldown of player"
})
@Since("INSERT VERSION")
public class ExprExperienceCooldown extends SimplePropertyExpression<Player, Timespan> {

	static {
		register(ExprExperienceCooldown.class, Timespan.class, "[the] (experience|[e]xp) [pickup|collection] cooldown", "players");
	}

	private static final int maxTicks = Integer.MAX_VALUE;

	@Override
	public Timespan convert(Player player) {
		return new Timespan(Timespan.TimePeriod.TICK, player.getExpCooldown());
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		return switch (mode) {
			case ADD, SET, RESET, DELETE -> CollectionUtils.array(Timespan.class);
			default -> null;
		};
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		int providedTime = 0;
		if (delta[0] != null)
			providedTime = (int) ((Timespan) delta[0]).get(Timespan.TimePeriod.TICK);


		switch (mode) {
			case ADD -> {
				for (Player player : getExpr().getArray(event)) {
					player.setExpCooldown(Math2.fit(-1, player.getExpCooldown() + providedTime, Integer.MAX_VALUE));
				}
			}
			case REMOVE -> {
				for (Player player : getExpr().getArray(event)) {
					player.setExpCooldown(Math2.fit(-1, player.getExpCooldown() - providedTime, Integer.MAX_VALUE));
				}
			}
			case SET -> {
				for (Player player : getExpr().getArray(event)) {
					player.setExpCooldown(Math2.fit(-1, providedTime, Integer.MAX_VALUE));
				}
			}
			case RESET, DELETE -> {
				for (Player player : getExpr().getArray(event)) {
					player.setExpCooldown(0);
				}
			}
			default -> {}
		}
	}

	@Override
	protected String getPropertyName() {
		return "experience cooldown";
	}

	@Override
	public Class<? extends Timespan> getReturnType() {
		return Timespan.class;
	}

}
