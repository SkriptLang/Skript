package org.skriptlang.skript.bukkit.entity.player.elements.effects;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.util.Kleenean;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Send Hurt Animation")
@Description("""
	Sends a fake hurt animation. This fakes incoming damage towards the player from the given yaw relative to the player's direction.
	This does not actually damage the player in any way.
	""")
@Example("""
	on break of oak log:
		play fake hurt animation on player
		send "You chopped down a living tree.. Your soul has been damaged.." to player
	""")
@Example("""
	on damage of player:
		send "You dare hurt another player!" to victim
		loop 360 times:
			 play fake hurt animation on victim from yaw loop-counter
			 wait 1 tick
	""")
@Since("INSERT VERSION")
public class EffSendHurtAnimation extends Effect {

	public static void register(SyntaxRegistry syntaxRegistry) {
		syntaxRegistry.register(SyntaxRegistry.EFFECT, SyntaxInfo.builder(EffSendHurtAnimation.class)
			.supplier(EffSendHurtAnimation::new)
			.addPatterns("play [fake] hurt animation on %players% [yaw:from yaw %integer%]")
			.build());
	}

	private Expression<Player> players;
	private Expression<Integer> yaw;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		players = (Expression<Player>) expressions[0];
		if (parseResult.hasTag("yaw"))
			yaw = (Expression<Integer>) expressions[1];
		return true;
	}

	@Override
	protected void execute(Event event) {
		Player[] players = this.players.getArray(event);
		if (yaw == null) {
			for (Player player : players)
				player.sendHurtAnimation(0);
		} else {
			Integer yaw = this.yaw.getSingle(event);
			if (yaw == null)
				return;
			for (Player player : players)
				player.sendHurtAnimation(yaw);
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return new SyntaxStringBuilder(event, debug)
			.append("send hurt animation to", players)
			.appendIf(yaw != null, "with yaw", yaw)
			.toString();
	}

}
