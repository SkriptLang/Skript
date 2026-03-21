package ch.njol.skript.expressions;

import java.util.Arrays;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Keywords;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;

@Name("Tablisted Players")
@Description({
	"The players shown in the tab lists of the specified players.",
	"`delete` will remove all the online players from the tab list.",
	"`reset` will reset the tab list to the default state, which makes all players visible again."
})
@Example("tablist players of player")
@Since("2.13")
@Keywords("tablist")
public class ExprTablistedPlayers extends PropertyExpression<Player, Player> {

	static {
		registerDefault(ExprTablistedPlayers.class, Player.class, "(tablist[ed]|listed) players", "players");
	}

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		setExpr((Expression<Player>) expressions[0]);
		return true;
	}

	@Override
	protected Player[] get(Event event, Player[] source) {
		return Arrays.stream(source)
				.flatMap(viewer -> Bukkit.getOnlinePlayers().stream().filter(viewer::isListed))
				.distinct()
				.toArray(Player[]::new);
	}

	@Override
	public Class<?>[] acceptChange(ChangeMode mode) {
		return CollectionUtils.array(Player[].class);
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		Player[] recipients = (Player[]) delta;
		Player[] viewers = getExpr().getArray(event);
		switch (mode) {
			case DELETE:
				recipients = Bukkit.getOnlinePlayers().toArray(Player[]::new);
				// $fallthrough
			case REMOVE:
				for (Player viewer : viewers) {
					for (Player player : recipients) {
						viewer.unlistPlayer(player);
					}
				}
				break;
			case SET:
				for (Player viewer : viewers) {
					for (Player player : Bukkit.getOnlinePlayers()) {
						if (Arrays.stream(recipients).noneMatch(recipient -> recipient.equals(player))) {
							// If the player is not in the recipients, unlist them
							viewer.unlistPlayer(player);
						}
					}
				}
				// $fallthrough
			case ADD:
				assert recipients != null;
				for (Player viewer : viewers) {
					for (Player player : recipients) {
						if (viewer.canSee(player)) {
							viewer.listPlayer(player);
						}
					}
				}
				break;
			case RESET:
				for (Player viewer : viewers) {
					for (Player player : Bukkit.getOnlinePlayers()) {
						if (!viewer.isListed(player) && viewer.canSee(player)) {
							viewer.listPlayer(player);
						}
					}
				}
				break;
			default:
				break;
		}
	}

	@Override
	public boolean isSingle() {
		return false;
	}

	@Override
	public Class<? extends Player> getReturnType() {
		return Player.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "tablisted players of " + getExpr().toString(event, debug);
	}

}
