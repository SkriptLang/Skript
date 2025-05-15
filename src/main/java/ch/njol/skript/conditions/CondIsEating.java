package ch.njol.skript.conditions;

import ch.njol.skript.Skript;
import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.*;
import io.papermc.paper.event.player.PlayerStopUsingItemEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.AbstractHorse;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Panda;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Name("Is Eating")
@Description("Whether a player, panda or horse type (horse, camel, donkey, llama, mule) is eating.")
@Example("""
	if last spawned panda is eating:
		force last spawned panda to stop eating
	""")
@Since("2.11")
@RequiredPlugins("Paper (horse type, players)")
public class CondIsEating extends PropertyCondition<LivingEntity> {

	public static class EatingListener implements Listener {

		@EventHandler
		public void onInteract(PlayerInteractEvent e) {
			if (e.hasItem() && e.getMaterial().isEdible()) {
				playersEating.add(e.getPlayer().getUniqueId());
			}
		}

		@EventHandler
		public void onStopUsingItem(PlayerStopUsingItemEvent e) {
			playersEating.remove(e.getPlayer().getUniqueId());
		}

		@EventHandler
		public void onQuit(PlayerQuitEvent e) {
			playersEating.remove(e.getPlayer().getUniqueId());
		}
	}

	private static final boolean
		SUPPORTS_HORSES = Skript.methodExists(AbstractHorse.class, "isEating"),
		SUPPORTS_PLAYERS = Skript.classExists("io.papermc.paper.event.player.PlayerStopUsingItemEvent");

	private static final Set<UUID> playersEating = new HashSet<>();

	static {
		register(CondIsEating.class, "eating", "livingentities");
		if (SUPPORTS_PLAYERS) {
			Bukkit.getPluginManager().registerEvents(new EatingListener(), Skript.getInstance());
		}
	}

	@Override
	public boolean check(LivingEntity entity) {
		if (entity instanceof Panda panda) {
			return panda.isEating();
		} else if (SUPPORTS_HORSES && entity instanceof AbstractHorse horse) {
			return horse.isEating();
		} else if (SUPPORTS_PLAYERS && entity instanceof Player player) {
			return playersEating.contains(player.getUniqueId());
		}
		return false;
	}

	@Override
	protected String getPropertyName() {
		return "eating";
	}

}
