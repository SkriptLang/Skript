package ch.njol.skript.variables;

import ch.njol.yggdrasil.Fields;
import ch.njol.yggdrasil.YggdrasilSerializer;
import org.bukkit.Bukkit;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.io.NotSerializableException;
import java.io.StreamCorruptedException;

/** Item snapshots for the optional MySQL codec, independent of an online inventory owner. */
final class MySQLInventorySerializer extends YggdrasilSerializer<Inventory> {

	private static final String ID = "mysql:inventory-snapshot:1";

	@Override
	public Class<Inventory> getClass(String id) {
		return ID.equals(id) ? Inventory.class : null;
	}

	@Override
	public String getID(Class<?> type) {
		return Inventory.class.isAssignableFrom(type) ? ID : null;
	}

	@Override
	public Fields serialize(Inventory inventory) throws NotSerializableException {
		if (!Bukkit.isPrimaryThread())
			throw new NotSerializableException("Inventory snapshots require the server thread");
		// Chest inventories also cover snapshots restored by this serializer.
		if (!(inventory instanceof PlayerInventory) && inventory.getType() != InventoryType.CHEST)
			throw new NotSerializableException("Only player and chest inventory snapshots are supported");
		ItemStack[] contents = inventory.getContents();
		if (contents.length < 1 || contents.length > 54)
			throw new NotSerializableException("Unsupported inventory size: " + contents.length);
		ItemStack[] snapshot = new ItemStack[contents.length];
		for (int slot = 0; slot < contents.length; slot++)
			snapshot[slot] = contents[slot] == null ? null : contents[slot].clone();
		Fields fields = new Fields();
		// Nested items use Skript's existing ItemStack serializer, including item metadata.
		fields.putObject("contents", snapshot);
		return fields;
	}

	@Override
	public boolean canBeInstantiated(Class<? extends Inventory> type) {
		return false;
	}

	@Override
	public <E extends Inventory> E newInstance(Class<E> type) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void deserialize(Inventory inventory, Fields fields) {
		throw new UnsupportedOperationException();
	}

	@Override
	public <E extends Inventory> E deserialize(Class<E> type, Fields fields) throws StreamCorruptedException {
		if (!Bukkit.isPrimaryThread())
			throw new StreamCorruptedException("Inventory snapshots require the server thread");
		ItemStack[] contents = fields.getObject("contents", ItemStack[].class);
		if (contents == null || contents.length < 1 || contents.length > 54)
			throw new StreamCorruptedException("Invalid inventory snapshot size");
		// Bukkit cannot create a detached PlayerInventory. Preserve its 41 indices in
		// a 45-slot chest: storage 0-35, boots through helmet 36-39, offhand 40.
		Inventory snapshot = Bukkit.createInventory(null, ((contents.length + 8) / 9) * 9);
		snapshot.setContents(contents);
		return type.cast(snapshot);
	}
}
