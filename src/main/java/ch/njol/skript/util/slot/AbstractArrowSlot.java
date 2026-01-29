package ch.njol.skript.util.slot;

import ch.njol.skript.registrations.Classes;
import org.bukkit.Material;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

public class AbstractArrowSlot extends Slot {

	private final AbstractArrow projectile;

	public AbstractArrowSlot(AbstractArrow projectile) {
		this.projectile = projectile;
	}

	@Override
	public ItemStack getItem() {
		return projectile.getItemStack();
	}

	@Override
	public void setItem(@Nullable ItemStack item) {
		projectile.setItemStack(item != null ? item : new ItemStack(Material.AIR));
	}

	@Override
	public int getAmount() {
		return projectile.getItemStack().getAmount();
	}

	@Override
	public void setAmount(int amount) {
		projectile.getItemStack().setAmount(amount);
	}

	public AbstractArrow getProjectile() {
		return projectile;
	}

	@Override
	public boolean isSameSlot(Slot slot) {
		return slot instanceof AbstractArrowSlot arrowSlot
			&& arrowSlot.getProjectile().equals(projectile);
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return Classes.toString(getItem());
	}

}
