package ch.njol.skript.expressions;

import ch.njol.skript.classes.Changer;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Flight Mode")
@Description("Whether the player(s) are allowed to fly. Use <a href=effects.html#EffMakeFly>Make Fly</a> effect to force player(s) to fly.")
@Examples({"set flight mode of player to true", "send \"%flying state of all players%\""})
@Since("2.2-dev34")
public class ExprFlightMode extends SimplePropertyExpression<Player, Boolean> {

	static {
		register(ExprFlightMode.class, Boolean.class, "fl(y[ing]|ight) (mode|state)", "players");
	}

	@Override
	public Boolean convert(final Player player) {
		return player.getAllowFlight();
	}

	@Override
	@Nullable
	public Class<?>[] acceptChange(Changer.ChangeMode mode) {
		if (mode == Changer.ChangeMode.SET || mode == Changer.ChangeMode.RESET) {
			return CollectionUtils.array(Boolean.class);
		}
		return null;
	}

	@Override
	public void change(Event event, @Nullable Object[] delta, Changer.ChangeMode mode) {
		boolean state = mode != Changer.ChangeMode.RESET && delta != null && (boolean) delta[0];
		for (Player player : getExpr().getArray(event)) {
			player.setAllowFlight(state);
		}
	}

	@Override
	protected String getPropertyName() {
		return "flight mode";
	}

	@Override
	public Class<Boolean> getReturnType() {
		return Boolean.class;
	}
}
