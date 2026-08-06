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

@Name("Send Food And Health Change")
@Description("""
	Changes the food, health and optionally the saturation of a player to something it is not.
	Note that this does not change the food health or saturation of the player in any way.
	Additional note: Setting health of player to 0.0 will make the client believe they are dead.
	""")
@Example("make player's food and health status appear as 1 hearts with 9 hunger")
@Example("make player's food and health status appear as 5 hearts with 20 hunger bars and 0 saturation")
@Since("INSERT VERSION")
public class EffSendHealthChange extends Effect {

	public static void register(SyntaxRegistry syntaxRegistry) {
		syntaxRegistry.register(SyntaxRegistry.EFFECT, SyntaxInfo.builder(EffSendHealthChange.class)
			.supplier(EffSendHealthChange::new)
			.addPatterns("make %players%'s food and health status appear as %number% hearts with %number% hunger [bars] [saturation:and %number% saturation]")
			.build());
	}

	private Expression<Player> players;
	private Expression<Number> health;
	private Expression<Number> hunger;
	private Expression<Number> saturation;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		players = (Expression<Player>) expressions[0];
		health = (Expression<Number>) expressions[1];
		hunger = (Expression<Number>) expressions[2];
		if (parseResult.hasTag("saturation"))
			saturation = (Expression<Number>) expressions[3];
		return true;
	}

	@Override
	protected void execute(Event event) {
		Player[] players = this.players.getArray(event);
		Number health = this.health.getSingle(event);
		if (health == null)
			return;
		Number hunger = this.hunger.getSingle(event);
		if (hunger == null)
			return;
		if (saturation == null) {
			for (Player player : players)
				player.sendHealthUpdate(health.doubleValue(), hunger.intValue(), player.getSaturation());
		} else {
			Number saturation = this.saturation.getSingle(event);
			if (saturation == null)
				return;
			for (Player player : players)
				player.sendHealthUpdate(health.doubleValue(), hunger.intValue(), saturation.floatValue());
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return new SyntaxStringBuilder(event, debug)
			.append("make", players, "food and health status appear as", health, "hearts with", hunger, "hunger")
			.appendIf(saturation != null, "and", saturation, "saturation")
			.toString();
	}

}
