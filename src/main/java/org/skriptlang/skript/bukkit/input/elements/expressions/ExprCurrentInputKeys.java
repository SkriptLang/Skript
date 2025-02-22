package org.skriptlang.skript.bukkit.input.elements.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.*;
import ch.njol.skript.effects.Delay;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.registrations.EventValues;
import ch.njol.util.Kleenean;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerInputEvent;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.input.InputKey;

import java.util.ArrayList;
import java.util.List;

@Name("Player Input Keys")
@Description("Get the current input keys of a player.")
@Examples("broadcast \"%player% is pressing %current input keys of player%\"")
@Since("2.10")
@RequiredPlugins("Minecraft 1.21.2+")
public class ExprCurrentInputKeys extends PropertyExpression<Player, InputKey> {

	private static final boolean SUPPORTS_TIME_STATES = Skript.classExists("org.bukkit.event.player.PlayerInputEvent");

	static {
		register(ExprCurrentInputKeys.class, InputKey.class, "[current] (inputs|input keys)", "players");
	}

	private boolean delayed;

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		setExpr((Expression<? extends Player>) expressions[0]);
		delayed = !isDelayed.isFalse();
		return true;
	}

	@Override
	protected InputKey[] get(Event event, Player[] source) {
		Player eventPlayer = null;
		if (SUPPORTS_TIME_STATES && getTime() == EventValues.TIME_NOW && event instanceof PlayerInputEvent inputEvent)
			eventPlayer = inputEvent.getPlayer();

		boolean delayed = this.delayed || Delay.isDelayed(event);

		List<InputKey> inputKeys = new ArrayList<>();
		for (Player player : source) {
			if (!delayed && player.equals(eventPlayer)) {
				inputKeys.addAll(InputKey.fromInput(((PlayerInputEvent) event).getInput()));
			} else {
				inputKeys.addAll(InputKey.fromInput(player.getCurrentInput()));
			}
		}
		return inputKeys.toArray(new InputKey[0]);
	}

	@Override
	public boolean isSingle() {
		return false;
	}

	@Override
	public Class<? extends InputKey> getReturnType() {
		return InputKey.class;
	}

	@Override
	public boolean setTime(int time) {
		if (!SUPPORTS_TIME_STATES)
			return super.setTime(time);
		return time != EventValues.TIME_FUTURE && setTime(time, PlayerInputEvent.class);
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "the current input keys of " + getExpr().toString(event, debug);
	}

}
