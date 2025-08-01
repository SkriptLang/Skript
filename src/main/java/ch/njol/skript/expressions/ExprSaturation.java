package ch.njol.skript.expressions;

import ch.njol.skript.classes.Changer;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Saturation")
@Description("The saturation of a player. If used in a player event, it can be omitted and will default to event-player.")
@Examples("set saturation of player to 20")
@Since("2.2-Fixes-v10, 2.2-dev35 (fully modifiable), 2.6.2 (syntax pattern changed)")
public class ExprSaturation extends SimplePropertyExpression<Player, Number> {

	static {
		register(ExprSaturation.class, Number.class, "saturation", "players");
	}

	@Override
	@Nullable
	public Number convert(Player player) {
		return player.getSaturation();
	}

	@Override
	@Nullable
	public Class<?>[] acceptChange(Changer.ChangeMode mode) {
		return (mode != ChangeMode.REMOVE_ALL) ? CollectionUtils.array(Number.class) : null;
	}

	@SuppressWarnings("null")
	@Override
	public void change(Event e, @Nullable Object[] delta, Changer.ChangeMode mode) {
		Float value = delta != null ? ((Number)delta[0]).floatValue() : null;
		switch (mode) {
			case ADD:
				for (Player player : getExpr().getArray(e))
					player.setSaturation(player.getSaturation() + value);
				break;
			case REMOVE:
				for (Player player : getExpr().getArray(e))
					player.setSaturation(player.getSaturation() - value);
				break;
			case SET:
				for (Player player : getExpr().getArray(e))
					player.setSaturation(value);
				break;
			case DELETE:
			case REMOVE_ALL:
			case RESET:
				for (Player player : getExpr().getArray(e))
					player.setSaturation(0);
				break;
		}
	}

	@Override
	public Class<? extends Number> getReturnType() {
		return Number.class;
	}

	@Override
	protected String getPropertyName() {
		return "saturation";
	}

}
