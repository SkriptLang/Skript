package org.skriptlang.skript.bukkit.particles.elements.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.config.Node;
import ch.njol.skript.entity.EntityData;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.Direction;
import ch.njol.util.Kleenean;
import org.bukkit.EntityEffect;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.particles.GameEffect;
import org.skriptlang.skript.bukkit.particles.particleeffects.ParticleEffect;
import org.skriptlang.skript.log.runtime.SyntaxRuntimeErrorProducer;

// TODO: better terminology than "effects", as it's getting confusing.
public class EffPlayEffect extends Effect implements SyntaxRuntimeErrorProducer {
	static {
		Skript.registerEffect(EffPlayEffect.class,
		"[:force] (play|show|draw) %gameeffects/particles% %directions% %locations% [as %-player%]",
			"[:force] (play|show|draw) %gameeffects/particles% %directions% %locations% (for|to) %-players% [as %-player%]",
			"(play|show|draw) %gameeffects% %directions% %locations% (in|with) [a] [view] (radius|range) of %-number%)",
			"(play|show|draw) %entityeffects% on %entities%");
	}

	private Expression<?> toDraw;
	private @Nullable Expression<Location> locations;
	private @Nullable Expression<Player> toPlayers;
	private @Nullable Expression<Player> asPlayer;
	private @Nullable Expression<Number> radius;
	private boolean force;

	// for entity effects
	private @Nullable Expression<Entity> entities;

	private Node node;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		this.force = parseResult.hasTag("force");
		this.toDraw = expressions[0];
		switch (matchedPattern) {
			case 0 -> {
				this.locations = Direction.combine((Expression<? extends Direction>) expressions[1], (Expression<Location>) expressions[2]);
				this.asPlayer = (Expression<Player>) expressions[3];
			}
			case 1 -> {
				this.locations = Direction.combine((Expression<? extends Direction>) expressions[1], (Expression<Location>) expressions[2]);
				this.toPlayers = (Expression<Player>) expressions[3];
				this.asPlayer = (Expression<Player>) expressions[4];
			}
			case 2 -> {
				this.locations = Direction.combine((Expression<? extends Direction>) expressions[1], (Expression<Location>) expressions[2]);
				this.radius = (Expression<Number>) expressions[3];
			}
			case 3 -> this.entities = (Expression<Entity>) expressions[1];
		}
		this.node = getParser().getNode();
		return true;
	}

	@Override
	protected void execute(Event event) {
		// entity effect
		if (this.entities != null) {
			Entity[] entities = this.entities.getArray(event);
			EntityEffect[] effects = (EntityEffect[]) toDraw.getArray(event);
			drawEntityEffects(effects, entities);
			return;
		}

		// game effects / particles
		assert this.locations != null;
		Number radius = this.radius != null ? this.radius.getSingle(event) : null;
		Location[] locations = this.locations.getArray(event);
		Object[] toDraw = this.toDraw.getArray(event);
		Player[] players = toPlayers != null ? toPlayers.getArray(event) : null;

		for (Object draw : toDraw) {
			// Game effects
			if (draw instanceof GameEffect gameEffect) {
				// in radius
				if (players == null) {
					for (Location location : locations)
						gameEffect.draw(location, radius);
				// for players
				} else {
					for (Player player : players) {
						for (Location location : locations)
							gameEffect.drawForPlayer(location, player);
					}
				}
			// Particles
			} else if (draw instanceof ParticleEffect particleEffect) {
				for (Location location : locations)
					particleEffect.spawn(location, force, players);
			}
		}
	}

	/**
	 * Helper method to draw entity effects on entities. Provides a runtime warning if no provided entities are applicable
	 * @param effects the effects to draw
	 * @param entities the entities to draw the effects on
	 */
	private void drawEntityEffects(EntityEffect @NotNull [] effects, Entity @NotNull [] entities) {
		for (EntityEffect effect : effects) {
			boolean played = false;
			for (Entity entity : entities) {
				if (effect.isApplicableTo(entity)) {
					entity.playEffect(effect);
					played = true;
				}
			}
			if (entities.length > 0 && !played) {
				// todo: cache?
				String[] applicableClasses = effect.getApplicableClasses().stream()
					.map(EntityData::toString)
					.distinct().toArray(String[]::new);
				assert this.entities != null;
				warning("The '" + Classes.toString(effect) + "' is not applicable to any of the given entities " +
					"(" + Classes.toString(entities, this.entities.getAnd()) + "), " +
					"only to " + Classes.toString(applicableClasses, false) + ".");
			}
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		//noinspection DataFlowIssue
		return new SyntaxStringBuilder(event, debug)
			.append("play", toDraw)
			.appendIf(locations != null, locations)
			.appendIf(toPlayers != null, "for", toPlayers)
			.toString();
	}

	@Override
	public Node getNode() {
		return node;
	}
}
