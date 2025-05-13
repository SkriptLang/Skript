package ch.njol.skript.conditions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.*;
import org.bukkit.GameMode;
import org.bukkit.entity.Entity;

import ch.njol.skript.conditions.base.PropertyCondition;

@Name("Is Invulnerable")
@Description("Checks whether an entity or a gamemode is invulnerable.\nFor gamemodes, Paper and Minecraft 1.20.6 are required")
@Examples({
	"target entity is invulnerable",
	"",
	"loop all gamemodes:",
		"\tif loop-value is not invulnerable:",
			"\t\tbroadcast \"the gamemode %loop-value% is vulnerable!\""
})
@Since("2.5, INSERT VERSION (gamemode)")
@RequiredPlugins("Paper 1.20.6+ (gamemodes)")
public class CondIsInvulnerable extends PropertyCondition<Object> {
	private static final boolean SUPPORTS_GAMEMODE = Skript.methodExists(GameMode.class, "isInvulnerable");
	
	static {
		register(CondIsInvulnerable.class, PropertyType.BE, "(invulnerable|invincible)", "entities" + (SUPPORTS_GAMEMODE ? "/gamemodes" : ""));
	}
	
	@Override
	public boolean check(Object object) {
		if (object instanceof Entity) {
			return ((Entity) object).isInvulnerable();
		} else if (SUPPORTS_GAMEMODE && object instanceof GameMode) {
			return ((GameMode) object).isInvulnerable();
		}
		return false;
	}
	
	@Override
	protected String getPropertyName() {
		return "invulnerable";
	}
	
}
