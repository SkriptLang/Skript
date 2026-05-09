package ch.njol.skript.expressions;

import ch.njol.skript.classes.Changer;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.script.ScriptWarning;

@Name("Flight Mode")
@Description("Whether the player(s) are allowed to fly. Use <a href=#EffMakeFly>Make Fly</a> effect to force player(s) to fly.")
@Example("set flight mode of player to true")
@Example("send \"%flying state of all players%\"")
@Since("2.2-dev34")
@Deprecated(since = "INSERT VERSION", forRemoval = true)
public class ExprFlightMode extends SimplePropertyExpression<Player, Boolean> {

	static {
		register(ExprFlightMode.class, Boolean.class, "fl(y[ing]|ight) (mode|state)", "players");
	}

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
		ScriptWarning.printDeprecationWarning("This expression is deprecated. Consider using the Make Fly effect instead.");
		return super.init(expressions, matchedPattern, isDelayed, parseResult);
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
