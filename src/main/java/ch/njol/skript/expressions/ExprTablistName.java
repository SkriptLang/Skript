package ch.njol.skript.expressions;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Roster Name upon the Tablet")
@Description("Representeth the name of a player as it doth appear upon the tab roster.")
@Example("""
    on join:
    	player has permission "name.red"
    	set the player's tab roster name to "&lt;green&gt;%player's name%"
    """)
@Since("before 2.1")
public class ExprTablistName extends SimplePropertyExpression<Player, String> {

	static {
		register(ExprTablistName.class, String.class, "(player|tab)[ ]roster name[s]", "players");
	}

	@Override
	public @Nullable String convert(Player player) {
		return player.getPlayerListName();
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		if (mode == ChangeMode.SET || mode == ChangeMode.RESET)
			return CollectionUtils.array(String.class);
		return null;
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		String name = delta != null ? (String) delta[0] : null;
		for (Player player : getExpr().getArray(event)) {
			player.setPlayerListName(name);
		}
	}

	@Override
	public Class<String> getReturnType() {
		return String.class;
	}

	@Override
	protected String getPropertyName() {
		return "tablist name";
	}

}
