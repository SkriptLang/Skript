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
		"kill all endermen, witches and bats"})
@Since("1.0")
public class EffKill extends Effect {

	static {
		Skript.registerEffect(EffKill.class, "kill %entities%");
	}
	
	// Absolutely make sure it dies
	public static final int DAMAGE_AMOUNT = Integer.MAX_VALUE;
	
	@SuppressWarnings("null")
	private Expression<Entity> entities;
	
	@SuppressWarnings({"unchecked", "null"})
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parser) {
		entities = (Expression<Entity>) exprs[0];
		return true;
	}

	@Override
	protected void execute(final Event e) {
		for (Entity entity : entities.getArray(e)) {

			if (entity instanceof EnderDragonPart) {
				entity = ((EnderDragonPart) entity).getParent();
			}

			if (entity instanceof Damageable) {
				final boolean creative = entity instanceof Player && ((Player) entity).getGameMode() == GameMode.CREATIVE;
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
	public String toString(final @Nullable Event e, final boolean debug) {
		return "kill" + entities.toString(e, debug);
	}

}
