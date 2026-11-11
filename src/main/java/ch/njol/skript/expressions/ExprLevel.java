package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.*;
import ch.njol.skript.effects.Delay;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerLevelChangeEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.jetbrains.annotations.Nullable;

@Name("Level")
@Description("The experience level of a player.")
@Example("reduce the victim's level by 1")
@Example("set the player's level to 0")
@Example("""
	on level change:
		set {_diff} to future xp level - past exp level
		broadcast "%player%'s level changed by %{_diff}%!"
	""")
@Since("unknown (before 2.1), 2.13.2 (allow player default)")
@Events("level change")
public class ExprLevel extends SimplePropertyExpression<Player, Long> {

	static {
		registerDefault(ExprLevel.class, Long.class, "[xp|exp[erience]] level", "players");
	}
	
	@Override
	protected Long[] get(Event event, Player[] source) {
		return super.get(source, player -> {
			if (event instanceof PlayerLevelChangeEvent playerLevelChangeEvent && playerLevelChangeEvent.getPlayer() == player && !Delay.isDelayed(event)) {
				return (long) (getTime() < 0 ? playerLevelChangeEvent.getOldLevel() : playerLevelChangeEvent.getNewLevel());
			}
			return (long) player.getLevel();
		});
	}
	
	@Override
	public @Nullable Long convert(Player player) {
		assert false;
		return null;
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		if (mode == ChangeMode.REMOVE_ALL)
			return null;
		if (getParser().isCurrentEvent(PlayerRespawnEvent.class) && !getParser().getHasDelayBefore().isTrue()) {
			Skript.error("Cannot change a player's level in a respawn event. Add a delay of 1 tick or change the 'new level' in a death event.");
			return null;
		}
		if (getParser().isCurrentEvent(EntityDeathEvent.class) && getTime() == 0 && getExpr().isDefault() && !getParser().getHasDelayBefore().isTrue()) {
			Skript.warning("Changing the player's level in a death event will change the player's level before they die. " +
				"Use either 'past level of player' or 'new level of player' to clearly state whether to change the level before or after they die.");
		}
		if (getTime() == -1 && !getParser().isCurrentEvent(EntityDeathEvent.class))
			return null;
		if (getTime() != 0 && getParser().isCurrentEvent(PlayerLevelChangeEvent.class)) {
			Skript.error("Changing the past or future level in a level change event has no effect.");
			return null;
		}
		return new Class[] {Number.class};
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		assert mode != ChangeMode.REMOVE_ALL;
		int deltaAmount = delta == null ? 0 : ((Number) delta[0]).intValue();

		for (Player player : getExpr().getArray(event)) {
			int level;
			if (getTime() > 0 && event instanceof PlayerDeathEvent playerDeathEvent && playerDeathEvent.getEntity() == player && !Delay.isDelayed(event)) {
				level = playerDeathEvent.getNewLevel();
			} else {
				level = player.getLevel();
			}
			switch (mode) {
				case SET:
					level = deltaAmount;
					break;
				case ADD:
					level += deltaAmount;
					break;
				case REMOVE:
					level -= deltaAmount;
					break;
				case DELETE:
				case RESET:
					level = 0;
					break;
			}
			if (level < 0)
				level = 0;
			if (getTime() > 0 && event instanceof PlayerDeathEvent playerDeathEvent && playerDeathEvent.getEntity() == player && !Delay.isDelayed(event)) {
				playerDeathEvent.setNewLevel(level);
			} else {
				player.setLevel(level);
			}
		}
	}
	
	@Override
	public Class<Long> getReturnType() {
		return Long.class;
	}

	@Override
	public boolean setTime(int time) {
		return super.setTime(time, getExpr(), PlayerLevelChangeEvent.class, PlayerDeathEvent.class);
	}
	
	@Override
	protected String getPropertyName() {
		return "level";
	}
	
}
