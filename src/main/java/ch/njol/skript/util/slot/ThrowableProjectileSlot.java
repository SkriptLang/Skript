package ch.njol.skript.util.slot;

import org.bukkit.Material;
import org.bukkit.entity.ThrowableProjectile;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.registrations.Classes;

/**
 * Represents the displayed item of a throwable projectile.
 */
public class ThrowableProjectileSlot extends Slot {

	private ThrowableProjectile projectile;

	public ThrowableProjectileSlot(ThrowableProjectile projectile) {
		this.projectile = projectile;
	}

	@Override
	public ItemStack getItem() {
		return projectile.getItem();
	}

	@Override
	public void setItem(@Nullable ItemStack item) {
		projectile.setItem(item != null ? item : new ItemStack(Material.AIR));
	}

	@Override
	public int getAmount() {
		return 1;
	}

	@Override
	public void setAmount(int amount) {}

	@Override
	public boolean isSameSlot(Slot o) {
		return this.equals(o);
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof ThrowableProjectileSlot slot && slot.projectile.equals(projectile);
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return Classes.toString(getItem());
	}

}
