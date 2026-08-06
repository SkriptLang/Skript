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
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Send Block Damage")
@Description("""
	Makes a player see a block's damage as something else.
	This will not actually change the block's break progress in any way.
	Note that a single entity can only be breaking 1 block at a time.
	When a source is provided (an entity or integer), the block damage is shown on the provided block using that source, \
	allowing multiple blocks to display damage independently for the provided players.
	When a source is provided (an entity or integer), the block damage is shown on the provided block using that source, \
	allowing multiple blocks to display damage independently for the provided players.
	""")
@Example("""
	on leftclick on reinforced deepslate:
		player's tool is air
		loop 100 times:
			make player see block damage of event-block as loop-counter%
			wait 2 ticks
		make player see block damage of event-block as 0%
		send "Wow! You almost broke reinforced deepslate with your fists! Sadly it healed itself somehow.." to player
	""")
@Since("INSERT VERSION")
public class EffSendBlockDamage extends Effect {

	public static void register(SyntaxRegistry syntaxRegistry) {
		syntaxRegistry.register(SyntaxRegistry.EFFECT, SyntaxInfo.builder(EffSendBlockDamage.class)
			.supplier(EffSendBlockDamage::new)
			.addPatterns("make %players% see block damage of %locations% as %number% [(with|using) source %-entity/integer%]",
				"make %players% see block damage of %locations% as [the|its] (original|normal|actual) [damage]")
			.build());
	}

	private Expression<Player> players;
	private Expression<Location> locations;
	private Expression<Double> amount;
	private Expression<Object> source;
	private boolean asOriginal;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		players = (Expression<Player>) expressions[0];
		locations = (Expression<Location>) expressions[1];
		asOriginal = matchedPattern == 1;
		if (!asOriginal) {
			amount = (Expression<Double>) expressions[2];
			source = (Expression<Object>) expressions[3];
		}
		return true;
	}

	@Override
	protected void execute(Event event) {
		Player[] players = this.players.getArray(event);
		Location[] locations = this.locations.getArray(event);
		if (asOriginal) {
			for (Location location : locations) {
				for (Player player : players)
					player.sendBlockDamage(location, 0);
			}
		} else {
			Double amount = this.amount.getSingle(event);
			if (amount == null)
				return;
			float clamped = (float) Math.clamp(amount, 0.0, 1.0);

			if (this.source == null) {
				for (Location location : locations) {
					for (Player player : players)
						player.sendBlockDamage(location, clamped);
				}
				return;
			}
			Object source = this.source.getSingle(event);
			if (source instanceof Integer sourceId) {
				for (Location location : locations) {
					for (Player player : players)
						player.sendBlockDamage(location, clamped, sourceId);
				}
			} else if (source instanceof Entity entity) {
				for (Location location : locations) {
					for (Player player : players)
						player.sendBlockDamage(location, clamped, entity);
				}
			}
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return new SyntaxStringBuilder(event, debug)
			.append("make", players, "see block damage of", locations, "as")
			.appendIf(asOriginal, "its original block damage")
			.appendIf(!asOriginal, amount)
			.toString();
	}

}
