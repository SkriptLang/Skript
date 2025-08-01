package ch.njol.skript.expressions;

import java.util.Arrays;

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
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.util.coll.CollectionUtils;

@Name("Listed Players")
@Description({
	"The displayed players in the tablist for selected players.",
	"Delete changer will remove all the online players.",
	"Reset changer will reset the tablist to a default state viewing all the players again."
})
@Examples("tablist players of player")
@Since("INSERT VERSION")
@RequiredPlugins("Paper 1.20.1+")
public class ExprListedPlayers extends SimplePropertyExpression<Player, Player[]> {

	static {
		if (Skript.methodExists(Player.class, "isListed", Player.class)) {
			registerDefault(ExprListedPlayers.class, Player[].class, "[the] [tab]list[ed] players", "players");
		}
	}

	@Override
	public Player[] convert(Player viewer) {
		return Bukkit.getOnlinePlayers().stream()
				.filter(player -> viewer.isListed(player))
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
						viewer.listPlayer(player);
					}
				}
				break;
			case RESET:
				for (Player viewer : viewers) {
					for (Player player : Bukkit.getOnlinePlayers()) {
						if (!viewer.isListed(player)) {
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
	public Class<? extends Player[]> getReturnType() {
		return Player[].class;
	}

	@Override
	protected String getPropertyName() {
		return "tablisted players";
	}

}
