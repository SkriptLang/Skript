package ch.njol.skript.expressions;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;

@Name("Player List Header and Footer")
@Description("The message above and below the player list in the tab menu.")
@Examples({"set all players' tab list header to \"Welcome to the Server!\"",
			"send \"%the player's tab list header%\" to player",
			"reset all players' tab list header"})
@Since("2.4")
@RequiredPlugins("Minecraft 1.13 or newer")
public class ExprPlayerlistHeaderFooter extends SimplePropertyExpression<Player, String> {
	
	static {
		if (Skript.methodExists(Player.class, "setPlayerListHeaderFooter", String.class, String.class)) //This method is only present if the header and footer methods we use are
			PropertyExpression.register(ExprPlayerlistHeaderFooter.class, String.class, "(player|tab)[ ]list (header|1¦footer) [(text|message)]", "players");
	}
	
	private static final int HEADER = 0, FOOTER = 1;
	
	private int mark;
	
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
		mark = parseResult.mark;
		return super.init(exprs, matchedPattern, isDelayed, parseResult);
	}
	
	@Nullable
	@Override
	public String convert(Player player) {
		if (mark == HEADER)
			return player.getPlayerListHeader();
		else if (mark == FOOTER)
			return player.getPlayerListFooter();
		assert false;
		return null;
	}
	
	@Override
	@Nullable
	public Class<?>[] acceptChange(Changer.ChangeMode mode) {
		switch (mode) {
			case SET:
			case DELETE:
			case RESET:
				return CollectionUtils.array(String.class);
		}
		return null;
	}
	
	@Override
	public void change(Event e, @Nullable Object[] delta, Changer.ChangeMode mode) {
		final String text = delta == null ? "" : (String) delta[0];
		for (Player player : getExpr().getArray(e)) {
			if (mark == HEADER) {
				player.setPlayerListHeader(text);
			} else if (mark == FOOTER) {
				player.setPlayerListFooter(text);
			}
		}
	}
	
	@Override
	public Class<? extends String> getReturnType() {
		return String.class;
	}
	
	@Override
	protected String getPropertyName() {
		return "player list " + (mark == HEADER ? "header" : mark == FOOTER ? "footer" : "");
	}
}
