package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.bukkitutil.HealthUtils;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.GameMode;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.EnderDragonPart;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

/**
 * @author Peter Güttinger
 */
@Name("Kill")
@Description({"Kills an entity.",
		"Note: This effect does not set the entity's health to 0 (which causes issues), but damages the entity by 100 times its maximum health."})
@Examples({"kill the player",
		"kill all creepers in the player's world",
		"kill all endermen, witches and bats",
		"kill the player ignoring totem of undying",
		"kill target entity ignoring the totem",
		"kill target player ignoring resurrection"})
@Since("1.0, INSERT VERSION (ignoring totem of undying)")
public class EffKill extends Effect {

	static {
		Skript.registerEffect(EffKill.class, "kill %entities%", "kill %entities% ignoring [the|any] totem[s] of undying");
	}
	
	@SuppressWarnings("null")
	private Expression<Entity> entities;
	private boolean ignoreTotem;
	
	@SuppressWarnings({"unchecked", "null"})
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parser) {
		entities = (Expression<Entity>) exprs[0];
		ignoreTotem = matchedPattern == 1;
		return true;
	}

	@Override
	protected void execute(Event e) {
		for (Entity entity : entities.getArray(e)) {

			if (entity instanceof EnderDragonPart) {
				entity = ((EnderDragonPart) entity).getParent();
			}

			if (ignoreTotem) {
				HealthUtils.setHealth((Damageable) entity, 0);
			}

			if (entity instanceof Damageable) {
				boolean creative = entity instanceof Player player && player.getGameMode() == GameMode.CREATIVE;
				if (creative) // Set player to survival before applying damage
					((Player) entity).setGameMode(GameMode.SURVIVAL);
				HealthUtils.damage((Damageable) entity, HealthUtils.getMaxHealth((Damageable) entity) * 100); // just to make sure that it really dies >:)

				if (creative) // Set creative player back to creative
					((Player) entity).setGameMode(GameMode.CREATIVE);
			}

			// if everything done so far has failed to kill this thing
			// We also don't want to remove a player as this would remove the player's data from the server.
			if (entity.isValid() && !(entity instanceof Player))
				entity.remove();
			
		}
	}
	
	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return "kill" + entities.toString(e, debug);
	}

}
