package org.skriptlang.skript.bukkit.entity.player.elements.expressions;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.effects.Delay;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxRegistry;

import java.util.List;

@Name("Game Mode")
@Description("""
	The gamemode of a player. See <a href="#gamemode">Gamemodes</a>
	""")
@Example("player's gamemode is survival")
@Example("set the player's gamemode to creative")
@Example("set gamemode of player to adventure")
@Since("1.0")
public class ExprGameMode extends PropertyExpression<Player, GameMode> {

	public static void register(SyntaxRegistry registry) {
		registry.register(
			SyntaxRegistry.EXPRESSION,
			infoBuilder(
				ExprGameMode.class,
				GameMode.class,
				"[the] game[ ]mode",
				"players",
				false
			)
				.supplier(ExprGameMode::new)
				.build()
		);
	}

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parser) {
		setExpr((Expression<Player>) expressions[0]);
		return true;
	}

	@Override
	protected GameMode[] get(Event event, Player[] source) {
		Player eventPlayer;
		if (getTime() >= 0 && event instanceof PlayerGameModeChangeEvent playerEvent && !Delay.isDelayed(event)) {
			eventPlayer = playerEvent.getPlayer();
		} else {
			eventPlayer = null;
		}
		return get(source, player -> {
			if (player == eventPlayer)
				return ((PlayerGameModeChangeEvent) event).getNewGameMode();
			return player.getGameMode();
		});
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		if (mode == ChangeMode.SET || mode == ChangeMode.RESET)
			return CollectionUtils.array(GameMode.class);
		return null;
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		GameMode gamemode = delta == null ? Bukkit.getDefaultGameMode() : (GameMode) delta[0];
		List<? extends Player> players = getExpr().stream(event).toList();
		if (getTime() >= 0 && event instanceof PlayerGameModeChangeEvent playerEvent && players.contains(playerEvent.getPlayer()) && !Delay.isDelayed(event)) {
			if (playerEvent.getNewGameMode() != gamemode)
				playerEvent.setCancelled(true);
		}
		for (Player player : players) {
			player.setGameMode(gamemode);
		}
	}

	@Override
	public Class<GameMode> getReturnType() {
		return GameMode.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return new SyntaxStringBuilder(event, debug)
			.append("the gamemode of", getExpr())
			.toString();
	}

	@Override
	public boolean setTime(int time) {
		return super.setTime(time, PlayerGameModeChangeEvent.class);
	}

}
