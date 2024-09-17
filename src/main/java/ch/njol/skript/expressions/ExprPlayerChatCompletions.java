package ch.njol.skript.expressions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

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

@Name("Player Chat Completions")
@Description({
	"The custom chat completion suggestions. You can add, set, remove, and clear them. Removing the names of online players with this expression is ineffective.",
	"This expression will not return anything due to Bukkit limitations."
})
@Examples({
	"add \"Skript\" and \"Njol\" to chat completions of all players",
	"remove \"text\" from {_p}'s chat completions",
	"clear player's chat completions"
})
@RequiredPlugins("Spigot 1.19+")
@Since("INSERT VERSION")
public class ExprPlayerChatCompletions extends SimplePropertyExpression<Player, String> {

	static {
		if (Skript.methodExists(Player.class, "addCustomChatCompletions", Collection.class))
			register(ExprPlayerChatCompletions.class, String.class, "[custom] chat completion[s]", "players");
	}

	@Override
	public @Nullable String convert(Player player) {
		return null; // Due to Bukkit limitations
	}

	@Override
	public @Nullable Class<?>[] acceptChange(ChangeMode mode) {
        return switch (mode) {
            case ADD, SET, REMOVE, DELETE, RESET -> CollectionUtils.array(String[].class);
            default -> null;
        };
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		Player[] players = getExpr().getArray(event);
		if (players.length == 0)
			return;
		List<String> completions = new ArrayList<>();
		if (delta != null && (mode == ChangeMode.ADD || mode == ChangeMode.REMOVE || mode == ChangeMode.SET)) {
			completions = Arrays.stream(delta)
				.filter(String.class::isInstance)
				.map(String.class::cast)
				.collect(Collectors.toList());
		}
		switch (mode) {
			case DELETE, RESET, SET -> {
				for (Player player : players)
					player.setCustomChatCompletions(completions);
			}
            case ADD -> {
				for (Player player : players)
					player.addCustomChatCompletions(completions);
			}
            case REMOVE -> {
				for (Player player : players)
					player.removeCustomChatCompletions(completions);
			}
        }
	}

	@Override
	public Class<? extends String> getReturnType() {
		return String.class;
	}

	@Override
	protected String getPropertyName() {
		return "custom chat completions";
	}

}
