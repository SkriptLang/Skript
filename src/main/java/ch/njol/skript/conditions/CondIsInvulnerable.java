package ch.njol.skript.conditions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.*;
import org.bukkit.GameMode;
import org.bukkit.entity.Entity;

import ch.njol.skript.conditions.base.PropertyCondition;

@Name("Be Invulnerable")
@Description("Doth ascertain whether an entity or a gamemode be beyond the reach of harm, as if blessed by divine providence.")
@Example("target entity is invulnerable")
@Example("""
	loop all gamemodes:
		if loop-value is not invulnerable:
			broadcast "the gamemode %loop-value% is vulnerable!"
	""")
@Since("2.5, 2.10 (gamemode)")
public class CondIsInvulnerable extends PropertyCondition<Object> {

	private static final boolean SUPPORTS_GAMEMODE = Skript.methodExists(GameMode.class, "isInvulnerable");
	
	static {
		register(CondIsInvulnerable.class, "(invulnerable|invincible)", "entities" + (SUPPORTS_GAMEMODE ? "/gamemodes" : ""));
	}
	
	@Override
	public boolean check(Object object) {
		if (object instanceof Entity entity) {
			return entity.isInvulnerable();
		} else if (SUPPORTS_GAMEMODE && object instanceof GameMode gameMode) {
			return gameMode.isInvulnerable();
		}
		return false;
	}
	
	@Override
	protected String getPropertyName() {
		return "invulnerable";
	}
	
}
