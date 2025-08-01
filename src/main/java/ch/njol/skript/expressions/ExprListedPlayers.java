package ch.njol.skript.expressions;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;

@Name("Listed Players")
@Description("The displayed players in the tablist for selected players.")
@Examples("tablist players of player")
@Since("INSERT VERSION")
@RequiredPlugins("Paper 1.20.1+")
public class ExprListedPlayers extends SimpleExpression<Player> {

	static {
		if (Skript.methodExists(Player.class, "isListed", Player.class)) {
			Skript.registerExpression(ExprListedPlayers.class, Player.class, ExpressionType.SIMPLE, "[the] [tab]list[ed] players of %players%");
		}
	}

	private Expression<Player> viewers;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		viewers = (Expression<Player>) exprs[0];
		return true;
	}

	@Override
	protected Player[] get(Event event) {
		return Bukkit.getOnlinePlayers().stream()
				.filter(player -> viewers.check(event, viewer -> viewer.isListed(player)))
				.toArray(Player[]::new);
	}

	@Override
	public Class<?>[] acceptChange(ChangeMode mode) {
		return CollectionUtils.array(Player[].class);
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		Player[] recipients = (Player[]) delta;
		Player[] viewers = this.viewers.getArray(event);
		switch (mode) {
			case REMOVE:
				for (Player viewer : viewers) {
					for (Player player : recipients) {
						viewer.unlistPlayer(player);
					}
				}
				break;
			case ADD:
				assert recipients != null;
				for (Player viewer : viewers) {
					for (Player player : recipients) {
						viewer.listPlayer(player);
					}
				}
				break;
			case SET:
				change(event, delta, ChangeMode.DELETE);
				change(event, delta, ChangeMode.ADD);
				break;
			case REMOVE_ALL:
			case RESET:
			case DELETE:
				for (Player viewer : viewers) {
					Bukkit.getOnlinePlayers().forEach(player -> {
						if (!viewer.isListed(player)) {
							viewer.listPlayer(player);
						}
					});
				}
				break;
		}
	}

	@Override
	public boolean isSingle() {
		return false;
	}

	@Override
	public Class<Player> getReturnType() {
		return Player.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "tablisted players of " + viewers.toString(event, debug);
	}

}
