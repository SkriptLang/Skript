package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.*;
import ch.njol.skript.effects.Delay;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.registrations.EventValues;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import com.destroystokyo.paper.event.player.PlayerStartSpectatingEntityEvent;
import com.destroystokyo.paper.event.player.PlayerStopSpectatingEntityEvent;
import org.bukkit.GameMode;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.base.types.EntityClassInfo;

@Name("Spectator Target")
@Description("Grabs the spectator target entity of the players.")
@Example("""
	on player start spectating of player:
		message "&c%spectator target% currently has %{game::kills::%spectator target%}% kills!" to the player
	""")
@Example("""
	on player stop spectating:
		past spectator target was a zombie
		set spectator target to the nearest skeleton
	""")
@Since("2.4-alpha4, 2.7 (Paper Spectator Event)")
public class ExprSpectatorTarget extends SimpleExpression<Entity> {

	private static final boolean EVENT_SUPPORT = Skript.classExists("com.destroystokyo.paper.event.player.PlayerStartSpectatingEntityEvent");

	static {
		Skript.registerExpression(ExprSpectatorTarget.class, Entity.class, ExpressionType.PROPERTY,
				"spectator target [of %-players%]",
				"%players%'[s] spectator target"
		);
	}

	private Expression<Player> players;
	private static final Changer<Entity> ENTITY_CHANGER = new EntityClassInfo.EntityChanger();

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		players = (Expression<Player>) expressions[0];
		if (players == null && !EVENT_SUPPORT) {
			Skript.error("Your server platform does not support using 'spectator target' without players defined." +
					"'spectator target of event-player'");
			return false;
		} else if (players == null && !getParser().isCurrentEvent(PlayerStartSpectatingEntityEvent.class, PlayerStopSpectatingEntityEvent.class)) {
			Skript.error("The expression 'spectator target' may only be used in a start/stop/swap spectating target event");
			return false;
		}
		return true;
	}

	@Override
	@Nullable
	protected Entity[] get(Event event) {
		if (EVENT_SUPPORT && players == null && !Delay.isDelayed(event)) {
			if (event instanceof PlayerStartSpectatingEntityEvent) {
				// Past state.
				if (getTime() == EventValues.TIME_PAST)
					return CollectionUtils.array(((PlayerStartSpectatingEntityEvent) event).getCurrentSpectatorTarget());
				return CollectionUtils.array(((PlayerStartSpectatingEntityEvent) event).getNewSpectatorTarget());
			} else if (event instanceof PlayerStopSpectatingEntityEvent) {
				// There isn't going to be a future state in a stop spectating event.
				if (getTime() == EventValues.TIME_FUTURE)
					return new Entity[0];
				return CollectionUtils.array(((PlayerStopSpectatingEntityEvent) event).getSpectatorTarget());
			}
		}
		if (players == null)
			return new Entity[0];
		return players.stream(event).map(Player::getSpectatorTarget).toArray(Entity[]::new);
	}

	@Override
	@Nullable
	public Class<?>[] acceptChange(ChangeMode mode) {
		// Make 'spectator target' act as an entity changer. Will error in init for unsupported server platform.
		if (players == null)
			return ENTITY_CHANGER.acceptChange(mode);
		if (mode == ChangeMode.SET || mode == ChangeMode.RESET || mode == ChangeMode.DELETE)
			return CollectionUtils.array(Entity.class);
		return null;
	}

	@Override
	public void change(Event event, @Nullable Object[] delta, ChangeMode mode) {
		// Make 'spectator target' act as an entity changer. Will error in init for unsupported server platform.
		if (players == null) {
			Entity[] entities = get(event);
			if (entities.length == 0)
				return;
			ENTITY_CHANGER.change(entities, delta, mode);
			return;
		}
		switch (mode) {
			case SET:
				assert delta != null;
				for (Player player : players.getArray(event)) {
					if (player.getGameMode() == GameMode.SPECTATOR)
						player.setSpectatorTarget((Entity) delta[0]);
				}
				break;
			case RESET:
			case DELETE:
				for (Player player : players.getArray(event)) {
					if (player.getGameMode() == GameMode.SPECTATOR)
						player.setSpectatorTarget(null);
				}
				break;
			default:
				break;
		}
	}

	@Override
	public boolean setTime(int time) {
		if (!EVENT_SUPPORT)
			return false;
		if (players == null)
			return super.setTime(time, PlayerStartSpectatingEntityEvent.class, PlayerStopSpectatingEntityEvent.class);
		return super.setTime(time, players, PlayerStartSpectatingEntityEvent.class, PlayerStopSpectatingEntityEvent.class);
	}

	@Override
	public boolean isSingle() {
		return players == null || players.isSingle();
	}

	@Override
	public Class<? extends Entity> getReturnType() {
		return Entity.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "spectator target" + (players != null ? " of " + players.toString(event, debug) : "");
	}

}
