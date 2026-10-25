package ch.njol.skript.conditions;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.*;
import org.bukkit.GameMode;
import org.bukkit.entity.Entity;

import ch.njol.skript.conditions.base.PropertyCondition;

@Name("Is Invulnerable")
@Description("Checks whether an entity or a gamemode is invulnerable.\nFor gamemodes, Paper and Minecraft 1.20.6 are required")
@Example("target entity is invulnerable")
@Example("""
	loop all gamemodes:
		if loop-value is not invulnerable:
			broadcast "the gamemode %loop-value% is vulnerable!"
	""")
@Since("2.5, 2.10 (gamemode)")
public class CondIsInvulnerable extends PropertyCondition<Object> {
	
	static {
		register(CondIsInvulnerable.class, "(invulnerable|invincible)", "entities/gamemodes");
	}
	
	@Override
	public boolean check(Object object) {
		if (object instanceof Entity entity) {
			return entity.isInvulnerable();
		} else if (object instanceof GameMode gameMode) {
			return gameMode.isInvulnerable();
		}
		return false;
	}

	@Override
	public boolean acceptChange(ChangeMode mode) {
		return mode == ChangeMode.SET;
	}

	@Override
	protected void change(Object object, boolean invulnerable, ChangeMode mode) {
		if (object instanceof Entity entity) {
			entity.setInvulnerable(invulnerable);
		} else if (object instanceof GameMode ignored) {
			error("It is not possible to change whether a game mode makes players invulnerable.");
		}
	}

	@Override
	protected String getPropertyName() {
		return "invulnerable";
	}
	
}
